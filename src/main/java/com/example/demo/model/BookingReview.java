package com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "booking_reviews", uniqueConstraints = {
        @UniqueConstraint(name = "uk_review_booking", columnNames = {"reviewer_account_id", "targetType", "bookingId"})
})
public class BookingReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "reviewer_account_id", nullable = false)
    private UserAccount reviewer;

    @Column(nullable = false, length = 20)
    private String targetType; // VENUE, PROFESSIONAL

    @Column(nullable = false)
    private Long targetId;

    @Column(nullable = false)
    private Long bookingId;

    @Column(nullable = false)
    private Integer rating; // 1 to 5

    @Column(nullable = false, length = 150)
    private String title;

    @Column(length = 1500)
    private String description;

    private LocalDateTime createdAt;

    public BookingReview() {}

    public Long getId() { return id; }
    public UserAccount getReviewer() { return reviewer; }
    public void setReviewer(UserAccount reviewer) { this.reviewer = reviewer; }
    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }
    public Long getTargetId() { return targetId; }
    public void setTargetId(Long targetId) { this.targetId = targetId; }
    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }
    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
