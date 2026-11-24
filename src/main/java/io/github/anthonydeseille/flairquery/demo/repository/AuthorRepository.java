package io.github.anthonydeseille.flairquery.demo.repository;

import io.github.anthonydeseille.flairquery.demo.domain.Author;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuthorRepository extends JpaRepository<Author, Long> {

    /**
     * TRAP 5: collection JOIN FETCH + pagination. Hibernate cannot paginate in SQL, logs HHH90003004
     * ("firstResult/maxResults specified with collection fetch; applying in memory") and loads every
     * matching author with all of its books before cutting the page in memory.
     */
    @Query(value = "select distinct a from Author a join fetch a.books where a.name like concat(:prefix, '%')",
           countQuery = "select count(a) from Author a where a.name like concat(:prefix, '%')")
    Page<Author> findWithBooksByNamePrefix(@Param("prefix") String prefix, Pageable pageable);
}
