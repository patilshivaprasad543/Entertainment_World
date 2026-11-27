package in.sp.main.controller;

import in.sp.main.entity.ArtistProfile;
import in.sp.main.entity.CastingCompany;
import in.sp.main.entity.SuperAdmin;
import in.sp.main.repository.ArtistProfileRepository;
import in.sp.main.repository.CastingCompanyRepository;
import in.sp.main.repository.SubscriptionPassRepository;
import in.sp.main.repository.SuperAdminRepository;
import in.sp.main.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

/**
 * Handles public landing data, Casting Company registration with KYC document uploads,
 * and unified session login/logout for both Casting Companies and Super Admins.
 */
@RestController
@RequestMapping("/api")
public class AuthAndPublicController {

    private final CastingCompanyRepository companyRepo;
    private final SuperAdminRepository adminRepo;
    private final SubscriptionPassRepository passRepo;
    private final ArtistProfileRepository artistRepo;
    private final FileStorageService fileStorage;
    private final PasswordService passwordService;
    private final NotificationService notificationService;
    private final DataSeedService seedService;
    private final SessionAuthHelper authHelper;

    public AuthAndPublicController(CastingCompanyRepository companyRepo,
                                   SuperAdminRepository adminRepo,
                                   SubscriptionPassRepository passRepo,
                                   ArtistProfileRepository artistRepo,
                                   FileStorageService fileStorage,
                                   PasswordService passwordService,
                                   NotificationService notificationService,
                                   DataSeedService seedService,
                                   SessionAuthHelper authHelper) {
        this.companyRepo = companyRepo;
        this.adminRepo = adminRepo;
        this.passRepo = passRepo;
        this.artistRepo = artistRepo;
        this.fileStorage = fileStorage;
        this.passwordService = passwordService;
        this.notificationService = notificationService;
        this.seedService = seedService;
        this.authHelper = authHelper;
    }

    @GetMapping("/public/passes")
    public ResponseEntity<?> getPublicPasses() {
        return ResponseEntity.ok(passRepo.findAllByOrderByDisplayOrderAsc());
    }

    @GetMapping("/public/landing-data")
    public ResponseEntity<?> getLandingData() {
        List<ArtistProfile> artists = artistRepo.findAll();
        Map<String, Long> categoryCounts = new LinkedHashMap<>();
        for (ArtistProfile a : artists) {
            categoryCounts.merge(a.getCategory(), 1L, Long::sum);
        }
        return ResponseEntity.ok(Map.of(
                "passes", passRepo.findAllByOrderByDisplayOrderAsc(),
                "categoryCounts", categoryCounts,
                "featuredArtists", artists.stream().limit(8).map(a -> Map.of(
                        "id", a.getId(),
                        "fullName", a.getFullName(),
                        "category", a.getCategory(),
                        "city", a.getCity(),
                        "avatarUrl", a.getAvatarUrl(),
                        "hasVideo", a.isHasVideo(),
                        "videoTitle", a.getVideoTitle() == null ? "" : a.getVideoTitle(),
                        "videoUrl", a.getVideoUrl() == null ? "" : a.getVideoUrl()
                )).toList()
        ));
    }

    @PostMapping(value = "/auth/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> registerCastingCompany(
            @RequestParam String representativeName,
            @RequestParam String companyName,
            @RequestParam String companyEmail,
            @RequestParam String contactNumber,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            @RequestParam(required = false) String website,
            @RequestParam String companyAddress,
            @RequestParam String city,
            @RequestParam String state,
            @RequestParam String country,
            @RequestParam(required = false) Integer yearEstablished,
            @RequestParam String companyDescription,
            @RequestParam(required = false) String gstNumber,
            @RequestParam(required = false) String panNumber,
            @RequestParam(required = false) String organizationType,
            @RequestParam(required = false) String talentSought,
            @RequestParam(required = false) String projectTypesInterest,
            @RequestParam(defaultValue = "false") boolean termsAccepted,
            @RequestParam("companyLogo") MultipartFile companyLogo,
            @RequestParam(value = "gstCertificate", required = false) MultipartFile gstCertificate,
            @RequestParam(value = "companyRegistrationCertificate", required = false) MultipartFile companyRegistrationCertificate,
            @RequestParam(value = "panDocument", required = false) MultipartFile panDocument,
            @RequestParam(value = "businessAddressProof", required = false) MultipartFile businessAddressProof,
            @RequestParam(value = "authorizationLetter", required = false) MultipartFile authorizationLetter,
            HttpSession session
    ) {
        if (!termsAccepted) {
            return ResponseEntity.badRequest().body(Map.of("error", "Please accept the Terms & Conditions and Privacy Policy."));
        }
        if (password == null || password.length() < 8) {
            return ResponseEntity.badRequest().body(Map.of("error", "Password must contain at least 8 characters."));
        }
        if (!password.equals(confirmPassword)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Password and Confirm Password do not match."));
        }
        if (companyLogo == null || companyLogo.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Company logo is required."));
        }
        if (companyRepo.existsByCompanyEmailIgnoreCase(companyEmail.trim()) ||
                adminRepo.findByEmailIgnoreCase(companyEmail.trim()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "An account with this business email already exists."));
        }

        CastingCompany c = new CastingCompany();
        c.setRepresentativeName(representativeName.trim());
        c.setPasswordHash(passwordService.hash(password));
        c.setOrganizationType(trimOrNull(organizationType));
        c.setTalentSought(trimOrNull(talentSought));
        c.setProjectTypesInterest(trimOrNull(projectTypesInterest));
        c.setCompanyName(companyName.trim());
        c.setCompanyLogoPath(fileStorage.store(companyLogo, "logos"));
        c.setCompanyEmail(companyEmail.trim().toLowerCase(Locale.ROOT));
        c.setContactNumber(contactNumber.trim());
        c.setWebsite(trimOrNull(website));
        c.setCompanyAddress(companyAddress.trim());
        c.setCity(city.trim());
        c.setState(state.trim());
        c.setCountry(country.trim());
        c.setYearEstablished(yearEstablished);
        c.setCompanyDescription(companyDescription.trim());
        c.setGstNumber(trimOrNull(gstNumber));
        c.setPanNumber(trimOrNull(panNumber));
        c.setTermsAccepted(true);

        if (gstCertificate != null && !gstCertificate.isEmpty()) {
            c.setGstCertificatePath(fileStorage.store(gstCertificate, "docs"));
        }
        if (companyRegistrationCertificate != null && !companyRegistrationCertificate.isEmpty()) {
            c.setCompanyRegistrationCertificatePath(fileStorage.store(companyRegistrationCertificate, "docs"));
        }
        if (panDocument != null && !panDocument.isEmpty()) {
            c.setPanDocumentPath(fileStorage.store(panDocument, "docs"));
        }
        if (businessAddressProof != null && !businessAddressProof.isEmpty()) {
            c.setBusinessAddressProofPath(fileStorage.store(businessAddressProof, "docs"));
        }
        if (authorizationLetter != null && !authorizationLetter.isEmpty()) {
            c.setAuthorizationLetterPath(fileStorage.store(authorizationLetter, "docs"));
        }

        // Strict initial verification state awaiting Super Admin
        c.setAccountStatus("Pending");
        c.setEmailVerification("Not verified");
        c.setMobileVerification("Not verified");
        c.setDocumentVerification("Pending");
        c.setAccountSuspension("Active");
        c.setVerifiedBadge(false);
        c.setSubscriptionActive(false);
        c.setShowreelVideosJson("[{\"title\":\"Company Introduction\",\"thumb\":\"" + c.getCompanyLogoPath() + "\",\"url\":\"/assets/video/entertainment-world-intro.mp4\"}]");
        c.setPortfolioPhotosJson("[\"" + c.getCompanyLogoPath() + "\"]");

        CastingCompany saved = companyRepo.save(c);
        seedService.createDefaultFolders(saved.getId());
        notificationService.notifyCompany(saved.getId(), saved.getCompanyEmail(),
                "Registration Submitted to Super Admin",
                "Welcome to Entertainment World! Your casting company request has been forwarded to Super Admin for verification.");

        // Sign the newly registered company into the Status Portal session
        session.setAttribute(SessionAuthHelper.SESSION_ROLE, SessionAuthHelper.ROLE_COMPANY);
        session.setAttribute(SessionAuthHelper.SESSION_USER_ID, saved.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message", "Registration submitted! Your request has reached the Super Admin for verification.",
                "company", authHelper.toSafeCompanyMap(saved),
                "redirectTarget", "/company-status.html"
        ));
    }

    @PostMapping("/auth/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body, HttpSession session) {
        String email = body.getOrDefault("email", "").trim();
        String password = body.getOrDefault("password", "");
        String portal = body.getOrDefault("portal", "AUTO"); // AUTO, ADMIN, COMPANY

        if (email.isBlank() || password.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email and password are required."));
        }

        if (!"COMPANY".equalsIgnoreCase(portal)) {
            Optional<SuperAdmin> adminOpt = adminRepo.findByEmailIgnoreCase(email);
            if (adminOpt.isPresent()) {
                SuperAdmin admin = adminOpt.get();
                if (!admin.isActive() || !passwordService.matches(password, admin.getPasswordHash())) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(Map.of("error", "Invalid Super Admin credentials."));
                }
                session.setAttribute(SessionAuthHelper.SESSION_ROLE, SessionAuthHelper.ROLE_ADMIN);
                session.setAttribute(SessionAuthHelper.SESSION_USER_ID, admin.getId());
                return ResponseEntity.ok(Map.of(
                        "role", SessionAuthHelper.ROLE_ADMIN,
                        "admin", authHelper.toSafeAdminMap(admin),
                        "redirectTarget", "/admin/dashboard.html"
                ));
            }
            if ("ADMIN".equalsIgnoreCase(portal)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid Super Admin credentials."));
            }
        }

        Optional<CastingCompany> companyOpt = companyRepo.findByCompanyEmailIgnoreCase(email);
        if (companyOpt.isEmpty() || !passwordService.matches(password, companyOpt.get().getPasswordHash())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid business email or password."));
        }

        CastingCompany company = companyOpt.get();
        session.setAttribute(SessionAuthHelper.SESSION_ROLE, SessionAuthHelper.ROLE_COMPANY);
        session.setAttribute(SessionAuthHelper.SESSION_USER_ID, company.getId());

        return ResponseEntity.ok(Map.of(
                "role", SessionAuthHelper.ROLE_COMPANY,
                "company", authHelper.toSafeCompanyMap(company),
                "redirectTarget", authHelper.computeRedirectTarget(company)
        ));
    }

    @PostMapping("/auth/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok(Map.of("message", "Signed out cleanly"));
    }

    @GetMapping("/auth/me")
    public ResponseEntity<?> currentSession(HttpSession session) {
        Object role = session.getAttribute(SessionAuthHelper.SESSION_ROLE);
        Object id = session.getAttribute(SessionAuthHelper.SESSION_USER_ID);
        if (SessionAuthHelper.ROLE_ADMIN.equals(role) && id instanceof Long adminId) {
            return adminRepo.findById(adminId)
                    .<ResponseEntity<?>>map(a -> ResponseEntity.ok(Map.of(
                            "authenticated", true,
                            "role", SessionAuthHelper.ROLE_ADMIN,
                            "admin", authHelper.toSafeAdminMap(a),
                            "redirectTarget", "/admin/dashboard.html"
                    )))
                    .orElse(ResponseEntity.ok(Map.of("authenticated", false)));
        }
        if (SessionAuthHelper.ROLE_COMPANY.equals(role) && id instanceof Long companyId) {
            return companyRepo.findById(companyId)
                    .<ResponseEntity<?>>map(c -> ResponseEntity.ok(Map.of(
                            "authenticated", true,
                            "role", SessionAuthHelper.ROLE_COMPANY,
                            "company", authHelper.toSafeCompanyMap(c),
                            "redirectTarget", authHelper.computeRedirectTarget(c)
                    )))
                    .orElse(ResponseEntity.ok(Map.of("authenticated", false)));
        }
        return ResponseEntity.ok(Map.of("authenticated", false));
    }

    private String trimOrNull(String val) {
        return (val == null || val.isBlank()) ? null : val.trim();
    }
}
