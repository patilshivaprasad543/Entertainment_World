package com.example.demo.repository;

import com.example.demo.model.CategoryVendorClient;
import com.example.demo.model.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryVendorClientRepository extends JpaRepository<CategoryVendorClient, Long> {
    List<CategoryVendorClient> findByVendorOrderByIdDesc(UserAccount vendor);
    Optional<CategoryVendorClient> findByIdAndVendor(Long id, UserAccount vendor);
}
