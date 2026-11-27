package com.example.demo.repository;

import com.example.demo.model.PlatformCommission;
import com.example.demo.model.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlatformCommissionRepository extends JpaRepository<PlatformCommission, Long> {

    Optional<PlatformCommission> findByModuleRole(UserRole moduleRole);
}
