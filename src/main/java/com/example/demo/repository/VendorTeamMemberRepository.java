package com.example.demo.repository;

import com.example.demo.model.UserAccount;
import com.example.demo.model.VendorTeamMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VendorTeamMemberRepository extends JpaRepository<VendorTeamMember, Long> {
    List<VendorTeamMember> findByVendorOrderByIdDesc(UserAccount vendor);
    Optional<VendorTeamMember> findByIdAndVendor(Long id, UserAccount vendor);
    long countByVendor(UserAccount vendor);
}
