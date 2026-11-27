package com.example.demo.repository;

import com.example.demo.model.CategoryVendorQuotation;
import com.example.demo.model.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryVendorQuotationRepository extends JpaRepository<CategoryVendorQuotation, Long> {
    List<CategoryVendorQuotation> findByVendorOrderByIdDesc(UserAccount vendor);
    Optional<CategoryVendorQuotation> findByIdAndVendor(Long id, UserAccount vendor);
}
