package io.github.anthonydeseille.flairquery.demo.web;

import io.github.anthonydeseille.flairquery.demo.service.BookService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/books")
@Validated
public class BookController {

    private final BookService books;

    public BookController(BookService books) {
        this.books = books;
    }

    /** TRAP 2: Book.publisher is EAGER (default) and never read here. */
    @GetMapping
    public List<BookDto> list(@RequestParam(defaultValue = "100") @Min(1) @Max(1000) int limit) {
        return books.firstBooks(limit);
    }

    /** TRAP 3: over-fetch, full entities loaded to return two fields. */
    @GetMapping("/titles")
    public List<BookTitleDto> titles(@RequestParam long authorId) {
        return books.titlesOfAuthor(authorId);
    }
}
