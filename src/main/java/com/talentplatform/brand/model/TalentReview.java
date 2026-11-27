package com.talentplatform.brand.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity @Table(name = "talent_reviews", uniqueConstraints = @UniqueConstraint(columnNames = "hire_request_id"))
public class TalentReview {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "hire_request_id") private Long hireRequestId;
    private Long brandId;
    private Long talentId;
    private int overallRating;
    private int qualityRating;
    private int punctualityRating;
    private int communicationRating;
    @Column(length = 2000) private String reviewComment;
    private boolean wouldHireAgain;
    private LocalDateTime createdAt = LocalDateTime.now();
    public Long getId() { return id; }
    public Long getHireRequestId() { return hireRequestId; }
    public void setHireRequestId(Long hireRequestId) { this.hireRequestId = hireRequestId; }
    public Long getBrandId() { return brandId; }
    public void setBrandId(Long brandId) { this.brandId = brandId; }
    public Long getTalentId() { return talentId; }
    public void setTalentId(Long talentId) { this.talentId = talentId; }
    public int getOverallRating() { return overallRating; }
    public void setOverallRating(int overallRating) { this.overallRating = overallRating; }
    public int getQualityRating() { return qualityRating; }
    public void setQualityRating(int qualityRating) { this.qualityRating = qualityRating; }
    public int getPunctualityRating() { return punctualityRating; }
    public void setPunctualityRating(int punctualityRating) { this.punctualityRating = punctualityRating; }
    public int getCommunicationRating() { return communicationRating; }
    public void setCommunicationRating(int communicationRating) { this.communicationRating = communicationRating; }
    public String getReviewComment() { return reviewComment; }
    public void setReviewComment(String reviewComment) { this.reviewComment = reviewComment; }
    public boolean isWouldHireAgain() { return wouldHireAgain; }
    public void setWouldHireAgain(boolean wouldHireAgain) { this.wouldHireAgain = wouldHireAgain; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
