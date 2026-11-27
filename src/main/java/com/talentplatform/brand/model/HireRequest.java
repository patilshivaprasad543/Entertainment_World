package com.talentplatform.brand.model;

import com.talentplatform.brand.enums.HireRequestStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "hire_requests")
public class HireRequest {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    private Long brandId;
    private Long talentId;
    private Long campaignId;
    private LocalDateTime sentAt = LocalDateTime.now();
    private LocalDateTime expiresAt = LocalDateTime.now().plusDays(15);
    private String draftUrl;
    @Enumerated(EnumType.STRING) private HireRequestStatus status = HireRequestStatus.PENDING_RESPONSE;
    public Long getId() { return id; }
    public Long getBrandId() { return brandId; }
    public void setBrandId(Long brandId) { this.brandId = brandId; }
    public Long getTalentId() { return talentId; }
    public void setTalentId(Long talentId) { this.talentId = talentId; }
    public Long getCampaignId() { return campaignId; }
    public void setCampaignId(Long campaignId) { this.campaignId = campaignId; }
    public LocalDateTime getSentAt() { return sentAt; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public String getDraftUrl() { return draftUrl; }
    public void setDraftUrl(String draftUrl) { this.draftUrl = draftUrl; }
    public HireRequestStatus getStatus() { return status; }
    public void setStatus(HireRequestStatus status) { this.status = status; }
}
