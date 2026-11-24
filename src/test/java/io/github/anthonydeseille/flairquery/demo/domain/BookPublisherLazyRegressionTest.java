package io.github.anthonydeseille.flairquery.demo.domain;

import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
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

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Guards Book.publisher being LAZY: GET /authors/with-books never reads the publisher, yet with the
 * JPA default (EAGER) on @ManyToOne Hibernate runs one extra select per distinct publisher after the
 * JOIN FETCH query.
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.jpa.properties.hibernate.generate_statistics=true")
class BookPublisherLazyRegressionTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired
    TestRestTemplate http;

    @Autowired
    JdbcTemplate jdbc;

    @Autowired
    EntityManagerFactory entityManagerFactory;

    @BeforeEach
    void data() {
        jdbc.update("DELETE FROM review");
        jdbc.update("DELETE FROM book");
        jdbc.update("DELETE FROM author");
        jdbc.update("DELETE FROM publisher");

        for (int p = 1; p <= 4; p++) {
            jdbc.update("INSERT INTO publisher (id, name, country) VALUES (?, ?, ?)", p, "Publisher " + p, "FR");
        }
        String[] names = {"Alice Martin", "Albert Durand", "Alan Petit", "Bob Leroy"};
        long bookId = 1;
        for (int a = 0; a < names.length; a++) {
            long authorId = a + 1;
            jdbc.update("INSERT INTO author (id, name) VALUES (?, ?)", authorId, names[a]);
            for (int b = 0; b < 3; b++) {
                long publisherId = (bookId % 4) + 1;
                jdbc.update("INSERT INTO book (id, title, author_id, publisher_id) VALUES (?, ?, ?, ?)",
                        bookId, "Book " + bookId, authorId, publisherId);
                bookId++;
            }
        }
    }

    @Test
    void authorsWithBooksDoesNotLoadUnreadPublishers() {
        Statistics stats = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        stats.clear();

        ResponseEntity<List> response = http.getForEntity("/authors/with-books?prefix=Al&size=2", List.class);

        long statements = stats.getPrepareStatementCount();

        assertThat(response.getStatusCode().value()).as("GET /authors/with-books status").isEqualTo(200);
        assertThat(response.getBody()).as("page of 2 authors whose name starts with 'Al'").hasSize(2);
        assertThat(((Map<?, ?>) response.getBody().get(0)).get("id")).isNotNull();

        assertThat(statements)
                .as("GET /authors/with-books should run at most 2 SQL statements (JOIN FETCH query + count), "
                        + "but ran %d: Book.publisher is loaded EAGERly (JPA default for @ManyToOne) although "
                        + "it is never read, causing one extra select per publisher. "
                        + "Keep @ManyToOne(fetch = FetchType.LAZY) on Book.publisher.", statements)
                .isLessThanOrEqualTo(2);
    }
}
