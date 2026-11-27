package com.example.demo.repository;

import com.example.demo.model.UserAccount;
import com.example.demo.model.Venue;
import com.example.demo.model.VenueMedia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VenueMediaRepository extends JpaRepository<VenueMedia, Long> {
    List<VenueMedia> findByVenueOrderByIdDesc(Venue venue);
    List<VenueMedia> findByVenueAndMediaTypeOrderByIdDesc(Venue venue, String mediaType);
    List<VenueMedia> findByVendorOrderByIdDesc(UserAccount vendor);
    Optional<VenueMedia> findByIdAndVendor(Long id, UserAccount vendor);
    long countByVenueAndMediaType(Venue venue, String mediaType);
    void deleteByVenue(Venue venue);
}
