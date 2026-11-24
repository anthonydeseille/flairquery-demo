package io.github.anthonydeseille.flairquery.demo.web;

import io.github.anthonydeseille.flairquery.demo.repository.ReviewRepository;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/reviews")
@Validated
@Transactional(readOnly = true)
public class ReviewController {

    private final ReviewRepository reviews;

    public ReviewController(ReviewRepository reviews) {
        this.reviews = reviews;
    }

    /** TRAP 4: filter on the non-indexed reviewer_name column of the largest table. */
    @GetMapping("/search")
    public List<ReviewDto> search(@RequestParam String reviewer,
                                  @RequestParam(defaultValue = "20") @Min(1) @Max(200) int limit) {
        return reviews.searchByReviewer(reviewer, PageRequest.of(0, limit));
    }

    /** TRAP 7: big sort on a non-indexed column, deep page → external merge sort on disk. */
    @GetMapping("/ranking")
    public List<ReviewDto> ranking(@RequestParam(defaultValue = "400") @Min(0) @Max(100_000) int page,
                                   @RequestParam(defaultValue = "50") @Min(1) @Max(200) int size) {
        return reviews.ranking(PageRequest.of(page, size));
    }
}
