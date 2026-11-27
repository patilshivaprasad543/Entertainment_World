package com.example.demo.repository;

import com.example.demo.model.UserAccount;
import com.example.demo.model.VendorLocation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VendorLocationRepository extends JpaRepository<VendorLocation, Long> {
    List<VendorLocation> findByVendorOrderByIdDesc(UserAccount vendor);
    Optional<VendorLocation> findByIdAndVendor(Long id, UserAccount vendor);
    long countByVendor(UserAccount vendor);
}
