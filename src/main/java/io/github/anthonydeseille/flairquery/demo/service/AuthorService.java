package io.github.anthonydeseille.flairquery.demo.service;

import io.github.anthonydeseille.flairquery.demo.domain.Author;
import io.github.anthonydeseille.flairquery.demo.repository.AuthorRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuthorService {

    private final AuthorRepository authors;

    public AuthorService(AuthorRepository authors) {
        this.authors = authors;
    }

    /** Returns managed entities whose lazy books are read later, in the controller (TRAP 1 + 6). */
    public List<Author> firstAuthors(int limit) {
        return authors.findAll(PageRequest.of(0, limit, Sort.by("id"))).getContent();
    }

    @Transactional(readOnly = true)
    public List<Author> withBooks(String prefix, int page, int size) {
        return authors.findWithBooksByNamePrefix(prefix, PageRequest.of(page, size, Sort.by("id"))).getContent();
    }
}
