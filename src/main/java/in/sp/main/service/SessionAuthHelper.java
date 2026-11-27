package in.sp.main.service;

import in.sp.main.entity.CastingCompany;
import in.sp.main.entity.SuperAdmin;
import in.sp.main.repository.CastingCompanyRepository;
import in.sp.main.repository.SuperAdminRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Centralized session resolution, authorization checks, and safe DTO projection.
 */
@Component
public class SessionAuthHelper {

    public static final String SESSION_ROLE = "EW_ROLE";
    public static final String SESSION_USER_ID = "EW_USER_ID";
    public static final String ROLE_ADMIN = "SUPER_ADMIN";
    public static final String ROLE_COMPANY = "CASTING_COMPANY";

    private final CastingCompanyRepository companyRepo;
    private final SuperAdminRepository adminRepo;

    public SessionAuthHelper(CastingCompanyRepository companyRepo, SuperAdminRepository adminRepo) {
        this.companyRepo = companyRepo;
        this.adminRepo = adminRepo;
    }

    public SuperAdmin requireAdmin(HttpSession session) {
        Object role = session.getAttribute(SESSION_ROLE);
        Object id = session.getAttribute(SESSION_USER_ID);
        if (!ROLE_ADMIN.equals(role) || !(id instanceof Long adminId)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Super Admin authentication required");
        }
        return adminRepo.findById(adminId)
                .filter(SuperAdmin::isActive)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Admin account unavailable"));
    }

    public CastingCompany requireCompany(HttpSession session) {
        Object role = session.getAttribute(SESSION_ROLE);
        Object id = session.getAttribute(SESSION_USER_ID);
        if (!ROLE_COMPANY.equals(role) || !(id instanceof Long companyId)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Casting Company authentication required");
        }
        return companyRepo.findById(companyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Company account not found"));
    }

    public CastingCompany requireApprovedActiveCompany(HttpSession session) {
        CastingCompany company = requireCompany(session);
        if ("Suspended".equalsIgnoreCase(company.getAccountSuspension())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account is suspended by Super Admin");
        }
        if (!"Approved".equalsIgnoreCase(company.getAccountStatus())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Company is awaiting Super Admin approval");
        }
        return company;
    }

    public CastingCompany requirePaidDashboardCompany(HttpSession session) {
        CastingCompany company = requireApprovedActiveCompany(session);
        if (!company.isSubscriptionActive()) {
            throw new ResponseStatusException(HttpStatus.PAYMENT_REQUIRED, "Active subscription pass required");
        }
        return company;
    }

    public String computeRedirectTarget(CastingCompany c) {
        if ("Suspended".equalsIgnoreCase(c.getAccountSuspension()) || !"Approved".equalsIgnoreCase(c.getAccountStatus())) {
            return "/company-status.html";
        }
        if (!c.isSubscriptionActive()) {
            return "/passes.html";
        }
        return "/dashboard.html";
    }

    public Map<String, Object> toSafeCompanyMap(CastingCompany c) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", c.getId());
        m.put("representativeName", c.getRepresentativeName());
        m.put("organizationType", c.getOrganizationType());
        m.put("talentSought", c.getTalentSought());
        m.put("projectTypesInterest", c.getProjectTypesInterest());
        m.put("companyName", c.getCompanyName());
        m.put("companyLogoPath", c.getCompanyLogoPath());
        m.put("companyEmail", c.getCompanyEmail());
        m.put("contactNumber", c.getContactNumber());
        m.put("website", c.getWebsite());
        m.put("companyAddress", c.getCompanyAddress());
        m.put("city", c.getCity());
        m.put("state", c.getState());
        m.put("country", c.getCountry());
        m.put("yearEstablished", c.getYearEstablished());
        m.put("companyDescription", c.getCompanyDescription());
        m.put("gstNumber", c.getGstNumber());
        m.put("gstCertificatePath", c.getGstCertificatePath());
        m.put("companyRegistrationCertificatePath", c.getCompanyRegistrationCertificatePath());
        m.put("panNumber", c.getPanNumber());
        m.put("panDocumentPath", c.getPanDocumentPath());
        m.put("businessAddressProofPath", c.getBusinessAddressProofPath());
        m.put("authorizationLetterPath", c.getAuthorizationLetterPath());

        // 5. Admin verification fields
        m.put("accountStatus", c.getAccountStatus());
        m.put("emailVerification", c.getEmailVerification());
        m.put("mobileVerification", c.getMobileVerification());
        m.put("documentVerification", c.getDocumentVerification());
        m.put("adminRemarks", c.getAdminRemarks());
        m.put("verifiedBy", c.getVerifiedBy());
        m.put("verificationDate", c.getVerificationDate());
        m.put("accountSuspension", c.getAccountSuspension());

        // Subscription & Recruiter Profile metrics
        m.put("verifiedBadge", c.isVerifiedBadge());
        m.put("followersCount", c.getFollowersCount());
        m.put("activePassCode", c.getActivePassCode());
        m.put("activePassName", c.getActivePassName());
        m.put("subscriptionActive", c.isSubscriptionActive());
        m.put("subscriptionExpiry", c.getSubscriptionExpiry());
        m.put("contactViewsTotal", c.getContactViewsTotal());
        m.put("contactViewsUsed", c.getContactViewsUsed());
        m.put("contactViewsRemaining", Math.max(0, c.getContactViewsTotal() - c.getContactViewsUsed()));
        m.put("portfolioViewsTotal", c.getPortfolioViewsTotal());
        m.put("portfolioViewsUsed", c.getPortfolioViewsUsed());
        m.put("auditionInvitesTotal", c.getAuditionInvitesTotal());
        m.put("auditionInvitesUsed", c.getAuditionInvitesUsed());
        m.put("projectPromotionsRemaining", c.getProjectPromotionsRemaining());
        m.put("showreelVideosJson", c.getShowreelVideosJson());
        m.put("portfolioPhotosJson", c.getPortfolioPhotosJson());
        m.put("createdAt", c.getCreatedAt());
        m.put("redirectTarget", computeRedirectTarget(c));
        return m;
    }

    public Map<String, Object> toSafeAdminMap(SuperAdmin a) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", a.getId());
        m.put("adminCode", a.getAdminCode());
        m.put("fullName", a.getFullName());
        m.put("email", a.getEmail());
        m.put("roleTitle", a.getRoleTitle());
        m.put("displayIdentity", a.getFullName() + " (" + a.getAdminCode() + ")");
        return m;
    }
}
