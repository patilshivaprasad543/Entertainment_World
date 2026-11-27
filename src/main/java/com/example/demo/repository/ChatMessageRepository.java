package com.example.demo.repository;

import com.example.demo.model.ChatMessage;
import com.example.demo.model.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    @Query("SELECT m FROM ChatMessage m WHERE (m.sender = :u1 AND m.recipient = :u2) OR (m.sender = :u2 AND m.recipient = :u1) ORDER BY m.sentAt ASC")
    List<ChatMessage> findConversation(@Param("u1") UserAccount u1, @Param("u2") UserAccount u2);

    List<ChatMessage> findByRecipientAndReadFlagFalse(UserAccount recipient);
}
