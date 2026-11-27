package com.example.demo.repository;

import com.example.demo.model.UserAccount;
import com.example.demo.model.Venue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VenueRepository extends JpaRepository<Venue, Long> {
    List<Venue> findByVendorOrderByIdDesc(UserAccount vendor);
    Optional<Venue> findByIdAndVendor(Long id, UserAccount vendor);
    long countByVendor(UserAccount vendor);
    long countByVendorAndActiveTrue(UserAccount vendor);
    List<Venue> findByActiveTrueAndFlaggedFalseOrderByIdDesc();
}
