package io.github.anthonydeseille.flairquery.demo.service;

import io.github.anthonydeseille.flairquery.demo.domain.Author;
import io.github.anthonydeseille.flairquery.demo.repository.AuthorBookTitle;
import io.github.anthonydeseille.flairquery.demo.repository.AuthorRepository;
import io.github.anthonydeseille.flairquery.demo.repository.AuthorSummary;
import io.github.anthonydeseille.flairquery.demo.repository.BookRepository;
import io.github.anthonydeseille.flairquery.demo.web.AuthorBooksDto;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AuthorService {

    private final AuthorRepository authors;
    private final BookRepository books;

    public AuthorService(AuthorRepository authors, BookRepository books) {
        this.authors = authors;
        this.books = books;
    }

    /**
     * First authors by id with their book count and first three titles (by book id): one query for the
     * authors, one for the titles of all of their books, no lazy loading.
     */
    @Transactional(readOnly = true)
    public List<AuthorBooksDto> firstAuthors(int limit) {
        List<AuthorSummary> page = authors.findSummaries(PageRequest.of(0, limit));
        if (page.isEmpty()) {
            return List.of();
        }
        List<Long> ids = page.stream().map(AuthorSummary::getId).toList();
        Map<Long, List<String>> titlesByAuthor = new HashMap<>();
        for (AuthorBookTitle t : books.findTitlesByAuthorIds(ids)) {
            titlesByAuthor.computeIfAbsent(t.getAuthorId(), k -> new ArrayList<>()).add(t.getTitle());
        }
        return page.stream().map(a -> {
            List<String> titles = titlesByAuthor.getOrDefault(a.getId(), List.of());
            return new AuthorBooksDto(a.getId(), a.getName(), titles.size(),
                    titles.stream().limit(3).toList());
        }).toList();
    }

    @Transactional(readOnly = true)
    public List<Author> withBooks(String prefix, int page, int size) {
        return authors.findWithBooksByNamePrefix(prefix, PageRequest.of(page, size, Sort.by("id"))).getContent();
    }
}
