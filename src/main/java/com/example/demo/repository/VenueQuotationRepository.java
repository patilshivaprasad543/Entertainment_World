package com.example.demo.repository;

import com.example.demo.model.UserAccount;
import com.example.demo.model.Venue;
import com.example.demo.model.VenueQuotation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VenueQuotationRepository extends JpaRepository<VenueQuotation, Long> {
    List<VenueQuotation> findByVendorOrderByIdDesc(UserAccount vendor);
    Optional<VenueQuotation> findByIdAndVendor(Long id, UserAccount vendor);
    List<VenueQuotation> findByCustomerEmailOrderByIdDesc(String customerEmail);
    List<VenueQuotation> findByEnquiryRequesterOrderByIdDesc(UserAccount requester);
    void deleteByVenue(Venue venue);
}
