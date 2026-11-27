package com.example.demo.repository;

import com.example.demo.model.Event;
import com.example.demo.model.EventScheduleItem;
import com.example.demo.model.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface EventScheduleItemRepository extends JpaRepository<EventScheduleItem, Long> {
    List<EventScheduleItem> findByEventAndOrganizerOrderByScheduleDateAscSortOrderAscStartTimeAsc(Event event, UserAccount organizer);
    List<EventScheduleItem> findByOrganizerOrderByScheduleDateAscStartTimeAsc(UserAccount organizer);
    List<EventScheduleItem> findByAssignedProfessionalIdAndScheduleDate(Long professionalId, LocalDate scheduleDate);
    Optional<EventScheduleItem> findByIdAndOrganizer(Long id, UserAccount organizer);
}
