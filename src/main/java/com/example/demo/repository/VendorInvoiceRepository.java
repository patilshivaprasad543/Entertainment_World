package com.example.demo.repository;

import com.example.demo.model.UserAccount;
import com.example.demo.model.VendorBooking;
import com.example.demo.model.VendorInvoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VendorInvoiceRepository extends JpaRepository<VendorInvoice, Long> {

    List<VendorInvoice> findByVendorOrderByIdDesc(UserAccount vendor);

    Optional<VendorInvoice> findByBooking(VendorBooking booking);
}
