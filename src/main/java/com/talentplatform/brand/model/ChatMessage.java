package com.talentplatform.brand.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity(name = "BrandChatMessage") @Table(name = "brand_chat_messages")
public class ChatMessage {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    private Long brandId;
    private Long talentId;
    private String senderType;
    @Column(length = 4000) private String message;
    private LocalDateTime sentAt = LocalDateTime.now();
    public Long getId() { return id; }
    public Long getBrandId() { return brandId; }
    public void setBrandId(Long brandId) { this.brandId = brandId; }
    public Long getTalentId() { return talentId; }
    public void setTalentId(Long talentId) { this.talentId = talentId; }
    public String getSenderType() { return senderType; }
    public void setSenderType(String senderType) { this.senderType = senderType; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public LocalDateTime getSentAt() { return sentAt; }
}
