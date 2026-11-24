package io.github.anthonydeseille.flairquery.demo.web;

import java.time.Instant;

public record ReviewDto(Long id, Long bookId, short rating, String reviewerName, Instant createdAt) { }
