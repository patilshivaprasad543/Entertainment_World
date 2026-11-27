package com.example.demo.repository;

import com.example.demo.model.EventPaymentRecord;
import com.example.demo.model.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EventPaymentRecordRepository extends JpaRepository<EventPaymentRecord, Long> {
    List<EventPaymentRecord> findByOrganizerOrderByIdDesc(UserAccount organizer);
    Optional<EventPaymentRecord> findByIdAndOrganizer(Long id, UserAccount organizer);
}
