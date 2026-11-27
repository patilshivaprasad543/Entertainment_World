package com.example.demo.repository;

import com.example.demo.model.BookingReview;
import com.example.demo.model.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingReviewRepository extends JpaRepository<BookingReview, Long> {
    List<BookingReview> findByTargetTypeAndTargetIdOrderByIdDesc(String targetType, Long targetId);
    List<BookingReview> findByTargetTypeAndTargetIdInOrderByIdDesc(String targetType, List<Long> targetIds);
    List<BookingReview> findByReviewerOrderByIdDesc(UserAccount reviewer);
    boolean existsByReviewerAndTargetTypeAndBookingId(UserAccount reviewer, String targetType, Long bookingId);
}
