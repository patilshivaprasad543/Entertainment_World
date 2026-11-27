package com.example.demo.repository;

import com.example.demo.model.UserAccount;
import com.example.demo.model.VendorNotification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VendorNotificationRepository extends JpaRepository<VendorNotification, Long> {

    List<VendorNotification> findByRecipientOrderByCreatedAtDesc(UserAccount recipient);
}
