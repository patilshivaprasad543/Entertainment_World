package com.example.demo.repository;

import com.example.demo.model.UserAccount;
import com.example.demo.model.Venue;
import com.example.demo.model.VenueEnquiry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VenueEnquiryRepository extends JpaRepository<VenueEnquiry, Long> {
    List<VenueEnquiry> findByVendorOrderByIdDesc(UserAccount vendor);
    Optional<VenueEnquiry> findByIdAndVendor(Long id, UserAccount vendor);
    List<VenueEnquiry> findByRequesterOrderByIdDesc(UserAccount requester);
    long countByVendor(UserAccount vendor);
    long countByVendorAndStatus(UserAccount vendor, String status);
    List<VenueEnquiry> findByVenueOrderByIdDesc(Venue venue);
    void deleteByVenue(Venue venue);
}
