package com.talentplatform.brand.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "talent_profiles")
public class TalentProfile {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    private String fullName;
    private String category;
    private String specialty;
    private String gender;
    private String contractTypes;
    private String city;
    private String languages;
    private BigDecimal baseRate = BigDecimal.ZERO;
    private BigDecimal engagementRate = BigDecimal.ZERO;
    private Double averageRating = 0.0;
    private Integer totalReviews = 0;
    private boolean verified;
    private String photoUrl;

    public Long getId() { return id; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getSpecialty() { return specialty; }
    public void setSpecialty(String specialty) { this.specialty = specialty; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getContractTypes() { return contractTypes; }
    public void setContractTypes(String contractTypes) { this.contractTypes = contractTypes; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getLanguages() { return languages; }
    public void setLanguages(String languages) { this.languages = languages; }
    public BigDecimal getBaseRate() { return baseRate; }
    public void setBaseRate(BigDecimal baseRate) { this.baseRate = baseRate; }
    public BigDecimal getEngagementRate() { return engagementRate; }
    public void setEngagementRate(BigDecimal engagementRate) { this.engagementRate = engagementRate; }
    public Double getAverageRating() { return averageRating; }
    public void setAverageRating(Double averageRating) { this.averageRating = averageRating; }
    public Integer getTotalReviews() { return totalReviews; }
    public void setTotalReviews(Integer totalReviews) { this.totalReviews = totalReviews; }
    public boolean isVerified() { return verified; }
    public void setVerified(boolean verified) { this.verified = verified; }
    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
}
