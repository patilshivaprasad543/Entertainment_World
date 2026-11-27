package com.example.demo.repository;

import com.example.demo.model.Event;
import com.example.demo.model.EventProfessionalBooking;
import com.example.demo.model.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EventProfessionalBookingRepository extends JpaRepository<EventProfessionalBooking, Long> {
    List<EventProfessionalBooking> findByOrganizerOrderByIdDesc(UserAccount organizer);
    List<EventProfessionalBooking> findByEventOrderByIdDesc(Event event);
    Optional<EventProfessionalBooking> findByIdAndOrganizer(Long id, UserAccount organizer);
    boolean existsByEventAndProfessionalAndStatusNotIn(Event event, UserAccount professional, List<String> closedStatuses);
}
