package com.example.demo.repository;

import com.example.demo.model.Event;
import com.example.demo.model.EventExpense;
import com.example.demo.model.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EventExpenseRepository extends JpaRepository<EventExpense, Long> {
    List<EventExpense> findByEventAndOrganizerOrderByIdDesc(Event event, UserAccount organizer);
    List<EventExpense> findByOrganizerOrderByIdDesc(UserAccount organizer);
    Optional<EventExpense> findByIdAndOrganizer(Long id, UserAccount organizer);
}
