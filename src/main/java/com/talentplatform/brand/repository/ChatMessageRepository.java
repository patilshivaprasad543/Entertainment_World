package com.talentplatform.brand.repository;
import com.talentplatform.brand.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository("brandChatMessageRepository")
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> { List<ChatMessage> findByBrandIdAndTalentIdOrderBySentAtAsc(Long brandId, Long talentId); }
