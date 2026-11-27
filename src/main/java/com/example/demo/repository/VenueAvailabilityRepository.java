package com.example.demo.repository;

import com.example.demo.model.Venue;
import com.example.demo.model.VenueAvailability;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface VenueAvailabilityRepository extends JpaRepository<VenueAvailability, Long> {
    List<VenueAvailability> findByVenueAndAvailabilityDateBetweenOrderByAvailabilityDateAsc(
            Venue venue, LocalDate start, LocalDate end);
    List<VenueAvailability> findByVenueOrderByAvailabilityDateAsc(Venue venue);
    Optional<VenueAvailability> findByVenueAndAvailabilityDate(Venue venue, LocalDate date);
    void deleteByVenue(Venue venue);
}
