package com.example.demo.repository;

import com.example.demo.model.UserAccount;
import com.example.demo.model.Venue;
import com.example.demo.model.VenueBooking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface VenueBookingRepository extends JpaRepository<VenueBooking, Long> {
    List<VenueBooking> findByVendorOrderByIdDesc(UserAccount vendor);
    List<VenueBooking> findByRequesterOrderByIdDesc(UserAccount requester);
    List<VenueBooking> findByEventIdOrderByIdDesc(Long eventId);
    Optional<VenueBooking> findByIdAndVendor(Long id, UserAccount vendor);
    List<VenueBooking> findByVenueOrderByIdDesc(Venue venue);
    boolean existsByVenueAndEventDateAndStatus(Venue venue, LocalDate eventDate, String status);
    long countByVendor(UserAccount vendor);
    long countByVendorAndStatus(UserAccount vendor, String status);
    void deleteByVenue(Venue venue);
}
