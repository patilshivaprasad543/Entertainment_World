package com.talentplatform.brand.service;

import com.talentplatform.brand.enums.PlanType;
import com.talentplatform.brand.model.BrandProfile;
import com.talentplatform.brand.repository.BrandProfileRepository;
import com.talentplatform.brand.repository.CampaignRepository;
import com.talentplatform.brand.repository.HireRequestRepository;
import com.talentplatform.brand.repository.TeamMemberRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class SubscriptionGuardService {
    private final BrandProfileRepository brands;
    private final CampaignRepository campaigns;
    private final TeamMemberRepository members;
    public SubscriptionGuardService(BrandProfileRepository brands, CampaignRepository campaigns,
            TeamMemberRepository members, HireRequestRepository requests) {
        this.brands = brands; this.campaigns = campaigns; this.members = members;
    }
    public BrandProfile profile(Long brandId) {
        return brands.findById(brandId).orElseGet(() -> {
            BrandProfile profile = new BrandProfile(); profile.setBrandName("Your Brand"); return brands.save(profile);
        });
    }
    public PlanType plan(Long brandId) { return profile(brandId).getPlanType(); }
    public void requireAdvancedSearch(Long brandId) {
        if (!plan(brandId).hasAdvancedSearch()) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Advanced search is available on Premium and Pro.");
    }
    public void requireTeamBuilder(Long brandId) {
        if (!plan(brandId).canBuildTeams()) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Team Builder is unlocked on the Pro plan.");
    }
    public void requireCampaignSlot(Long brandId) {
        BrandProfile profile = profile(brandId);
        if (campaigns.countByBrandIdAndStatus(brandId, "ACTIVE") >= profile.getActiveCampaignLimit())
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Your plan's active campaign limit has been reached.");
    }
    public void requireTeamSeat(Long brandId) {
        BrandProfile profile = profile(brandId);
        if (members.countByBrandId(brandId) + 1 >= profile.getTeamSeatsLimit())
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Your plan's team seat limit has been reached.");
    }
    public int aiMatchLimit(Long brandId) { return plan(brandId).getAiMatchesPerCampaign(); }
}
