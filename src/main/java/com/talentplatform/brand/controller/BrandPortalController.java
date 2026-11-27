package com.talentplatform.brand.controller;

import com.talentplatform.brand.enums.HireRequestStatus;
import com.talentplatform.brand.enums.PlanType;
import com.talentplatform.brand.model.*;
import com.talentplatform.brand.repository.*;
import com.talentplatform.brand.service.SubscriptionGuardService;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@RestController
@RequestMapping("/api/v1/brand")
@CrossOrigin(origins = "http://localhost:5173")
public class BrandPortalController {
    private final BrandProfileRepository brands;
    private final TalentProfileRepository talents;
    private final CampaignRepository campaigns;
    private final WishlistItemRepository wishlists;
    private final HireRequestRepository requests;
    private final TeamMemberRepository members;
    private final InfluencerTeamSquadRepository squads;
    private final TalentReviewRepository reviews;
    private final NotificationRepository notifications;
    private final ChatMessageRepository messages;
    private final PaymentInvoiceRepository invoices;
    private final SubscriptionGuardService guard;

    public BrandPortalController(BrandProfileRepository brands, TalentProfileRepository talents,
            CampaignRepository campaigns, WishlistItemRepository wishlists, HireRequestRepository requests,
            TeamMemberRepository members, InfluencerTeamSquadRepository squads, TalentReviewRepository reviews,
            NotificationRepository notifications, ChatMessageRepository messages, PaymentInvoiceRepository invoices,
            SubscriptionGuardService guard) {
        this.brands = brands; this.talents = talents; this.campaigns = campaigns; this.wishlists = wishlists;
        this.requests = requests; this.members = members; this.squads = squads; this.reviews = reviews;
        this.notifications = notifications; this.messages = messages; this.invoices = invoices; this.guard = guard;
    }

    @PostMapping("/auth/sign-in")
    public Map<String, Object> signIn(@RequestBody Map<String, String> credentials) {
        String email = credentials.getOrDefault("email", "").trim().toLowerCase(Locale.ROOT);
        if (email.isBlank() || credentials.getOrDefault("password", "").isBlank())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email and password are required.");
        BrandProfile profile = brands.findByAccountEmailIgnoreCase(email).orElseGet(() -> {
            BrandProfile unassigned = brands.findByAccountEmailIsNull().stream().findFirst().orElseGet(BrandProfile::new);
            unassigned.setAccountEmail(email);
            if (unassigned.getBrandName() == null || unassigned.getBrandName().isBlank()) unassigned.setBrandName("Your Brand");
            return brands.save(unassigned);
        });
        return Map.of("brandId", profile.getId(), "brandName", profile.getBrandName(),
            "accountEmail", profile.getAccountEmail(), "planType", profile.getPlanType().name(), "demoMode", true);
    }

    @GetMapping("/{brandId}/profile")
    public BrandProfile getProfile(@PathVariable Long brandId) { return guard.profile(brandId); }

    @PutMapping("/{brandId}/profile")
    public BrandProfile saveProfile(@PathVariable Long brandId, @RequestBody BrandProfile incoming) {
        if (incoming.getBrandName() == null || incoming.getBrandName().isBlank()
                || incoming.getCompanyType() == null || incoming.getCompanyType().isBlank()
                || incoming.getIndustryCategory() == null || incoming.getIndustryCategory().isBlank())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Company name, company type, and industry are required.");
        BrandProfile profile = guard.profile(brandId);
        profile.setBrandName(incoming.getBrandName()); profile.setLegalEntityName(incoming.getLegalEntityName());
        profile.setLogoUrl(incoming.getLogoUrl()); profile.setCompanyType(incoming.getCompanyType());
        profile.setEstablishedYear(incoming.getEstablishedYear()); profile.setIndustryCategory(incoming.getIndustryCategory());
        profile.setAboutCompany(incoming.getAboutCompany()); profile.setWebsiteUrl(incoming.getWebsiteUrl());
        profile.setContactDesignation(incoming.getContactDesignation());
        profile.setInstagramHandle(incoming.getInstagramHandle());
        profile.setBillingState(incoming.getBillingState());
        return brands.save(profile);
    }

    @GetMapping("/plans")
    public List<Map<String, Object>> plans() {
        return Arrays.stream(PlanType.values()).map(p -> Map.<String, Object>of(
            "code", p.name(), "monthlyPriceInr", p.getMonthlyPriceInr(), "activeCampaigns", p.getActiveCampaignLimit(),
            "teamSeats", p.getTeamSeatsLimit(), "advancedSearch", p.hasAdvancedSearch(), "detailedAnalytics", p.hasDetailedAnalytics(),
            "teamBuilder", p.canBuildTeams(), "reportExport", p.canExportReports(), "aiMatches", p.getAiMatchesPerCampaign())).toList();
    }

    @PutMapping("/{brandId}/subscription")
    public BrandProfile changePlan(@PathVariable Long brandId, @RequestBody Map<String, String> request) {
        PlanType plan;
        try { plan = PlanType.valueOf(request.get("planType")); }
        catch (Exception ex) { throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown subscription plan."); }
        BrandProfile profile = guard.profile(brandId); profile.setPlanType(plan); return brands.save(profile);
    }

    @GetMapping("/{brandId}/talents")
    public List<TalentProfile> searchTalents(@PathVariable Long brandId,
            @RequestParam(required = false) String category, @RequestParam(required = false) String gender,
            @RequestParam(required = false) String specialty,
            @RequestParam(required = false) String contractType, @RequestParam(required = false) String city,
            @RequestParam(required = false) String language, @RequestParam(required = false) BigDecimal minBudget,
            @RequestParam(required = false) BigDecimal maxBudget, @RequestParam(required = false) Boolean verified,
            @RequestParam(required = false) String q) {
        if (city != null || language != null || minBudget != null || maxBudget != null || verified != null)
            guard.requireAdvancedSearch(brandId);
        Specification<TalentProfile> spec = (root, query, cb) -> cb.conjunction();
        spec = spec.and(contains("category", category)).and(contains("specialty", specialty)).and(contains("gender", gender)).and(contains("contractTypes", contractType))
            .and(contains("city", city)).and(contains("languages", language)).and(contains("fullName", q));
        if (minBudget != null) spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("baseRate"), minBudget));
        if (maxBudget != null) spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("baseRate"), maxBudget));
        if (verified != null) spec = spec.and((root, query, cb) -> cb.equal(root.get("verified"), verified));
        return talents.findAll(spec);
    }

    @GetMapping("/{brandId}/ai-matches")
    public List<TalentProfile> aiMatches(@PathVariable Long brandId) {
        int limit = guard.aiMatchLimit(brandId);
        if (limit == 0) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "AI matching is available on Premium and Pro.");
        List<TalentProfile> ranked = talents.findAll(org.springframework.data.domain.Sort.by(
            org.springframework.data.domain.Sort.Direction.DESC, "averageRating"));
        return limit == Integer.MAX_VALUE ? ranked : ranked.stream().limit(limit).toList();
    }

    private Specification<TalentProfile> contains(String field, String value) {
        if (value == null || value.isBlank()) return (root, query, cb) -> cb.conjunction();
        return (root, query, cb) -> cb.like(cb.lower(root.get(field)), "%" + value.toLowerCase(Locale.ROOT) + "%");
    }

    @GetMapping("/{brandId}/campaigns")
    public List<Campaign> campaigns(@PathVariable Long brandId) { return campaigns.findByBrandId(brandId); }

    @PostMapping("/{brandId}/campaigns")
    public Campaign createCampaign(@PathVariable Long brandId, @RequestBody Campaign campaign) {
        guard.requireCampaignSlot(brandId); campaign.setBrandId(brandId); return campaigns.save(campaign);
    }

    @PutMapping("/{brandId}/campaigns/{campaignId}")
    public Campaign updateCampaign(@PathVariable Long brandId, @PathVariable Long campaignId, @RequestBody Campaign incoming) {
        Campaign campaign = campaigns.findById(campaignId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project post not found."));
        if (!Objects.equals(campaign.getBrandId(), brandId)) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        campaign.setTitle(incoming.getTitle());
        campaign.setShootType(incoming.getShootType());
        campaign.setShootCity(incoming.getShootCity());
        campaign.setBudgetPerTalent(incoming.getBudgetPerTalent());
        campaign.setDeliverables(incoming.getDeliverables());
        campaign.setUsageRightsDuration(incoming.getUsageRightsDuration());
        campaign.setProductShippingRequired(incoming.isProductShippingRequired());
        campaign.setBrief(incoming.getBrief());
        return campaigns.save(campaign);
    }
    @GetMapping("/{brandId}/wishlist")
    public List<WishlistItem> wishlist(@PathVariable Long brandId) { return wishlists.findByBrandId(brandId); }

    @PostMapping("/{brandId}/wishlist")
    public WishlistItem addWishlist(@PathVariable Long brandId, @RequestBody WishlistItem item) {
        if (!talents.existsById(item.getTalentId())) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Talent not found.");
        String folder = item.getFolderName() == null || item.getFolderName().isBlank() ? "My shortlist" : item.getFolderName();
        if (wishlists.existsByBrandIdAndTalentIdAndFolderName(brandId, item.getTalentId(), folder))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Talent is already in this shortlist.");
        item.setBrandId(brandId); item.setFolderName(folder); return wishlists.save(item);
    }

    @DeleteMapping("/{brandId}/wishlist/{itemId}")
    public void removeWishlist(@PathVariable Long brandId, @PathVariable Long itemId) {
        WishlistItem item = wishlists.findById(itemId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!Objects.equals(item.getBrandId(), brandId)) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        wishlists.delete(item);
    }

    @GetMapping("/{brandId}/requests")
    public List<HireRequest> requests(@PathVariable Long brandId) { return requests.findByBrandId(brandId); }

    @PostMapping("/{brandId}/requests")
    public HireRequest sendRequest(@PathVariable Long brandId, @RequestBody HireRequest request) {
        if (!talents.existsById(request.getTalentId())) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Talent not found.");
        request.setBrandId(brandId); request.setStatus(HireRequestStatus.PENDING_RESPONSE); return requests.save(request);
    }

    @PatchMapping("/{brandId}/requests/{requestId}/{action}")
    public HireRequest updateRequest(@PathVariable Long brandId, @PathVariable Long requestId, @PathVariable String action) {
        HireRequest request = requests.findById(requestId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!Objects.equals(request.getBrandId(), brandId)) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        HireRequestStatus status = switch (action.toLowerCase(Locale.ROOT)) {
            case "accept" -> HireRequestStatus.ACCEPTED; case "decline" -> HireRequestStatus.DECLINED;
            case "escrow" -> HireRequestStatus.HIRED_IN_ESCROW; case "complete", "approve" -> HireRequestStatus.COMPLETED;
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown request action.");
        };
        request.setStatus(status); return requests.save(request);
    }

    @PutMapping("/{brandId}/requests/{requestId}/draft")
    public HireRequest attachDraft(@PathVariable Long brandId, @PathVariable Long requestId, @RequestBody Map<String, String> body) {
        HireRequest request = requests.findById(requestId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!Objects.equals(request.getBrandId(), brandId)) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        if (request.getStatus() != HireRequestStatus.HIRED_IN_ESCROW) throw new ResponseStatusException(HttpStatus.CONFLICT, "Draft review is available after escrow is locked.");
        request.setDraftUrl(body.get("draftUrl")); return requests.save(request);
    }

    @GetMapping("/{brandId}/team-members")
    public List<TeamMember> teamMembers(@PathVariable Long brandId) { return members.findByBrandId(brandId); }

    @PostMapping("/{brandId}/team-members")
    public TeamMember addTeamMember(@PathVariable Long brandId, @RequestBody TeamMember member) {
        guard.requireTeamSeat(brandId); member.setBrandId(brandId); return members.save(member);
    }

    @GetMapping("/{brandId}/squads")
    public List<InfluencerTeamSquad> squads(@PathVariable Long brandId) { guard.requireTeamBuilder(brandId); return squads.findByBrandId(brandId); }

    @PostMapping("/{brandId}/squads")
    public InfluencerTeamSquad createSquad(@PathVariable Long brandId, @RequestBody InfluencerTeamSquad squad) {
        guard.requireTeamBuilder(brandId);
        BigDecimal combined = BigDecimal.ZERO;
        if (squad.getTalentIds() != null && !squad.getTalentIds().isBlank()) {
            for (String rawId : squad.getTalentIds().split(",")) {
                try { combined = combined.add(talents.findById(Long.valueOf(rawId.trim())).orElseThrow().getBaseRate()); }
                catch (Exception ex) { throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Squad contains an unknown talent id."); }
            }
        }
        squad.setCombinedBudget(combined); squad.setBrandId(brandId); return squads.save(squad);
    }

    @PostMapping("/{brandId}/reviews")
    public TalentReview addReview(@PathVariable Long brandId, @RequestBody TalentReview review) {
        HireRequest request = requests.findById(review.getHireRequestId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Hire request not found."));
        if (!Objects.equals(request.getBrandId(), brandId) || request.getStatus() != HireRequestStatus.COMPLETED)
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only completed hires can be reviewed by the hiring brand.");
        if (reviews.existsByHireRequestId(request.getId())) throw new ResponseStatusException(HttpStatus.CONFLICT, "This hire already has a review.");
        validateRating(review.getOverallRating()); validateRating(review.getQualityRating()); validateRating(review.getPunctualityRating()); validateRating(review.getCommunicationRating());
        review.setBrandId(brandId); review.setTalentId(request.getTalentId());
        TalentProfile talent = talents.findById(request.getTalentId()).orElseThrow();
        int oldCount = talent.getTotalReviews() == null ? 0 : talent.getTotalReviews();
        double oldAverage = talent.getAverageRating() == null ? 0 : talent.getAverageRating();
        talent.setAverageRating(((oldAverage * oldCount) + review.getOverallRating()) / (oldCount + 1));
        talent.setTotalReviews(oldCount + 1); talents.save(talent); return reviews.save(review);
    }

    private void validateRating(int rating) {
        if (rating < 1 || rating > 5) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ratings must be between 1 and 5.");
    }

    @GetMapping("/{brandId}/reviews")
    public List<TalentReview> reviews(@PathVariable Long brandId) { return reviews.findByBrandId(brandId); }

    @PatchMapping("/{brandId}/notifications/{notificationId}/read")
    public Notification markNotificationRead(@PathVariable Long brandId, @PathVariable Long notificationId) {
        Notification notification = notifications.findById(notificationId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found."));
        if (!Objects.equals(notification.getBrandId(), brandId)) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        notification.setRead(true);
        return notifications.save(notification);
    }
    @GetMapping("/{brandId}/notifications")
    public List<Notification> notifications(@PathVariable Long brandId) { return notifications.findByBrandIdOrderByCreatedAtDesc(brandId); }

    @GetMapping("/{brandId}/chat/{talentId}")
    public List<ChatMessage> chat(@PathVariable Long brandId, @PathVariable Long talentId) { return messages.findByBrandIdAndTalentIdOrderBySentAtAsc(brandId, talentId); }

    @PostMapping("/{brandId}/chat/{talentId}")
    public ChatMessage sendMessage(@PathVariable Long brandId, @PathVariable Long talentId, @RequestBody ChatMessage message) {
        message.setBrandId(brandId); message.setTalentId(talentId); message.setSenderType("BRAND"); return messages.save(message);
    }

    @PatchMapping("/{brandId}/invoices/{invoiceId}/paid")
    public PaymentInvoice recordInvoicePayment(@PathVariable Long brandId, @PathVariable Long invoiceId) {
        PaymentInvoice invoice = invoices.findById(invoiceId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invoice not found."));
        if (!Objects.equals(invoice.getBrandId(), brandId)) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        invoice.setStatus("PAID");
        return invoices.save(invoice);
    }
    @GetMapping("/{brandId}/invoices")
    public List<PaymentInvoice> invoices(@PathVariable Long brandId) { return invoices.findByBrandIdOrderByCreatedAtDesc(brandId); }

    @PostMapping("/{brandId}/invoices")
    public PaymentInvoice createInvoice(@PathVariable Long brandId, @RequestBody Map<String, BigDecimal> body) {
        BigDecimal subtotal = Optional.ofNullable(body.get("subtotal")).orElse(BigDecimal.ZERO);
        if (subtotal.signum() < 0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Subtotal cannot be negative.");
        PaymentInvoice invoice = new PaymentInvoice(); invoice.setBrandId(brandId); invoice.setInvoiceNumber("INV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT));
        invoice.setSubtotal(subtotal); invoice.setGstAmount(subtotal.multiply(new BigDecimal("0.18")).setScale(2, RoundingMode.HALF_UP));
        invoice.setTdsAmount(subtotal.multiply(new BigDecimal("0.10")).setScale(2, RoundingMode.HALF_UP));
        invoice.setTotalAmount(subtotal.add(invoice.getGstAmount()).subtract(invoice.getTdsAmount()));
        return invoices.save(invoice);
    }

    @GetMapping("/{brandId}/overview")
    public Map<String, Object> overview(@PathVariable Long brandId) {
        return Map.of("activeCampaigns", campaigns.countByBrandIdAndStatus(brandId, "ACTIVE"),
            "campaignLimit", guard.profile(brandId).getActiveCampaignLimit(),
            "pendingRequests", requests.countByBrandIdAndStatus(brandId, HireRequestStatus.PENDING_RESPONSE),
            "wishlistedTalent", wishlists.findByBrandId(brandId).size(), "walletBalanceInr", 0,
            "planType", guard.plan(brandId).name());
    }
}
