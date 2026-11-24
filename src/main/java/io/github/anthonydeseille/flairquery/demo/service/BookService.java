package io.github.anthonydeseille.flairquery.demo.service;

import io.github.anthonydeseille.flairquery.demo.repository.BookRepository;
import io.github.anthonydeseille.flairquery.demo.web.BookDto;
import io.github.anthonydeseille.flairquery.demo.web.BookTitleDto;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class BookService {

    private final BookRepository books;

    public BookService(BookRepository books) {
        this.books = books;
    }

    public List<BookDto> firstBooks(int limit) {
        return books.findPage(PageRequest.of(0, limit)).stream()
                .map(b -> new BookDto(b.getId(), b.getTitle(), b.getIsbn(), b.getPublicationYear()))
                .toList();
    }

    public List<BookTitleDto> titlesOfAuthor(long authorId) {
        return books.findByAuthorIdOrderById(authorId).stream()
                .map(b -> new BookTitleDto(b.getId(), b.getTitle()))
                .toList();
    }
}
