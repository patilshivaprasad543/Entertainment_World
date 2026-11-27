package com.example.demo.repository;

import com.example.demo.model.UserAccount;
import com.example.demo.model.VendorService;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VendorServiceRepository extends JpaRepository<VendorService, Long> {

    List<VendorService> findByVendorOrderByIdDesc(UserAccount vendor);

    List<VendorService> findByActiveTrueOrderByIdDesc();

    long countByVendor(UserAccount vendor);

    long countByVendorAndActiveTrue(UserAccount vendor);
}
