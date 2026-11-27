package com.example.demo.service;

import com.example.demo.model.*;
import com.example.demo.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class SubscriptionService {

    private final SubscriptionPlanRepository planRepository;
    private final UserSubscriptionRepository subscriptionRepository;
    private final PlatformCommissionRepository commissionRepository;
    private final VendorNotificationRepository notificationRepository;

    public SubscriptionService(
            SubscriptionPlanRepository planRepository,
            UserSubscriptionRepository subscriptionRepository,
            PlatformCommissionRepository commissionRepository,
            VendorNotificationRepository notificationRepository
    ) {
        this.planRepository = planRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.commissionRepository = commissionRepository;
        this.notificationRepository = notificationRepository;
    }

    public List<Map<String, Object>> getPublicPlans(UserRole role) {
        List<SubscriptionPlan> plans = role == null
                ? planRepository.findByOrderByDashboardRoleAscMonthlyPriceAsc().stream()
                    .filter(p -> p.isActive() && p.isVisible()).toList()
                : planRepository.findByDashboardRoleAndActiveTrueAndVisibleTrueOrderByMonthlyPriceAsc(role);
        return plans.stream().map(this::toPlanMap).toList();
    }

    @Transactional
    public Map<String, Object> getMySubscriptionDashboard(UserAccount user) {
        UserSubscription active = ensureActiveOrDefaultFree(user);
        List<Map<String, Object>> available = getPublicPlans(user.getRole());
        List<Map<String, Object>> history = subscriptionRepository.findByUserOrderByIdDesc(user).stream()
                .map(this::toSubscriptionMap)
                .toList();

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("role", user.getRole().name());
        res.put("roleDisplayName", user.getRole().getDisplayName());
        res.put("activeSubscription", active == null ? null : toSubscriptionMap(active));
        res.put("availablePlans", available);
        res.put("history", history);
        return res;
    }

    @Transactional
    public Map<String, Object> subscribe(UserAccount user, Long planId, String billingCycleRaw, String paymentRefRaw) {
        SubscriptionPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Subscription plan not found."));
        if (plan.getDashboardRole() != user.getRole()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Selected plan does not match your dashboard role.");
        }
        if (!plan.isActive()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selected plan is currently inactive.");
        }

        String cycle = "ANNUAL".equalsIgnoreCase(billingCycleRaw) ? "ANNUAL" : "MONTHLY";
        BigDecimal baseAmount = "ANNUAL".equals(cycle) ? plan.getAnnualPrice() : plan.getMonthlyPrice();
        BigDecimal taxPct = plan.getTaxPercent() == null ? new BigDecimal("18.00") : plan.getTaxPercent();
        BigDecimal tax = baseAmount.multiply(taxPct).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        BigDecimal total = baseAmount.add(tax);
        int days = "ANNUAL".equals(cycle) ? 365 : plan.getDurationDays();

        subscriptionRepository.findFirstByUserAndStatusOrderByIdDesc(user, "ACTIVE")
                .ifPresent(prev -> {
                    prev.setStatus("CANCELLED");
                    subscriptionRepository.save(prev);
                });

        UserSubscription sub = new UserSubscription();
        sub.setUser(user);
        sub.setPlan(plan);
        sub.setBillingCycle(cycle);
        sub.setStatus("ACTIVE");
        sub.setStartDate(LocalDate.now());
        sub.setEndDate(LocalDate.now().plusDays(days));
        sub.setAutoRenew("AUTO".equalsIgnoreCase(plan.getRenewalRule()));
        sub.setAmountPaid(baseAmount);
        sub.setTaxAmount(tax);
        sub.setTotalPaid(total);
        sub.setPaymentReference(
                paymentRefRaw != null && !paymentRefRaw.isBlank()
                        ? paymentRefRaw.trim()
                        : (baseAmount.compareTo(BigDecimal.ZERO) == 0 ? "FREE_TIER" : "SIM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
        );
        sub.setInvoiceNumber("SUB-" + LocalDate.now().getYear() + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
        sub.setCreatedAt(LocalDateTime.now());

        UserSubscription saved = subscriptionRepository.save(sub);

        notificationRepository.save(new VendorNotification(
                user,
                "Subscription Activated (" + plan.getPlanName() + ")",
                "Your " + plan.getPlanName() + " (" + cycle + ") is active until " + saved.getEndDate()
                        + ". Invoice: " + saved.getInvoiceNumber(),
                "SUBSCRIPTION_ACTIVATED",
                saved.getId()
        ));

        return toSubscriptionMap(saved);
    }

    @Transactional
    public Map<String, Object> cancelActiveSubscription(UserAccount user) {
        UserSubscription active = subscriptionRepository.findFirstByUserAndStatusOrderByIdDesc(user, "ACTIVE")
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No active subscription found."));
        active.setStatus("CANCELLED");
        subscriptionRepository.save(active);

        notificationRepository.save(new VendorNotification(
                user,
                "Subscription Cancelled",
                "Your " + active.getPlan().getPlanName() + " subscription has been cancelled.",
                "SUBSCRIPTION_CANCELLED",
                active.getId()
        ));
        return Map.of("cancelled", true);
    }

    // ==================== SUPER ADMIN ====================

    public List<Map<String, Object>> adminListAllPlans() {
        return planRepository.findByOrderByDashboardRoleAscMonthlyPriceAsc().stream()
                .map(this::toPlanMap)
                .toList();
    }

    @Transactional
    public Map<String, Object> adminSavePlan(Long id, Map<String, Object> body) {
        SubscriptionPlan plan = id != null
                ? planRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Plan not found."))
                : new SubscriptionPlan();

        if (body.get("dashboardRole") != null) {
            plan.setDashboardRole(UserRole.valueOf(String.valueOf(body.get("dashboardRole"))));
        }
        if (body.get("tier") != null) plan.setTier(String.valueOf(body.get("tier")).toUpperCase());
        if (body.get("planName") != null) plan.setPlanName(String.valueOf(body.get("planName")));
        if (body.get("monthlyPrice") != null) plan.setMonthlyPrice(new BigDecimal(String.valueOf(body.get("monthlyPrice"))));
        if (body.get("annualPrice") != null) {
            plan.setAnnualPrice(new BigDecimal(String.valueOf(body.get("annualPrice"))));
        } else if (plan.getMonthlyPrice() != null && plan.getAnnualPrice() == null) {
            plan.setAnnualPrice(plan.getMonthlyPrice().multiply(BigDecimal.valueOf(12)).multiply(new BigDecimal("0.80")));
        }
        if (body.get("durationDays") != null) plan.setDurationDays(Integer.parseInt(String.valueOf(body.get("durationDays"))));
        if (body.get("usageLimit") != null) plan.setUsageLimit(Integer.parseInt(String.valueOf(body.get("usageLimit"))));
        if (body.get("teamAccountsLimit") != null) plan.setTeamAccountsLimit(Integer.parseInt(String.valueOf(body.get("teamAccountsLimit"))));
        if (body.get("featuresSummary") != null) plan.setFeaturesSummary(String.valueOf(body.get("featuresSummary")));
        if (body.get("renewalRule") != null) plan.setRenewalRule(String.valueOf(body.get("renewalRule")));
        if (body.get("visible") != null) plan.setVisible(Boolean.parseBoolean(String.valueOf(body.get("visible"))));
        if (body.get("active") != null) plan.setActive(Boolean.parseBoolean(String.valueOf(body.get("active"))));

        return toPlanMap(planRepository.save(plan));
    }

    @Transactional
    public Map<String, Object> adminTogglePlan(Long id) {
        SubscriptionPlan plan = planRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Plan not found."));
        plan.setActive(!plan.isActive());
        return toPlanMap(planRepository.save(plan));
    }

    public List<Map<String, Object>> adminListUserSubscriptions() {
        return subscriptionRepository.findAllByOrderByIdDesc().stream()
                .map(s -> {
                    Map<String, Object> m = new LinkedHashMap<>(toSubscriptionMap(s));
                    m.put("userName", s.getUser().getFullName());
                    m.put("userEmail", s.getUser().getEmail());
                    m.put("userRole", s.getUser().getRole().getDisplayName());
                    return m;
                })
                .toList();
    }

    public List<PlatformCommission> adminListCommissions() {
        return commissionRepository.findAll();
    }

    @Transactional
    public PlatformCommission adminUpdateCommission(Long id, BigDecimal percent, String description) {
        PlatformCommission c = commissionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Commission config not found."));
        if (percent != null) c.setCommissionPercent(percent);
        if (description != null) c.setDescription(description);
        c.setUpdatedAt(LocalDateTime.now());
        return commissionRepository.save(c);
    }

    public Map<String, Object> adminSubscriptionAnalytics() {
        List<UserSubscription> all = subscriptionRepository.findAll();
        long activeCount = all.stream().filter(s -> "ACTIVE".equals(s.getStatus())).count();
        long paidActive = all.stream().filter(s -> "ACTIVE".equals(s.getStatus()) && s.getTotalPaid().compareTo(BigDecimal.ZERO) > 0).count();
        BigDecimal totalRevenue = all.stream().map(UserSubscription::getTotalPaid).reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, BigDecimal> revenueByRole = new LinkedHashMap<>();
        for (UserSubscription s : all) {
            String key = s.getPlan().getDashboardRole().getDisplayName();
            revenueByRole.merge(key, s.getTotalPaid(), BigDecimal::add);
        }

        return Map.of(
                "totalSubscriptions", all.size(),
                "activeSubscriptions", activeCount,
                "paidActiveSubscriptions", paidActive,
                "totalRevenue", totalRevenue,
                "revenueByDashboard", revenueByRole
        );
    }

    // ==================== HELPERS ====================

    public UserSubscription ensureActiveOrDefaultFree(UserAccount user) {
        Optional<UserSubscription> existing = subscriptionRepository.findFirstByUserAndStatusOrderByIdDesc(user, "ACTIVE");
        if (existing.isPresent()) {
            return existing.get();
        }
        return planRepository.findByDashboardRoleAndTier(user.getRole(), "FREE")
                .map(freePlan -> {
                    UserSubscription sub = new UserSubscription();
                    sub.setUser(user);
                    sub.setPlan(freePlan);
                    sub.setBillingCycle("MONTHLY");
                    sub.setStatus("ACTIVE");
                    sub.setStartDate(LocalDate.now());
                    sub.setEndDate(LocalDate.now().plusDays(3650));
                    sub.setAmountPaid(BigDecimal.ZERO);
                    sub.setTaxAmount(BigDecimal.ZERO);
                    sub.setTotalPaid(BigDecimal.ZERO);
                    sub.setPaymentReference("FREE_DEFAULT");
                    sub.setInvoiceNumber("SUB-FREE-" + user.getId());
                    sub.setCreatedAt(LocalDateTime.now());
                    return subscriptionRepository.save(sub);
                })
                .orElse(null);
    }

    private Map<String, Object> toPlanMap(SubscriptionPlan p) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", p.getId());
        m.put("dashboardRole", p.getDashboardRole().name());
        m.put("dashboardName", p.getDashboardRole().getDisplayName());
        m.put("tier", p.getTier());
        m.put("planName", p.getPlanName());
        m.put("monthlyPrice", p.getMonthlyPrice());
        m.put("annualPrice", p.getAnnualPrice());
        m.put("durationDays", p.getDurationDays());
        m.put("usageLimit", p.getUsageLimit());
        m.put("teamAccountsLimit", p.getTeamAccountsLimit());
        m.put("features", p.getFeaturesSummary() == null ? List.of() : Arrays.asList(p.getFeaturesSummary().split("\\|")));
        m.put("featuresSummary", p.getFeaturesSummary());
        m.put("active", p.isActive());
        m.put("visible", p.isVisible());
        m.put("renewalRule", p.getRenewalRule());
        m.put("taxPercent", p.getTaxPercent());
        return m;
    }

    private Map<String, Object> toSubscriptionMap(UserSubscription s) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", s.getId());
        m.put("planId", s.getPlan().getId());
        m.put("planName", s.getPlan().getPlanName());
        m.put("tier", s.getPlan().getTier());
        m.put("billingCycle", s.getBillingCycle());
        m.put("status", s.getStatus());
        m.put("startDate", s.getStartDate());
        m.put("endDate", s.getEndDate());
        m.put("amountPaid", s.getAmountPaid());
        m.put("taxAmount", s.getTaxAmount());
        m.put("totalPaid", s.getTotalPaid());
        m.put("paymentReference", s.getPaymentReference());
        m.put("invoiceNumber", s.getInvoiceNumber());
        m.put("usageLimit", s.getPlan().getUsageLimit());
        m.put("teamAccountsLimit", s.getPlan().getTeamAccountsLimit());
        m.put("features", s.getPlan().getFeaturesSummary() == null ? List.of() : Arrays.asList(s.getPlan().getFeaturesSummary().split("\\|")));
        return m;
    }
}
