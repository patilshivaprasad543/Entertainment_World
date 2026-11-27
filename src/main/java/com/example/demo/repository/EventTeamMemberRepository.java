package com.example.demo.repository;

import com.example.demo.model.Event;
import com.example.demo.model.EventTeamMember;
import com.example.demo.model.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EventTeamMemberRepository extends JpaRepository<EventTeamMember, Long> {
    List<EventTeamMember> findByEventAndOrganizerOrderByIdDesc(Event event, UserAccount organizer);
    Optional<EventTeamMember> findByIdAndOrganizer(Long id, UserAccount organizer);
}
