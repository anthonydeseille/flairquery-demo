package io.github.anthonydeseille.flairquery.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Liquibase schema and JPA mapping agree (ddl-auto=validate) and every endpoint answers on an empty
 * database. Data-dependent behaviour (traps) is covered by the it/ module with the corpus.
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SchemaMappingTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired
    TestRestTemplate http;

    @Test
    void endpointsAnswerOnEmptyDatabase() {
        for (String path : new String[]{"/authors", "/books", "/books/titles?authorId=1",
                "/reviews/search?reviewer=x", "/authors/with-books", "/reviews/ranking?page=0"}) {
            assertThat(http.getForEntity(path, String.class).getStatusCode().value()).as(path).isEqualTo(200);
        }
        assertThat(http.getForEntity("/publishers/1", String.class).getStatusCode().value()).isEqualTo(404);
    }
}
