package in.sp.main.controller;

import in.sp.main.entity.CastingCompany;
import in.sp.main.entity.SubscriptionPass;
import in.sp.main.entity.SubscriptionPayment;
import in.sp.main.repository.CastingCompanyRepository;
import in.sp.main.repository.SubscriptionPassRepository;
import in.sp.main.repository.SubscriptionPaymentRepository;
import in.sp.main.service.NotificationService;
import in.sp.main.service.SessionAuthHelper;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

/**
 * Handles the 3 Subscription Passes pricing, checkout, and quota activation
 * strictly after Super Admin approval.
 */
@RestController
@RequestMapping("/api/subscription")
public class SubscriptionController {

    private final SubscriptionPassRepository passRepo;
    private final SubscriptionPaymentRepository paymentRepo;
    private final CastingCompanyRepository companyRepo;
    private final SessionAuthHelper authHelper;
    private final NotificationService notificationService;

    public SubscriptionController(SubscriptionPassRepository passRepo,
                                  SubscriptionPaymentRepository paymentRepo,
                                  CastingCompanyRepository companyRepo,
                                  SessionAuthHelper authHelper,
                                  NotificationService notificationService) {
        this.passRepo = passRepo;
        this.paymentRepo = paymentRepo;
        this.companyRepo = companyRepo;
        this.authHelper = authHelper;
        this.notificationService = notificationService;
    }

    @GetMapping("/passes")
    public ResponseEntity<?> getPassesForCompany(HttpSession session) {
        CastingCompany company = authHelper.requireApprovedActiveCompany(session);
        return ResponseEntity.ok(Map.of(
                "company", authHelper.toSafeCompanyMap(company),
                "passes", passRepo.findAllByOrderByDisplayOrderAsc(),
                "payments", paymentRepo.findByCompanyIdOrderByPaidAtDesc(company.getId())
        ));
    }

    @PostMapping("/checkout")
    public ResponseEntity<?> checkout(@RequestBody Map<String, Object> body, HttpSession session) {
        CastingCompany company = authHelper.requireApprovedActiveCompany(session);

        String passCode = String.valueOf(body.getOrDefault("passCode", ""));
        String paymentMethod = String.valueOf(body.getOrDefault("paymentMethod", "UPI"));
        int months = Integer.parseInt(String.valueOf(body.getOrDefault("durationMonths", 1)));
        if (months != 1 && months != 3 && months != 6 && months != 12) {
            months = 1;
        }

        SubscriptionPass pass = passRepo.findByPassCode(passCode)
                .orElseThrow(() -> new IllegalArgumentException("Unknown subscription pass: " + passCode));

        // Scale price proportionally for 1 / 3 / 6 / 12 month selections with multi-month savings
        BigDecimal monthlyBase = BigDecimal.valueOf(pass.getBasePriceInr())
                .divide(BigDecimal.valueOf(Math.max(1, pass.getDefaultMonths())), 2, RoundingMode.HALF_UP);
        BigDecimal discountFactor = switch (months) {
            case 6 -> new BigDecimal("0.90");
            case 12 -> new BigDecimal("0.80");
            default -> BigDecimal.ONE;
        };
        BigDecimal baseAmount = (months == pass.getDefaultMonths())
                ? BigDecimal.valueOf(pass.getBasePriceInr()).setScale(2, RoundingMode.HALF_UP)
                : monthlyBase.multiply(BigDecimal.valueOf(months)).multiply(discountFactor).setScale(2, RoundingMode.HALF_UP);
        BigDecimal gstAmount = baseAmount.multiply(new BigDecimal("0.18")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalPaid = baseAmount.add(gstAmount);

        LocalDate validFrom = LocalDate.now();
        LocalDate validUntil = validFrom.plusMonths(months);
        String txnRef = "EW-TXN-" + validFrom.toString().replace("-", "") + "-"
                + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        SubscriptionPayment payment = new SubscriptionPayment();
        payment.setCompanyId(company.getId());
        payment.setPassCode(pass.getPassCode());
        payment.setPassName(pass.getPassName());
        payment.setDurationMonths(months);
        payment.setBaseAmountInr(baseAmount);
        payment.setGstAmountInr(gstAmount);
        payment.setTotalPaidInr(totalPaid);
        payment.setPaymentMethod(paymentMethod);
        payment.setTransactionRef(txnRef);
        payment.setPaymentStatus("PAID");
        payment.setValidFrom(validFrom);
        payment.setValidUntil(validUntil);
        paymentRepo.save(payment);

        // Activate Pass & Allocate Entitlements
        int multiplier = Math.max(1, months / Math.max(1, pass.getDefaultMonths()));
        company.setActivePassCode(pass.getPassCode());
        company.setActivePassName(pass.getPassName());
        company.setSubscriptionActive(true);
        company.setSubscriptionExpiry(validUntil);
        company.setContactViewsTotal(company.getContactViewsTotal() + pass.getContactViewsLimit() * multiplier);
        if (pass.getPortfolioViewsLimit() < 0) {
            company.setPortfolioViewsTotal(-1);
        } else if (company.getPortfolioViewsTotal() >= 0) {
            company.setPortfolioViewsTotal(company.getPortfolioViewsTotal() + pass.getPortfolioViewsLimit() * multiplier);
        }
        company.setAuditionInvitesTotal(company.getAuditionInvitesTotal() + pass.getInviteAuditionLimit() * multiplier);
        company.setProjectPromotionsRemaining(company.getProjectPromotionsRemaining() + pass.getProjectPromotionQuota() * multiplier);
        CastingCompany saved = companyRepo.save(company);

        notificationService.notifyCompany(saved.getId(), saved.getCompanyEmail(),
                pass.getPassName() + " Activated",
                "Payment " + txnRef + " (Rs. " + totalPaid + ") succeeded. Your Recruiter Dashboard is now unlocked until " + validUntil + ".");

        return ResponseEntity.ok(Map.of(
                "message", "Pass activated! Redirecting to your Entertainment World Recruiter Dashboard.",
                "payment", payment,
                "company", authHelper.toSafeCompanyMap(saved),
                "redirectTarget", "/dashboard.html"
        ));
    }
}
