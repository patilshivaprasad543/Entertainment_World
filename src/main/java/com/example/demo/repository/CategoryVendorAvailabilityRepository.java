package com.example.demo.repository;

import com.example.demo.model.CategoryVendorAvailability;
import com.example.demo.model.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CategoryVendorAvailabilityRepository extends JpaRepository<CategoryVendorAvailability, Long> {
    List<CategoryVendorAvailability> findByVendorOrderByAvailableDateAsc(UserAccount vendor);
    Optional<CategoryVendorAvailability> findByVendorAndAvailableDate(UserAccount vendor, LocalDate date);
}
