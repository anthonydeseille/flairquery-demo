package io.github.anthonydeseille.flairquery.demo.repository;

import io.github.anthonydeseille.flairquery.demo.domain.Book;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long> {

    @Query("select b from Book b order by b.id")
    List<Book> findPage(Pageable pageable);

    /** Full entities (with description and the EAGER publisher) although only id + title are returned. */
    List<Book> findByAuthorIdOrderById(Long authorId);
}
