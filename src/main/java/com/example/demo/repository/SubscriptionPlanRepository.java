package com.example.demo.repository;

import com.example.demo.model.SubscriptionPlan;
import com.example.demo.model.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, Long> {

    List<SubscriptionPlan> findByDashboardRoleAndActiveTrueAndVisibleTrueOrderByMonthlyPriceAsc(UserRole role);

    List<SubscriptionPlan> findByOrderByDashboardRoleAscMonthlyPriceAsc();

    Optional<SubscriptionPlan> findByDashboardRoleAndTier(UserRole role, String tier);
}
