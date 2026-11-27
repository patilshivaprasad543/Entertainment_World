package com.example.demo.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "subscription_plans")
public class SubscriptionPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32, columnDefinition = "VARCHAR(32)")
    private UserRole dashboardRole;

    @Column(nullable = false, length = 24)
    private String tier; // FREE, PREMIUM, PRO

    @Column(nullable = false, length = 120)
    private String planName;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal monthlyPrice;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal annualPrice;

    @Column(nullable = false)
    private Integer durationDays = 30;

    @Column(nullable = false)
    private Integer usageLimit = 1;

    @Column(nullable = false)
    private Integer teamAccountsLimit = 1;

    @Column(length = 1500)
    private String featuresSummary;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private boolean visible = true;

    @Column(length = 24)
    private String renewalRule = "MANUAL";

    @Column(precision = 5, scale = 2)
    private BigDecimal taxPercent = new BigDecimal("18.00");

    public SubscriptionPlan() {
    }

    public Long getId() { return id; }
    public UserRole getDashboardRole() { return dashboardRole; }
    public void setDashboardRole(UserRole dashboardRole) { this.dashboardRole = dashboardRole; }
    public String getTier() { return tier; }
    public void setTier(String tier) { this.tier = tier; }
    public String getPlanName() { return planName; }
    public void setPlanName(String planName) { this.planName = planName; }
    public BigDecimal getMonthlyPrice() { return monthlyPrice; }
    public void setMonthlyPrice(BigDecimal monthlyPrice) { this.monthlyPrice = monthlyPrice; }
    public BigDecimal getAnnualPrice() { return annualPrice; }
    public void setAnnualPrice(BigDecimal annualPrice) { this.annualPrice = annualPrice; }
    public Integer getDurationDays() { return durationDays; }
    public void setDurationDays(Integer durationDays) { this.durationDays = durationDays; }
    public Integer getUsageLimit() { return usageLimit; }
    public void setUsageLimit(Integer usageLimit) { this.usageLimit = usageLimit; }
    public Integer getTeamAccountsLimit() { return teamAccountsLimit; }
    public void setTeamAccountsLimit(Integer teamAccountsLimit) { this.teamAccountsLimit = teamAccountsLimit; }
    public String getFeaturesSummary() { return featuresSummary; }
    public void setFeaturesSummary(String featuresSummary) { this.featuresSummary = featuresSummary; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public boolean isVisible() { return visible; }
    public void setVisible(boolean visible) { this.visible = visible; }
    public String getRenewalRule() { return renewalRule; }
    public void setRenewalRule(String renewalRule) { this.renewalRule = renewalRule; }
    public BigDecimal getTaxPercent() { return taxPercent; }
    public void setTaxPercent(BigDecimal taxPercent) { this.taxPercent = taxPercent; }
}
