package io.github.anthonydeseille.flairquery.demo.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "review")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    private short rating;

    /** TRAP 4: filtered on by GET /reviews/search, intentionally NOT indexed. */
    @Column(name = "reviewer_name")
    private String reviewerName;

    @Column(columnDefinition = "text")
    private String comment;

    /** TRAP 7: sorted on by GET /reviews/ranking, not indexed (big sort + low work_mem → disk). */
    @Column(name = "created_at")
    private Instant createdAt;

    protected Review() { }

    public Long getId() { return id; }
    public Book getBook() { return book; }
    public short getRating() { return rating; }
    public String getReviewerName() { return reviewerName; }
    public String getComment() { return comment; }
    public Instant getCreatedAt() { return createdAt; }
}
