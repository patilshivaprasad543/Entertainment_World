package com.example.demo.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "platform_commissions")
public class PlatformCommission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 32, columnDefinition = "VARCHAR(32)")
    private UserRole moduleRole;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal commissionPercent;

    @Column(length = 255)
    private String description;

    private LocalDateTime updatedAt;

    public PlatformCommission() {
    }

    public PlatformCommission(UserRole moduleRole, BigDecimal commissionPercent, String description) {
        this.moduleRole = moduleRole;
        this.commissionPercent = commissionPercent;
        this.description = description;
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public UserRole getModuleRole() { return moduleRole; }
    public void setModuleRole(UserRole moduleRole) { this.moduleRole = moduleRole; }
    public BigDecimal getCommissionPercent() { return commissionPercent; }
    public void setCommissionPercent(BigDecimal commissionPercent) { this.commissionPercent = commissionPercent; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
