package io.github.anthonydeseille.flairquery.demo.repository;

/** Interface projection: the title of a book together with the id of its author. */
public interface AuthorBookTitle {
    Long getAuthorId();
    String getTitle();
}
