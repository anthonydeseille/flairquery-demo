package io.github.anthonydeseille.flairquery.demo.repository;

import io.github.anthonydeseille.flairquery.demo.domain.Publisher;
import io.github.anthonydeseille.flairquery.demo.web.PublisherDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PublisherRepository extends JpaRepository<Publisher, Long> {

    /** Witness: DTO projection, primary-key lookup, exactly one query. */
    @Query("select new io.github.anthonydeseille.flairquery.demo.web.PublisherDto(p.id, p.name, p.country) "
            + "from Publisher p where p.id = :id")
    Optional<PublisherDto> findDtoById(@Param("id") Long id);
}
