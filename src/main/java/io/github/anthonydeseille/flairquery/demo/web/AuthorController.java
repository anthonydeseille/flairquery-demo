package io.github.anthonydeseille.flairquery.demo.web;

import io.github.anthonydeseille.flairquery.demo.domain.Author;
import io.github.anthonydeseille.flairquery.demo.domain.Book;
import io.github.anthonydeseille.flairquery.demo.service.AuthorService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/authors")
@Validated
public class AuthorController {

    private final AuthorService authors;

    public AuthorController(AuthorService authors) {
        this.authors = authors;
    }

    /**
     * Authors with their book count and first titles, built in a read-only transaction from two projection
     * queries (authors, then the titles of their books): no lazy loading in the web layer.
     */
    @GetMapping
    public List<AuthorBooksDto> list(@RequestParam(defaultValue = "50") @Min(1) @Max(500) int limit) {
        return authors.firstAuthors(limit);
    }

    /** TRAP 5: JOIN FETCH + pagination (HHH90003004, pagination applied in memory). */
    @GetMapping("/with-books")
    public List<AuthorBooksDto> withBooks(@RequestParam(defaultValue = "Al") String prefix,
                                          @RequestParam(defaultValue = "0") @Min(0) int page,
                                          @RequestParam(defaultValue = "20") @Min(1) @Max(200) int size) {
        return authors.withBooks(prefix, page, size).stream().map(AuthorController::toDto).toList();
    }

    private static AuthorBooksDto toDto(Author a) {
        List<Book> books = a.getBooks();
        return new AuthorBooksDto(a.getId(), a.getName(), books.size(),
                books.stream().limit(3).map(Book::getTitle).toList());
    }
}
