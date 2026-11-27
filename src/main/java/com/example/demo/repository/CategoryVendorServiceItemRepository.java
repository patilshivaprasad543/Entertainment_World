package com.example.demo.repository;

import com.example.demo.model.CategoryVendorServiceItem;
import com.example.demo.model.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryVendorServiceItemRepository extends JpaRepository<CategoryVendorServiceItem, Long> {
    List<CategoryVendorServiceItem> findByVendorOrderByIdDesc(UserAccount vendor);
    Optional<CategoryVendorServiceItem> findByIdAndVendor(Long id, UserAccount vendor);
    long countByVendor(UserAccount vendor);
    long countByVendorAndActiveTrue(UserAccount vendor);
}
