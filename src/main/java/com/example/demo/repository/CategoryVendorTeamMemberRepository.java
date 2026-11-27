package com.example.demo.repository;

import com.example.demo.model.CategoryVendorTeamMember;
import com.example.demo.model.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryVendorTeamMemberRepository extends JpaRepository<CategoryVendorTeamMember, Long> {
    List<CategoryVendorTeamMember> findByVendorOrderByIdDesc(UserAccount vendor);
    Optional<CategoryVendorTeamMember> findByIdAndVendor(Long id, UserAccount vendor);
}
