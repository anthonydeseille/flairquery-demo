package io.github.anthonydeseille.flairquery.demo.web;

import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Guards GET /authors against the N+1 on Author.books (lazy collection loaded once per author, in the web
 * layer thanks to open-in-view). The fixed code reads the authors and the titles of their books with two
 * queries, whatever the number of authors.
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.jpa.properties.hibernate.generate_statistics=true")
class AuthorsListQueriesRegressionTest {

    private static final int AUTHORS = 4;
    private static final int BOOKS_PER_AUTHOR = 3;

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired
    TestRestTemplate http;

    @Autowired
    JdbcTemplate jdbc;

    @Autowired
    EntityManagerFactory emf;

    @BeforeEach
    void data() {
        jdbc.update("delete from review");
        jdbc.update("delete from book");
        jdbc.update("delete from author");
        jdbc.update("delete from publisher");
        Long publisherId = jdbc.queryForObject(
                "insert into publisher (name, country) values ('Pub', 'FR') returning id", Long.class);
        for (int a = 0; a < AUTHORS; a++) {
            Long authorId = jdbc.queryForObject(
                    "insert into author (name, bio) values (?, 'a long bio') returning id", Long.class,
                    "Author " + a);
            for (int b = 0; b < BOOKS_PER_AUTHOR; b++) {
                jdbc.update("insert into book (title, description, author_id, publisher_id) values (?, ?, ?, ?)",
                        "Title " + a + "-" + b, "a long description", authorId, publisherId);
            }
        }
    }

    @Test
    void listingAuthorsDoesNotLoadBooksOncePerAuthor() {
        SessionFactory sf = emf.unwrap(SessionFactory.class);
        sf.getStatistics().clear();

        ResponseEntity<AuthorBooksDto[]> response = http.getForEntity("/authors?limit=10", AuthorBooksDto[].class);

        long statements = sf.getStatistics().getPrepareStatementCount();

        assertThat(response.getStatusCode().value()).as("GET /authors status").isEqualTo(200);
        assertThat(response.getBody()).as("GET /authors returns every author of the data set").hasSize(AUTHORS);
        for (AuthorBooksDto dto : response.getBody()) {
            assertThat(dto.bookCount()).as("book count of author %s", dto.name()).isEqualTo(BOOKS_PER_AUTHOR);
            assertThat(dto.firstTitles()).as("first titles of author %s", dto.name()).hasSize(BOOKS_PER_AUTHOR);
        }
        assertThat(statements)
                .as("GET /authors with %d authors ran %d SQL statements: expected at most 2 (one query for the "
                        + "authors, one for the titles of all their books). More means the N+1 on Author.books "
                        + "is back (one lazy books query per author, run in the controller via open-in-view).",
                        AUTHORS, statements)
                .isLessThanOrEqualTo(2);
    }
}
