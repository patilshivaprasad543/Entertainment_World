package com.example.demo.repository;

import com.example.demo.model.OrganizerTeamMember;
import com.example.demo.model.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrganizerTeamMemberRepository extends JpaRepository<OrganizerTeamMember, Long> {
    List<OrganizerTeamMember> findByOrganizerOrderByIdDesc(UserAccount organizer);
    Optional<OrganizerTeamMember> findByIdAndOrganizer(Long id, UserAccount organizer);
    long countByOrganizer(UserAccount organizer);
}
