package in.sp.main.controller;

import in.sp.main.entity.CastingCompany;
import in.sp.main.entity.SuperAdmin;
import in.sp.main.repository.CastingCompanyRepository;
import in.sp.main.repository.SubscriptionPaymentRepository;
import in.sp.main.service.NotificationService;
import in.sp.main.service.SessionAuthHelper;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

/**
 * Super Admin Verification and Approval module implementing all required verification fields.
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private static final Set<String> VALID_ACCOUNT_STATUS = Set.of("Pending", "Approved", "Rejected");
    private static final Set<String> VALID_VERIFICATION = Set.of("Verified", "Not verified");
    private static final Set<String> VALID_DOC_STATUS = Set.of("Pending", "Verified", "Rejected");
    private static final Set<String> VALID_SUSPENSION = Set.of("Active", "Suspended");

    private final CastingCompanyRepository companyRepo;
    private final SubscriptionPaymentRepository paymentRepo;
    private final SessionAuthHelper authHelper;
    private final NotificationService notificationService;

    public AdminController(CastingCompanyRepository companyRepo,
                           SubscriptionPaymentRepository paymentRepo,
                           SessionAuthHelper authHelper,
                           NotificationService notificationService) {
        this.companyRepo = companyRepo;
        this.paymentRepo = paymentRepo;
        this.authHelper = authHelper;
        this.notificationService = notificationService;
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getStats(HttpSession session) {
        SuperAdmin admin = authHelper.requireAdmin(session);
        List<CastingCompany> all = companyRepo.findAll();
        long pending = all.stream().filter(c -> "Pending".equalsIgnoreCase(c.getAccountStatus())).count();
        long approved = all.stream().filter(c -> "Approved".equalsIgnoreCase(c.getAccountStatus())).count();
        long rejected = all.stream().filter(c -> "Rejected".equalsIgnoreCase(c.getAccountStatus())).count();
        long paidActive = all.stream().filter(c -> c.isSubscriptionActive() && "Active".equalsIgnoreCase(c.getAccountSuspension())).count();
        long suspended = all.stream().filter(c -> "Suspended".equalsIgnoreCase(c.getAccountSuspension())).count();

        return ResponseEntity.ok(Map.of(
                "admin", authHelper.toSafeAdminMap(admin),
                "totalCompanies", all.size(),
                "pendingCount", pending,
                "approvedCount", approved,
                "rejectedCount", rejected,
                "paidActiveCount", paidActive,
                "suspendedCount", suspended
        ));
    }

    @GetMapping("/companies")
    public ResponseEntity<?> listCompanies(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String q,
            HttpSession session
    ) {
        authHelper.requireAdmin(session);
        List<CastingCompany> companies = companyRepo.findAllByOrderByCreatedAtDesc();
        String search = q == null ? "" : q.trim().toLowerCase(Locale.ROOT);
        List<Map<String, Object>> result = companies.stream()
                .filter(c -> status == null || status.isBlank() || "ALL".equalsIgnoreCase(status)
                        || ("SUSPENDED".equalsIgnoreCase(status) && "Suspended".equalsIgnoreCase(c.getAccountSuspension()))
                        || c.getAccountStatus().equalsIgnoreCase(status))
                .filter(c -> search.isEmpty()
                        || c.getCompanyName().toLowerCase(Locale.ROOT).contains(search)
                        || c.getCompanyEmail().toLowerCase(Locale.ROOT).contains(search)
                        || c.getCity().toLowerCase(Locale.ROOT).contains(search)
                        || c.getRepresentativeName().toLowerCase(Locale.ROOT).contains(search))
                .map(authHelper::toSafeCompanyMap)
                .toList();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/companies/{id}")
    public ResponseEntity<?> getCompanyDetail(@PathVariable Long id, HttpSession session) {
        authHelper.requireAdmin(session);
        CastingCompany company = companyRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));
        return ResponseEntity.ok(Map.of(
                "company", authHelper.toSafeCompanyMap(company),
                "payments", paymentRepo.findByCompanyIdOrderByPaidAtDesc(id)
        ));
    }

    @PutMapping("/companies/{id}/verify")
    public ResponseEntity<?> verifyCompany(@PathVariable Long id,
                                           @RequestBody Map<String, String> payload,
                                           HttpSession session) {
        SuperAdmin admin = authHelper.requireAdmin(session);
        CastingCompany company = companyRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));

        String accountStatus = payload.getOrDefault("accountStatus", company.getAccountStatus());
        String emailVerification = payload.getOrDefault("emailVerification", company.getEmailVerification());
        String mobileVerification = payload.getOrDefault("mobileVerification", company.getMobileVerification());
        String documentVerification = payload.getOrDefault("documentVerification", company.getDocumentVerification());
        String adminRemarks = payload.getOrDefault("adminRemarks", "");
        String verifiedBy = payload.getOrDefault("verifiedBy", admin.getFullName() + " (" + admin.getAdminCode() + ")");
        String verificationDateStr = payload.get("verificationDate");
        String accountSuspension = payload.getOrDefault("accountSuspension", company.getAccountSuspension());

        if (!VALID_ACCOUNT_STATUS.contains(accountStatus) ||
                !VALID_VERIFICATION.contains(emailVerification) ||
                !VALID_VERIFICATION.contains(mobileVerification) ||
                !VALID_DOC_STATUS.contains(documentVerification) ||
                !VALID_SUSPENSION.contains(accountSuspension)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid verification option value supplied."));
        }

        company.setAccountStatus(accountStatus);
        company.setEmailVerification(emailVerification);
        company.setMobileVerification(mobileVerification);
        company.setDocumentVerification(documentVerification);
        company.setAdminRemarks(adminRemarks);
        company.setVerifiedBy(verifiedBy.isBlank() ? admin.getFullName() + " (" + admin.getAdminCode() + ")" : verifiedBy.trim());
        company.setVerificationDate(verificationDateStr != null && !verificationDateStr.isBlank()
                ? LocalDate.parse(verificationDateStr)
                : LocalDate.now());
        company.setAccountSuspension(accountSuspension);
        company.setVerifiedBadge("Approved".equals(accountStatus) && "Active".equals(accountSuspension));

        CastingCompany saved = companyRepo.save(company);

        String notifyTitle;
        String notifyMsg;
        if ("Suspended".equals(accountSuspension)) {
            notifyTitle = "Account Suspended by Super Admin";
            notifyMsg = "Your account has been suspended. Remarks: " + (adminRemarks.isBlank() ? "Contact support." : adminRemarks);
        } else if ("Approved".equals(accountStatus)) {
            notifyTitle = "Company Approved — Choose a Subscription Pass";
            notifyMsg = "Congratulations! Your company passed Super Admin verification (" + saved.getVerifiedBy()
                    + "). Select one of the 3 Subscription Passes to unlock your Casting Dashboard.";
        } else if ("Rejected".equals(accountStatus)) {
            notifyTitle = "Verification Update — Rejected";
            notifyMsg = "Your verification request was rejected. Admin remarks: " + adminRemarks;
        } else {
            notifyTitle = "Verification Status Updated";
            notifyMsg = "Your verification record was updated to " + accountStatus + ".";
        }
        notificationService.notifyCompany(saved.getId(), saved.getCompanyEmail(), notifyTitle, notifyMsg);

        return ResponseEntity.ok(Map.of(
                "message", "Admin verification saved for " + saved.getCompanyName(),
                "company", authHelper.toSafeCompanyMap(saved)
        ));
    }
}
