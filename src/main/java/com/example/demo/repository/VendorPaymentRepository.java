package com.example.demo.repository;

import com.example.demo.model.VendorInvoice;
import com.example.demo.model.VendorPayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VendorPaymentRepository extends JpaRepository<VendorPayment, Long> {

    List<VendorPayment> findByInvoiceOrderByRecordedAtDesc(VendorInvoice invoice);
}
