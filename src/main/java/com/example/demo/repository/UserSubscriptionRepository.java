package com.example.demo.repository;

import com.example.demo.model.UserAccount;
import com.example.demo.model.UserSubscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, Long> {

    List<UserSubscription> findByUserOrderByIdDesc(UserAccount user);

    Optional<UserSubscription> findFirstByUserAndStatusOrderByIdDesc(UserAccount user, String status);

    List<UserSubscription> findAllByOrderByIdDesc();
}
