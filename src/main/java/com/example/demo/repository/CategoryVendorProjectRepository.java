package com.example.demo.repository;

import com.example.demo.model.CategoryVendorProject;
import com.example.demo.model.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryVendorProjectRepository extends JpaRepository<CategoryVendorProject, Long> {
    List<CategoryVendorProject> findByVendorOrderByIdDesc(UserAccount vendor);
    Optional<CategoryVendorProject> findByIdAndVendor(Long id, UserAccount vendor);
}
