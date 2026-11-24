package io.github.anthonydeseille.flairquery.demo.web;

import java.util.List;

public record AuthorBooksDto(Long id, String name, int bookCount, List<String> firstTitles) { }
