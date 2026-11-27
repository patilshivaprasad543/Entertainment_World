package com.talentplatform.brand.model;

import com.talentplatform.brand.enums.PlanType;
import jakarta.persistence.*;

@Entity
@Table(name = "brand_profiles")
public class BrandProfile {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(unique = true) private String accountEmail;
    private String brandName;
    private String legalEntityName;
    private String logoUrl;
    private String companyType;
    private Integer establishedYear;
    private String industryCategory;
    @Column(length = 2000) private String aboutCompany;
    private String websiteUrl;
    private String contactDesignation;
    private String instagramHandle;
    private String gstin;
    private String billingState;
    @Enumerated(EnumType.STRING) private PlanType planType = PlanType.FREE;
    private Integer teamSeatsLimit = 1;
    private Integer activeCampaignLimit = 1;

    public Long getId() { return id; }
    public String getAccountEmail() { return accountEmail; }
    public void setAccountEmail(String accountEmail) { this.accountEmail = accountEmail; }
    public String getBrandName() { return brandName; }
    public void setBrandName(String brandName) { this.brandName = brandName; }
    public String getLegalEntityName() { return legalEntityName; }
    public void setLegalEntityName(String legalEntityName) { this.legalEntityName = legalEntityName; }
    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }
    public String getCompanyType() { return companyType; }
    public void setCompanyType(String companyType) { this.companyType = companyType; }
    public Integer getEstablishedYear() { return establishedYear; }
    public void setEstablishedYear(Integer establishedYear) { this.establishedYear = establishedYear; }
    public String getIndustryCategory() { return industryCategory; }
    public void setIndustryCategory(String industryCategory) { this.industryCategory = industryCategory; }
    public String getAboutCompany() { return aboutCompany; }
    public void setAboutCompany(String aboutCompany) { this.aboutCompany = aboutCompany; }
    public String getWebsiteUrl() { return websiteUrl; }
    public void setWebsiteUrl(String websiteUrl) { this.websiteUrl = websiteUrl; }
    public String getContactDesignation() { return contactDesignation; }
    public void setContactDesignation(String contactDesignation) { this.contactDesignation = contactDesignation; }
    public String getInstagramHandle() { return instagramHandle; }
    public void setInstagramHandle(String instagramHandle) { this.instagramHandle = instagramHandle; }
    public String getGstin() { return gstin; }
    public void setGstin(String gstin) { this.gstin = gstin; }
    public String getBillingState() { return billingState; }
    public void setBillingState(String billingState) { this.billingState = billingState; }
    public PlanType getPlanType() { return planType; }
    public void setPlanType(PlanType planType) { this.planType = planType; this.activeCampaignLimit = planType.getActiveCampaignLimit(); this.teamSeatsLimit = planType.getTeamSeatsLimit(); }
    public Integer getTeamSeatsLimit() { return teamSeatsLimit; }
    public Integer getActiveCampaignLimit() { return activeCampaignLimit; }
}
