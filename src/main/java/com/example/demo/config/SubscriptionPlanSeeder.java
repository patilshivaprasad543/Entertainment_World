package com.example.demo.config;

import com.example.demo.model.PlatformCommission;
import com.example.demo.model.SubscriptionPlan;
import com.example.demo.model.UserRole;
import com.example.demo.repository.PlatformCommissionRepository;
import com.example.demo.repository.SubscriptionPlanRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component
public class SubscriptionPlanSeeder implements CommandLineRunner {

    private final SubscriptionPlanRepository planRepository;
    private final PlatformCommissionRepository commissionRepository;

    public SubscriptionPlanSeeder(
            SubscriptionPlanRepository planRepository,
            PlatformCommissionRepository commissionRepository
    ) {
        this.planRepository = planRepository;
        this.commissionRepository = commissionRepository;
    }

    @Override
    public void run(String... args) {
        seedPlans();
        seedCommissions();
    }

    private void seedPlans() {
        // Hide any legacy INFLUENCER plans so only the 6 main modules are active
        planRepository.findByOrderByDashboardRoleAscMonthlyPriceAsc().stream()
                .filter(p -> p.getDashboardRole() == UserRole.INFLUENCER)
                .forEach(p -> {
                    p.setActive(false);
                    p.setVisible(false);
                    planRepository.save(p);
                });

        List<PlanSeed> seeds = List.of(
                // 2. Professional (ACTOR)
                new PlanSeed(UserRole.ACTOR, "FREE", "Professional Free", 0, 5, 1,
                        "Create Profile|5 Portfolio Uploads|Audition Applications|Availability Calendar"),
                new PlanSeed(UserRole.ACTOR, "PREMIUM", "Professional Premium", 199, 20, 1,
                        "20 Portfolio Uploads|Advanced Job & Event Filters|AI Portfolio Builder|Portfolio Analytics|AI Audition Practice (5/mo)|Enhanced Digital Passport"),
                new PlanSeed(UserRole.ACTOR, "PRO", "Professional Pro", 399, 50, 1,
                        "50 Portfolio Uploads|All Premium Features|AI Audition Practice (20/mo)|Priority Support|500 Wishlist Items"),

                // 3. Vendors
                new PlanSeed(UserRole.VENDOR, "FREE", "Vendors Free", 0, 1, 1,
                        "Vendor Profile|1 Listing|5 Photos/Listing|Availability Calendar|Booking Enquiries"),
                new PlanSeed(UserRole.VENDOR, "PREMIUM", "Vendors Premium", 999, 3, 3,
                        "3 Listings|20 Photos/Listing|Advanced Booking Management|360 Venue Tour|Multiple Location Management|Detailed Analytics|3 Team Accounts"),
                new PlanSeed(UserRole.VENDOR, "PRO", "Vendors Pro", 2499, 10, 10,
                        "10 Listings|50 Photos/Listing|Expanded Multi-Location Management|Report Export|10 Team Accounts"),

                // 4. Casting Agency & Production House
                new PlanSeed(UserRole.CASTING, "FREE", "Casting Free", 0, 1, 1,
                        "Company Profile|1 Active Audition Post|Basic Talent Search|20 Saved Talent"),
                new PlanSeed(UserRole.CASTING, "PREMIUM", "Casting Premium", 999, 5, 3,
                        "5 Active Audition Posts|Advanced Talent Search|AI Talent Matching (20/mo)|Virtual Auditions|Crew Builder|3 Team Accounts"),
                new PlanSeed(UserRole.CASTING, "PRO", "Casting Pro", 2499, 20, 10,
                        "20 Active Audition Posts|AI Talent Matching (100/mo)|Emergency Talent Search|10 Team Accounts|Detailed Recruitment Analytics"),

                // 5. Brands & Advertisement Agencies
                new PlanSeed(UserRole.BRAND, "FREE", "Brands Free", 0, 1, 1,
                        "Brand Profile|1 Active Campaign|Basic Creator Search|20 Saved Creators"),
                new PlanSeed(UserRole.BRAND, "PREMIUM", "Brands Premium", 1499, 5, 3,
                        "5 Active Campaigns|Advanced Creator Filters|AI Creator Matching (20/mo)|Detailed Campaign Analytics|Report Export|3 Team Accounts"),
                new PlanSeed(UserRole.BRAND, "PRO", "Brands Pro", 3999, 20, 10,
                        "20 Active Campaigns|AI Creator Matching (100/mo)|Creator Team Builder|10 Team Accounts"),

                // 6. Event Management
                new PlanSeed(UserRole.EVENT_MANAGEMENT, "FREE", "Event Management Free", 0, 1, 1,
                        "Organizer Profile|1 Active Event|Basic Professional & Vendor Search|Booking Requests"),
                new PlanSeed(UserRole.EVENT_MANAGEMENT, "PREMIUM", "Event Management Premium", 799, 5, 3,
                        "5 Active Events|Event Team Builder|AI Professional Matching (20/mo)|Event Budget Planner|Multiple Event Management|3 Team Accounts"),
                new PlanSeed(UserRole.EVENT_MANAGEMENT, "PRO", "Event Management Pro", 1999, 20, 10,
                        "20 Active Events|AI Professional Matching (100/mo)|Detailed Event Reports|10 Team Accounts")
        );

        for (PlanSeed seed : seeds) {
            if (planRepository.findByDashboardRoleAndTier(seed.role, seed.tier).isEmpty()) {
                SubscriptionPlan p = new SubscriptionPlan();
                p.setDashboardRole(seed.role);
                p.setTier(seed.tier);
                p.setPlanName(seed.name);
                BigDecimal monthly = BigDecimal.valueOf(seed.monthlyPrice);
                BigDecimal annual = monthly.multiply(BigDecimal.valueOf(12))
                        .multiply(new BigDecimal("0.80"))
                        .setScale(2, RoundingMode.HALF_UP);
                p.setMonthlyPrice(monthly);
                p.setAnnualPrice(annual);
                p.setDurationDays(30);
                p.setUsageLimit(seed.usageLimit);
                p.setTeamAccountsLimit(seed.teamLimit);
                p.setFeaturesSummary(seed.features);
                p.setActive(true);
                p.setVisible(true);
                planRepository.save(p);
            }
        }
    }

    private void seedCommissions() {
        List<PlatformCommission> defaults = List.of(
                new PlatformCommission(UserRole.INFLUENCER, new BigDecimal("20.00"), "20-25% platform commission on completed brand collaborations"),
                new PlatformCommission(UserRole.EVENT_MANAGEMENT, new BigDecimal("10.00"), "Commission on confirmed event bookings"),
                new PlatformCommission(UserRole.VENDOR, new BigDecimal("10.00"), "Commission on confirmed venue/equipment bookings")
        );
        for (PlatformCommission c : defaults) {
            if (commissionRepository.findByModuleRole(c.getModuleRole()).isEmpty()) {
                commissionRepository.save(c);
            }
        }
    }

    private record PlanSeed(UserRole role, String tier, String name, int monthlyPrice, int usageLimit, int teamLimit, String features) {
    }
}
