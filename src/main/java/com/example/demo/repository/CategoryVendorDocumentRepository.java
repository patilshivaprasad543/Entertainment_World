package com.example.demo.repository;

import com.example.demo.model.CategoryVendorDocument;
import com.example.demo.model.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryVendorDocumentRepository extends JpaRepository<CategoryVendorDocument, Long> {
    List<CategoryVendorDocument> findByVendorOrderByIdDesc(UserAccount vendor);
    Optional<CategoryVendorDocument> findByIdAndVendor(Long id, UserAccount vendor);
}
