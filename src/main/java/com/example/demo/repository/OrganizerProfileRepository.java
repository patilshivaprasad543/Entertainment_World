package com.example.demo.repository;

import com.example.demo.model.OrganizerProfile;
import com.example.demo.model.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrganizerProfileRepository extends JpaRepository<OrganizerProfile, Long> {
    Optional<OrganizerProfile> findByUserAccount(UserAccount userAccount);
}
