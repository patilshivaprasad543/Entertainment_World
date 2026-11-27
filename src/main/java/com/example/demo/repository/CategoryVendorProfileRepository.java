package com.example.demo.repository;

import com.example.demo.model.CategoryVendorProfile;
import com.example.demo.model.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryVendorProfileRepository extends JpaRepository<CategoryVendorProfile, Long> {
    Optional<CategoryVendorProfile> findByUserAccount(UserAccount userAccount);
}
