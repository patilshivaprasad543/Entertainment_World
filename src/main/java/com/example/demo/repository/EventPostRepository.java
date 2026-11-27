package com.example.demo.repository;

import com.example.demo.model.EventPost;
import com.example.demo.model.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EventPostRepository extends JpaRepository<EventPost, Long> {
    List<EventPost> findAllByOrderByIdDesc();
    List<EventPost> findByOrganizerOrderByIdDesc(UserAccount organizer);
    Optional<EventPost> findByIdAndOrganizer(Long id, UserAccount organizer);
}
