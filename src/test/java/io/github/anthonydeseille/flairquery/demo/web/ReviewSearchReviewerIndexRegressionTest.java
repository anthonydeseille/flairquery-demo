package io.github.anthonydeseille.flairquery.demo.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * GET /reviews/search filters review on reviewer_name. Without an index on that column every call scans
 * the whole review table (the largest one) and filters it row by row. The fix adds
 * idx_review_reviewer_name; this test checks that the planner can match reviewer_name through an index.
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ReviewSearchReviewerIndexRegressionTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired
    TestRestTemplate http;

    @Autowired
    JdbcTemplate jdbc;

    @Test
    void reviewerNameSearchUsesAnIndex() {
        Long authorId = jdbc.queryForObject(
                "INSERT INTO author (name) VALUES ('Regression Author') RETURNING id", Long.class);
        Long bookId = jdbc.queryForObject(
                "INSERT INTO book (title, author_id) VALUES ('Regression Book', ?) RETURNING id", Long.class, authorId);
        // 2000 reviews, 100 distinct reviewers -> 20 reviews per reviewer
        jdbc.update("INSERT INTO review (book_id, rating, reviewer_name, comment, created_at) "
                + "SELECT ?, (g % 5) + 1, 'Reviewer ' || (g % 100), 'comment ' || g, now() "
                + "FROM generate_series(1, 2000) g", bookId);
        jdbc.execute("ANALYZE review");

        ResponseEntity<ReviewDto[]> response =
                http.getForEntity("/reviews/search?reviewer=Reviewer 7&limit=50", ReviewDto[].class);
        assertThat(response.getStatusCode().value())
                .as("GET /reviews/search must answer 200")
                .isEqualTo(200);
        assertThat(response.getBody())
                .as("GET /reviews/search?reviewer=Reviewer 7 must return the 20 reviews of that reviewer")
                .hasSize(20);

        List<String> plan = jdbc.execute((ConnectionCallback<List<String>>) connection -> {
            List<String> lines = new ArrayList<>();
            try (Statement st = connection.createStatement()) {
                st.execute("SET enable_seqscan = off");
                try (ResultSet rs = st.executeQuery(
                        "EXPLAIN SELECT r1_0.id, r1_0.book_id, r1_0.rating, r1_0.reviewer_name, r1_0.created_at "
                                + "FROM review r1_0 WHERE r1_0.reviewer_name = 'Reviewer 7'")) {
                    while (rs.next()) {
                        lines.add(rs.getString(1));
                    }
                } finally {
                    st.execute("RESET enable_seqscan");
                }
            }
            return lines;
        });
        String planText = String.join("\n", plan);

        assertThat(plan)
                .as("Missing index on review.reviewer_name (GET /reviews/search scans the whole review table "
                        + "and filters reviewer_name row by row). Expected the column to be matched by an "
                        + "'Index Cond' on idx_review_reviewer_name, even with sequential scans disabled. Plan:\n"
                        + planText)
                .anyMatch(line -> line.contains("Index Cond") && line.contains("reviewer_name"));
        assertThat(plan)
                .as("reviewer_name must not be evaluated as a Filter (sign of a full scan of review: the index "
                        + "idx_review_reviewer_name added by the fix is missing). Plan:\n" + planText)
                .noneMatch(line -> line.contains("Filter:") && line.contains("reviewer_name"));
    }
}
