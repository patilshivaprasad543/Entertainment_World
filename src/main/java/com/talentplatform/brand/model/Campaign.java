package com.talentplatform.brand.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "campaigns")
public class Campaign {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    private Long brandId;
    private String title;
    private String shootType;
    private String shootCity;
    private String deliverables;
    private String usageRightsDuration;
    private boolean productShippingRequired;
    private String courierTrackingId;
    private BigDecimal budgetPerTalent = BigDecimal.ZERO;
    @Column(length = 4000) private String brief;
    private String status = "ACTIVE";
    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getId() { return id; }
    public Long getBrandId() { return brandId; }
    public void setBrandId(Long brandId) { this.brandId = brandId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getShootType() { return shootType; }
    public void setShootType(String shootType) { this.shootType = shootType; }
    public String getShootCity() { return shootCity; }
    public void setShootCity(String shootCity) { this.shootCity = shootCity; }
    public String getDeliverables() { return deliverables; }
    public void setDeliverables(String deliverables) { this.deliverables = deliverables; }
    public String getUsageRightsDuration() { return usageRightsDuration; }
    public void setUsageRightsDuration(String usageRightsDuration) { this.usageRightsDuration = usageRightsDuration; }
    public boolean isProductShippingRequired() { return productShippingRequired; }
    public void setProductShippingRequired(boolean productShippingRequired) { this.productShippingRequired = productShippingRequired; }
    public String getCourierTrackingId() { return courierTrackingId; }
    public void setCourierTrackingId(String courierTrackingId) { this.courierTrackingId = courierTrackingId; }
    public BigDecimal getBudgetPerTalent() { return budgetPerTalent; }
    public void setBudgetPerTalent(BigDecimal budgetPerTalent) { this.budgetPerTalent = budgetPerTalent; }
    public String getBrief() { return brief; }
    public void setBrief(String brief) { this.brief = brief; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
