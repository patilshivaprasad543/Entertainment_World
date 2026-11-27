package com.example.demo.repository;

import com.example.demo.model.Event;
import com.example.demo.model.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByOrganizerOrderByIdDesc(UserAccount organizer);
    Optional<Event> findByIdAndOrganizer(Long id, UserAccount organizer);
    long countByOrganizerAndStatusIn(UserAccount organizer, List<String> statuses);
}
