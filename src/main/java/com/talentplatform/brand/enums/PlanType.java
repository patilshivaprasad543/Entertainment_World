package com.talentplatform.brand.enums;

public enum PlanType {
    FREE(0, 1, 1, 0),
    PREMIUM_1499(1499, 5, 3, 5),
    PRO_3999(3999, 20, 10, Integer.MAX_VALUE);

    private final int monthlyPriceInr;
    private final int activeCampaignLimit;
    private final int teamSeatsLimit;
    private final int aiMatchesPerCampaign;

    PlanType(int monthlyPriceInr, int activeCampaignLimit, int teamSeatsLimit, int aiMatchesPerCampaign) {
        this.monthlyPriceInr = monthlyPriceInr;
        this.activeCampaignLimit = activeCampaignLimit;
        this.teamSeatsLimit = teamSeatsLimit;
        this.aiMatchesPerCampaign = aiMatchesPerCampaign;
    }

    public int getMonthlyPriceInr() { return monthlyPriceInr; }
    public int getActiveCampaignLimit() { return activeCampaignLimit; }
    public int getTeamSeatsLimit() { return teamSeatsLimit; }
    public int getAiMatchesPerCampaign() { return aiMatchesPerCampaign; }
    public boolean hasAdvancedSearch() { return this != FREE; }
    public boolean hasDetailedAnalytics() { return this != FREE; }
    public boolean canExportReports() { return this != FREE; }
    public boolean canBuildTeams() { return this == PRO_3999; }
}
