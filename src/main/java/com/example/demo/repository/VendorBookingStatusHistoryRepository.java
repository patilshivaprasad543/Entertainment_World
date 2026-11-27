package com.example.demo.repository;

import com.example.demo.model.VendorBooking;
import com.example.demo.model.VendorBookingStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VendorBookingStatusHistoryRepository extends JpaRepository<VendorBookingStatusHistory, Long> {

    List<VendorBookingStatusHistory> findByBookingOrderByChangedAtAsc(VendorBooking booking);
}
