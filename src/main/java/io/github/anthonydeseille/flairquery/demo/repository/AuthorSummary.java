package io.github.anthonydeseille.flairquery.demo.repository;

/** Interface projection: only the author columns read by {@code GET /authors}. */
public interface AuthorSummary {
    Long getId();
    String getName();
}
