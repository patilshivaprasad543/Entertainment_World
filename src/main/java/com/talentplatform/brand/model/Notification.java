package com.talentplatform.brand.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity @Table(name = "brand_notifications")
public class Notification {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    private Long brandId;
    private String type;
    private String message;
    @Column(name = "is_read") private boolean read;
    private LocalDateTime createdAt = LocalDateTime.now();
    public Long getId() { return id; }
    public Long getBrandId() { return brandId; }
    public void setBrandId(Long brandId) { this.brandId = brandId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
