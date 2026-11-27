package com.example.demo.repository;

import com.example.demo.model.CategoryVendorInvoice;
import com.example.demo.model.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryVendorInvoiceRepository extends JpaRepository<CategoryVendorInvoice, Long> {
    List<CategoryVendorInvoice> findByVendorOrderByIdDesc(UserAccount vendor);
    Optional<CategoryVendorInvoice> findByIdAndVendor(Long id, UserAccount vendor);
}
