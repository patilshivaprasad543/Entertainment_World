package com.example.demo.repository;

import com.example.demo.model.Event;
import com.example.demo.model.ProfessionalShortlist;
import com.example.demo.model.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProfessionalShortlistRepository extends JpaRepository<ProfessionalShortlist, Long> {
    List<ProfessionalShortlist> findByOrganizerOrderByIdDesc(UserAccount organizer);
    List<ProfessionalShortlist> findByOrganizerAndEventOrderByIdDesc(UserAccount organizer, Event event);
    Optional<ProfessionalShortlist> findByIdAndOrganizer(Long id, UserAccount organizer);
    boolean existsByOrganizerAndEventAndProfessional(UserAccount organizer, Event event, UserAccount professional);
}
