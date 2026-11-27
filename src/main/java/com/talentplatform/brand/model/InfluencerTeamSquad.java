package com.talentplatform.brand.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity @Table(name = "influencer_team_squads")
public class InfluencerTeamSquad {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    private Long brandId;
    private Long campaignId;
    private String squadName;
    @Column(length = 2000) private String talentIds;
    private BigDecimal combinedBudget = BigDecimal.ZERO;
    public Long getId() { return id; }
    public Long getBrandId() { return brandId; }
    public void setBrandId(Long brandId) { this.brandId = brandId; }
    public Long getCampaignId() { return campaignId; }
    public void setCampaignId(Long campaignId) { this.campaignId = campaignId; }
    public String getSquadName() { return squadName; }
    public void setSquadName(String squadName) { this.squadName = squadName; }
    public String getTalentIds() { return talentIds; }
    public void setTalentIds(String talentIds) { this.talentIds = talentIds; }
    public BigDecimal getCombinedBudget() { return combinedBudget; }
    public void setCombinedBudget(BigDecimal combinedBudget) { this.combinedBudget = combinedBudget; }
}
