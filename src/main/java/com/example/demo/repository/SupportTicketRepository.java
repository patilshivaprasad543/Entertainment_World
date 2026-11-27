package com.example.demo.repository;

import com.example.demo.model.SupportTicket;
import com.example.demo.model.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> {

    List<SupportTicket> findByUserOrderByCreatedAtDesc(UserAccount user);

    List<SupportTicket> findAllByOrderByCreatedAtDesc();
}
