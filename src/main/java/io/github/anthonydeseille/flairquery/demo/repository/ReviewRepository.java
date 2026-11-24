package io.github.anthonydeseille.flairquery.demo.repository;

import io.github.anthonydeseille.flairquery.demo.domain.Review;
import io.github.anthonydeseille.flairquery.demo.web.ReviewDto;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    /** TRAP 4: sequential scan on a large table (reviewer_name has no index). */
    @Query("select new io.github.anthonydeseille.flairquery.demo.web.ReviewDto(r.id, r.book.id, r.rating, r.reviewerName, r.createdAt) "
            + "from Review r where r.reviewerName = :reviewer order by r.id")
    List<ReviewDto> searchByReviewer(@Param("reviewer") String reviewer, Pageable pageable);

    /**
     * TRAP 7: ORDER BY a non-indexed column with a deep OFFSET. PostgreSQL must keep offset + limit rows;
     * once that exceeds work_mem the bounded heap sort turns into an external merge sort on disk.
     */
    @Query("select new io.github.anthonydeseille.flairquery.demo.web.ReviewDto(r.id, r.book.id, r.rating, r.reviewerName, r.createdAt) "
            + "from Review r order by r.createdAt desc, r.comment")
    List<ReviewDto> ranking(Pageable pageable);
}
