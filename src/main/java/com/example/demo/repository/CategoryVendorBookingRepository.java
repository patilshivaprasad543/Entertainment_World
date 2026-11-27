package com.example.demo.repository;

import com.example.demo.model.CategoryVendorBooking;
import com.example.demo.model.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryVendorBookingRepository extends JpaRepository<CategoryVendorBooking, Long> {
    List<CategoryVendorBooking> findByVendorOrderByIdDesc(UserAccount vendor);
    Optional<CategoryVendorBooking> findByIdAndVendor(Long id, UserAccount vendor);
}
