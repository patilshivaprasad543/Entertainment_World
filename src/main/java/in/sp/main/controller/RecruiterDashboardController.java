package in.sp.main.controller;

import in.sp.main.entity.*;
import in.sp.main.repository.*;
import in.sp.main.service.FileStorageService;
import in.sp.main.service.NotificationService;
import in.sp.main.service.SessionAuthHelper;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implements every feature shown in the Recruiter Dashboard walkthrough video:
 * Verified Profile + Showreels, Post Project + Online Audition Script, Project History + Promote/Social Share,
 * View Applications (ALL/Shortlisted/Invited/Suggested), Smart Search Artists + Bulk Invite/Tag/Excel Download,
 * My Artists Custom Folders, Recommended Platinum Artists, Get Package, and Transaction History.
 */
@RestController
@RequestMapping("/api/dashboard")
public class RecruiterDashboardController {

    private final CastingCompanyRepository companyRepo;
    private final CastingProjectRepository projectRepo;
    private final ProjectApplicationRepository appRepo;
    private final ArtistProfileRepository artistRepo;
    private final ArtistFolderRepository folderRepo;
    private final ArtistFolderItemRepository folderItemRepo;
    private final ContactPortfolioHistoryRepository historyRepo;
    private final SubscriptionPassRepository passRepo;
    private final SubscriptionPaymentRepository paymentRepo;
    private final CompanyNotificationRepository notificationRepo;
    private final SearchHistoryEntryRepository searchHistoryRepo;
    private final FileStorageService fileStorage;
    private final NotificationService notificationService;
    private final SessionAuthHelper authHelper;

    public RecruiterDashboardController(CastingCompanyRepository companyRepo,
                                      CastingProjectRepository projectRepo,
                                      ProjectApplicationRepository appRepo,
                                      ArtistProfileRepository artistRepo,
                                      ArtistFolderRepository folderRepo,
                                      ArtistFolderItemRepository folderItemRepo,
                                      ContactPortfolioHistoryRepository historyRepo,
                                      SubscriptionPassRepository passRepo,
                                      SubscriptionPaymentRepository paymentRepo,
                                      CompanyNotificationRepository notificationRepo,
                                      SearchHistoryEntryRepository searchHistoryRepo,
                                      FileStorageService fileStorage,
                                      NotificationService notificationService,
                                      SessionAuthHelper authHelper) {
        this.companyRepo = companyRepo;
        this.projectRepo = projectRepo;
        this.appRepo = appRepo;
        this.artistRepo = artistRepo;
        this.folderRepo = folderRepo;
        this.folderItemRepo = folderItemRepo;
        this.historyRepo = historyRepo;
        this.passRepo = passRepo;
        this.paymentRepo = paymentRepo;
        this.notificationRepo = notificationRepo;
        this.searchHistoryRepo = searchHistoryRepo;
        this.fileStorage = fileStorage;
        this.notificationService = notificationService;
        this.authHelper = authHelper;
    }

    @GetMapping("/overview")
    public ResponseEntity<?> getOverview(HttpSession session) {
        CastingCompany company = authHelper.requirePaidDashboardCompany(session);
        List<CastingProject> projects = projectRepo.findByCompanyIdOrderByPostedDateDescIdDesc(company.getId());
        long activeProjects = projectRepo.countByCompanyIdAndStatus(company.getId(), "ACTIVE");
        long profilesViewed = historyRepo.countByCompanyIdAndViewType(company.getId(), "PORTFOLIO_VIEW")
                + historyRepo.countByCompanyIdAndViewType(company.getId(), "CONTACT_VIEW");
        List<CompanyNotification> notifications = notificationRepo.findByCompanyIdOrderByCreatedAtDesc(company.getId());
        long unreadCount = notificationRepo.countByCompanyIdAndReadFlagFalse(company.getId());

        List<Map<String, Object>> projectSummaries = projects.stream().map(p -> toProjectSummary(p, company)).toList();

        return ResponseEntity.ok(Map.of(
                "company", authHelper.toSafeCompanyMap(company),
                "activeProjectsCount", activeProjects,
                "profilesViewedCount", profilesViewed,
                "unreadNotifications", unreadCount,
                "notifications", notifications.stream().limit(12).toList(),
                "recentProjects", projectSummaries.stream().limit(5).toList()
        ));
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, String> body, HttpSession session) {
        CastingCompany company = authHelper.requirePaidDashboardCompany(session);
        if (body.containsKey("representativeName")) company.setRepresentativeName(body.get("representativeName").trim());
        if (body.containsKey("companyDescription")) company.setCompanyDescription(body.get("companyDescription").trim());
        if (body.containsKey("website")) company.setWebsite(body.get("website").trim());
        if (body.containsKey("city")) company.setCity(body.get("city").trim());
        if (body.containsKey("contactNumber")) company.setContactNumber(body.get("contactNumber").trim());
        if (body.containsKey("talentSought")) company.setTalentSought(body.get("talentSought").trim());
        if (body.containsKey("showreelVideosJson")) company.setShowreelVideosJson(body.get("showreelVideosJson"));
        if (body.containsKey("portfolioPhotosJson")) company.setPortfolioPhotosJson(body.get("portfolioPhotosJson"));
        CastingCompany saved = companyRepo.save(company);
        return ResponseEntity.ok(Map.of("company", authHelper.toSafeCompanyMap(saved)));
    }

    @PostMapping(value = "/profile/media", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadProfilePhoto(@RequestParam("file") MultipartFile file, HttpSession session) {
        CastingCompany company = authHelper.requirePaidDashboardCompany(session);
        String url = fileStorage.store(file, "portfolio");
        return ResponseEntity.ok(Map.of("url", url));
    }

    @GetMapping("/projects")
    public ResponseEntity<?> listProjects(@RequestParam(required = false, defaultValue = "ALL") String filter,
                                          HttpSession session) {
        CastingCompany company = authHelper.requirePaidDashboardCompany(session);
        List<CastingProject> projects = projectRepo.findByCompanyIdOrderByPostedDateDescIdDesc(company.getId());
        List<Map<String, Object>> filtered = projects.stream()
                .filter(p -> "ALL".equalsIgnoreCase(filter)
                        || ("PROMOTED".equalsIgnoreCase(filter) && p.isPromoted())
                        || p.getStatus().equalsIgnoreCase(filter))
                .map(p -> toProjectSummary(p, company))
                .toList();
        return ResponseEntity.ok(filtered);
    }

    @PostMapping(value = "/projects", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> postProject(
            // 1. Basic project information
            @RequestParam String projectTitle,
            @RequestParam String projectType,
            @RequestParam String productionHouseName,
            @RequestParam String castingCompanyName,
            @RequestParam String projectDetails,
            @RequestParam String projectLanguage,
            @RequestParam String projectStage,
            // 2. Talent requirements
            @RequestParam String targetCategory,
            @RequestParam(defaultValue = "Any") String targetGender,
            @RequestParam(defaultValue = "18") int ageMin,
            @RequestParam(defaultValue = "35") int ageMax,
            @RequestParam(required = false) String heightRequirement,
            @RequestParam(required = false) String preferredAppearance,
            @RequestParam(defaultValue = "Both") String experienceLevel,
            @RequestParam(defaultValue = "1") int artistsRequiredCount,
            @RequestParam(required = false) String roleDescription,
            @RequestParam(required = false) String characterName,
            // 3. Project location and schedule
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String auditionLocation,
            @RequestParam(required = false) String auditionDate,
            @RequestParam(defaultValue = "Online") String auditionType,
            @RequestParam(required = false) String shootingStartDate,
            @RequestParam(required = false) String shootingEndDate,
            @RequestParam(required = false) String shootingDuration,
            @RequestParam(required = false) String availabilityRequirements,
            @RequestParam(defaultValue = "Full-Time") String durationType,
            // 4. Payment details
            @RequestParam(defaultValue = "Paid") String paymentType,
            @RequestParam(required = false) String budget,
            @RequestParam(defaultValue = "Per day") String paymentBasis,
            @RequestParam(defaultValue = "No") String accommodationProvided,
            @RequestParam(defaultValue = "No") String foodProvided,
            @RequestParam(defaultValue = "No") String travelExpensesCovered,
            // 5. Audition details
            @RequestParam(required = false) String auditionProcess,
            @RequestParam(required = false) String auditionInstructions,
            @RequestParam(defaultValue = "Yes") String selfIntroVideoRequired,
            @RequestParam(defaultValue = "Yes") String portfolioRequired,
            @RequestParam(required = false) String auditionScript,
            @RequestParam(required = false) String lastDateToApply,
            @RequestParam(defaultValue = "true") boolean onlineAuditionEnabled,
            // 6. Application requirements
            @RequestParam(required = false) String requiredPhotographs,
            @RequestParam(required = false) String introVideoRequirement,
            @RequestParam(required = false) String actingPortfolioRequirement,
            @RequestParam(required = false) String previousWorkLinksReq,
            @RequestParam(required = false) String applicantContactReq,
            @RequestParam(required = false) String additionalDocumentsReq,
            // 7. Contact information
            @RequestParam(required = false) String castingDirectorName,
            @RequestParam(required = false) String contactEmail,
            @RequestParam(required = false) String contactNumber,
            @RequestParam(required = false) String whatsappNumber,
            @RequestParam(required = false) String websiteOrSocialLinks,
            // 8. Media and attachments
            @RequestParam(value = "projectPoster", required = false) MultipartFile projectPoster,
            @RequestParam(value = "referenceImages", required = false) MultipartFile[] referenceImages,
            @RequestParam(value = "characterLookReferences", required = false) MultipartFile[] characterLookReferences,
            @RequestParam(value = "auditionScriptPdf", required = false) MultipartFile auditionScriptPdf,
            @RequestParam(value = "projectPromotionalImage", required = false) MultipartFile projectPromotionalImage,
            @RequestParam(required = false) String tags,
            HttpSession session
    ) {
        CastingCompany company = authHelper.requirePaidDashboardCompany(session);
        if (projectTitle.isBlank() || projectDetails.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Project title and project description are required."));
        }

        CastingProject p = new CastingProject();
        p.setCompanyId(company.getId());
        p.setProjectTitle(projectTitle.trim());
        p.setProjectType(projectType.trim());
        p.setProductionHouseName(productionHouseName.trim());
        p.setCastingCompanyName(castingCompanyName.isBlank() ? company.getCompanyName() : castingCompanyName.trim());
        p.setProjectDetails(projectDetails.trim());
        p.setProjectLanguage(projectLanguage.trim());
        p.setProjectStage(projectStage.trim());

        p.setTargetCategory(targetCategory.trim());
        p.setTargetGender(targetGender.trim());
        p.setAgeMin(ageMin);
        p.setAgeMax(ageMax);
        p.setHeightRequirement(trimOrNull(heightRequirement));
        p.setPreferredAppearance(trimOrNull(preferredAppearance));
        p.setExperienceLevel(experienceLevel);
        p.setArtistsRequiredCount(Math.max(1, artistsRequiredCount));
        p.setRoleDescription(trimOrNull(roleDescription));
        p.setCharacterName(trimOrNull(characterName));

        p.setLocation(location == null || location.isBlank() ? company.getCity() : location.trim());
        p.setAuditionLocation(trimOrNull(auditionLocation));
        p.setAuditionDate(parseDateOrNull(auditionDate));
        p.setAuditionType(auditionType);
        p.setShootingStartDate(parseDateOrNull(shootingStartDate));
        p.setShootingEndDate(parseDateOrNull(shootingEndDate));
        p.setShootingDuration(trimOrNull(shootingDuration));
        p.setAvailabilityRequirements(trimOrNull(availabilityRequirements));
        p.setDurationType(durationType);

        p.setPaymentType(paymentType);
        p.setBudget(trimOrNull(budget));
        p.setPaymentBasis(paymentBasis);
        p.setAccommodationProvided(accommodationProvided);
        p.setFoodProvided(foodProvided);
        p.setTravelExpensesCovered(travelExpensesCovered);

        p.setAuditionProcess(trimOrNull(auditionProcess));
        p.setAuditionInstructions(trimOrNull(auditionInstructions));
        p.setSelfIntroVideoRequired(selfIntroVideoRequired);
        p.setPortfolioRequired(portfolioRequired);
        p.setAuditionScript(trimOrNull(auditionScript));
        LocalDate applyDeadline = parseDateOrNull(lastDateToApply);
        p.setLastDateToApply(applyDeadline);
        p.setOnlineAuditionEnabled(onlineAuditionEnabled);

        p.setRequiredPhotographs(trimOrNull(requiredPhotographs));
        p.setIntroVideoRequirement(trimOrNull(introVideoRequirement));
        p.setActingPortfolioRequirement(trimOrNull(actingPortfolioRequirement));
        p.setPreviousWorkLinksReq(trimOrNull(previousWorkLinksReq));
        p.setApplicantContactReq(trimOrNull(applicantContactReq));
        p.setAdditionalDocumentsReq(trimOrNull(additionalDocumentsReq));

        p.setCastingDirectorName(castingDirectorName == null || castingDirectorName.isBlank() ? company.getRepresentativeName() : castingDirectorName.trim());
        p.setContactEmail(contactEmail == null || contactEmail.isBlank() ? company.getCompanyEmail() : contactEmail.trim());
        p.setContactNumber(contactNumber == null || contactNumber.isBlank() ? company.getContactNumber() : contactNumber.trim());
        p.setWhatsappNumber(trimOrNull(whatsappNumber));
        p.setWebsiteOrSocialLinks(websiteOrSocialLinks == null || websiteOrSocialLinks.isBlank() ? company.getWebsite() : websiteOrSocialLinks.trim());

        if (projectPoster != null && !projectPoster.isEmpty()) {
            p.setProjectPosterPath(fileStorage.store(projectPoster, "projects"));
        }
        p.setReferenceImagePathsJson(storeMultiPathsJson(referenceImages, "projects"));
        p.setCharacterLookPathsJson(storeMultiPathsJson(characterLookReferences, "projects"));
        if (auditionScriptPdf != null && !auditionScriptPdf.isEmpty()) {
            p.setAuditionScriptPdfPath(fileStorage.store(auditionScriptPdf, "projects"));
        }
        if (projectPromotionalImage != null && !projectPromotionalImage.isEmpty()) {
            p.setProjectPromotionalImagePath(fileStorage.store(projectPromotionalImage, "projects"));
        }

        p.setTags(trimOrNull(tags));
        p.setPostedDate(LocalDate.now());
        p.setExpiryDate(applyDeadline != null ? applyDeadline : LocalDate.now().plusDays(30));
        p.setLastViewedDate(LocalDate.now());

        CastingProject saved = projectRepo.save(p);

        // Attach matching Suggested/Applicant talent so View Applications is immediately actionable
        List<ArtistProfile> matching = artistRepo.findAll().stream()
                .filter(a -> "All".equalsIgnoreCase(saved.getTargetCategory())
                        || a.getCategory().equalsIgnoreCase(saved.getTargetCategory()))
                .limit(4)
                .toList();
        if (matching.isEmpty()) {
            matching = artistRepo.findAll().stream().limit(4).toList();
        }
        int idx = 0;
        for (ArtistProfile a : matching) {
            ProjectApplication pa = new ProjectApplication();
            pa.setProjectId(saved.getId());
            pa.setArtistId(a.getId());
            pa.setRoleName(saved.getTargetCategory());
            pa.setStatus(idx < 2 ? "APPLIED" : "SUGGESTED");
            pa.setAuditionSubmissionTitle(a.isHasVideo() ? a.getVideoTitle() : "Portfolio Match");
            pa.setAuditionVideoUrl(a.getVideoUrl() != null ? a.getVideoUrl() : "/assets/video/audition-1.mp4");
            appRepo.save(pa);
            idx++;
        }

        notificationService.notifyCompany(company.getId(), company.getCompanyEmail(),
                "Casting Project Published",
                "'" + saved.getProjectTitle() + "' is live and receiving online auditions.");

        return ResponseEntity.status(HttpStatus.CREATED).body(toProjectSummary(saved, company));
    }

    @PostMapping("/projects/{id}/promote")
    public ResponseEntity<?> promoteProject(@PathVariable Long id, HttpSession session) {
        CastingCompany company = authHelper.requirePaidDashboardCompany(session);
        CastingProject project = projectRepo.findByIdAndCompanyId(id, company.getId())
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));
        if (project.isPromoted()) {
            return ResponseEntity.ok(Map.of("message", "Project is already promoted", "project", toProjectSummary(project, company)));
        }
        if (company.getProjectPromotionsRemaining() <= 0) {
            return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED)
                    .body(Map.of("error", "No project promotions remaining on your current pass. Upgrade pass to promote more projects."));
        }
        company.setProjectPromotionsRemaining(company.getProjectPromotionsRemaining() - 1);
        companyRepo.save(company);
        project.setPromoted(true);
        projectRepo.save(project);
        notificationService.notifyCompany(company.getId(), company.getCompanyEmail(),
                "Project Promoted", "'" + project.getProjectTitle() + "' is now boosted across artist feeds.");
        return ResponseEntity.ok(Map.of("message", "Project promoted!", "project", toProjectSummary(project, company)));
    }

    @GetMapping("/applications")
    public ResponseEntity<?> viewApplications(
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false, defaultValue = "ALL") String tab,
            @RequestParam(required = false, defaultValue = "ALL") String role,
            @RequestParam(required = false, defaultValue = "") String q,
            @RequestParam(required = false, defaultValue = "RELEVANCE") String sort,
            HttpSession session
    ) {
        CastingCompany company = authHelper.requirePaidDashboardCompany(session);
        List<CastingProject> projects = projectRepo.findByCompanyIdOrderByPostedDateDescIdDesc(company.getId());
        if (projects.isEmpty()) {
            return ResponseEntity.ok(Map.of("projects", List.of(), "applications", List.of(), "roles", List.of()));
        }
        Long activeProjectId = projectId != null ? projectId : projects.get(0).getId();
        CastingProject activeProject = projectRepo.findByIdAndCompanyId(activeProjectId, company.getId())
                .orElse(projects.get(0));
        activeProject.setLastViewedDate(LocalDate.now());
        projectRepo.save(activeProject);

        Map<Long, ArtistProfile> artistById = artistRepo.findAll().stream()
                .collect(Collectors.toMap(ArtistProfile::getId, a -> a));
        Set<Long> unlockedIds = getUnlockedContactIds(company.getId());
        Set<Long> savedIds = getSavedArtistIds(company.getId());

        List<ProjectApplication> rawApps = appRepo.findByProjectIdOrderByAppliedAtDesc(activeProject.getId());
        List<String> roles = rawApps.stream().map(ProjectApplication::getRoleName).distinct().toList();
        String query = q.trim().toLowerCase(Locale.ROOT);

        List<Map<String, Object>> items = rawApps.stream()
                .filter(pa -> "ALL".equalsIgnoreCase(tab) || pa.getStatus().equalsIgnoreCase(tab))
                .filter(pa -> "ALL".equalsIgnoreCase(role) || pa.getRoleName().equalsIgnoreCase(role))
                .map(pa -> {
                    ArtistProfile a = artistById.get(pa.getArtistId());
                    if (a == null) return null;
                    if (!query.isEmpty() && !a.getFullName().toLowerCase(Locale.ROOT).contains(query)
                            && !a.getCity().toLowerCase(Locale.ROOT).contains(query)
                            && !a.getCategory().toLowerCase(Locale.ROOT).contains(query)) {
                        return null;
                    }
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("applicationId", pa.getId());
                    m.put("projectId", pa.getProjectId());
                    m.put("roleName", pa.getRoleName());
                    m.put("applicationStatus", pa.getStatus());
                    m.put("auditionSubmissionTitle", pa.getAuditionSubmissionTitle());
                    m.put("auditionVideoUrl", pa.getAuditionVideoUrl());
                    m.put("appliedAt", pa.getAppliedAt());
                    m.put("artist", toArtistView(a, unlockedIds.contains(a.getId()), savedIds.contains(a.getId())));
                    return m;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(ArrayList::new));

        if ("POPULAR".equalsIgnoreCase(sort)) {
            items.sort((x, y) -> Integer.compare(
                    (int) ((Map<?, ?>) y.get("artist")).get("viewsCount"),
                    (int) ((Map<?, ?>) x.get("artist")).get("viewsCount")));
        }

        return ResponseEntity.ok(Map.of(
                "projects", projects.stream().map(p -> toProjectSummary(p, company)).toList(),
                "activeProject", toProjectSummary(activeProject, company),
                "roles", roles,
                "applications", items
        ));
    }

    @PostMapping("/applications/{id}/status")
    public ResponseEntity<?> updateApplicationStatus(@PathVariable Long id,
                                                   @RequestBody Map<String, String> body,
                                                   HttpSession session) {
        CastingCompany company = authHelper.requirePaidDashboardCompany(session);
        ProjectApplication pa = appRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));
        projectRepo.findByIdAndCompanyId(pa.getProjectId(), company.getId())
                .orElseThrow(() -> new IllegalArgumentException("Unauthorized project access"));
        String status = body.getOrDefault("status", "SHORTLISTED").toUpperCase(Locale.ROOT);
        pa.setStatus(status);
        appRepo.save(pa);
        return ResponseEntity.ok(pa);
    }

    @PostMapping("/artists/search")
    public ResponseEntity<?> searchArtists(@RequestBody Map<String, Object> filters, HttpSession session) {
        CastingCompany company = authHelper.requirePaidDashboardCompany(session);

        String sortBy = String.valueOf(filters.getOrDefault("sortBy", "Relevant"));
        String category = String.valueOf(filters.getOrDefault("category", "All"));
        String gender = String.valueOf(filters.getOrDefault("gender", "All"));
        String agency = String.valueOf(filters.getOrDefault("agency", "All"));
        int ageMin = Integer.parseInt(String.valueOf(filters.getOrDefault("ageMin", 0)));
        int ageMax = Integer.parseInt(String.valueOf(filters.getOrDefault("ageMax", 75)));
        String city = String.valueOf(filters.getOrDefault("city", "")).trim();
        String expertise = String.valueOf(filters.getOrDefault("expertise", "All"));
        String language = String.valueOf(filters.getOrDefault("language", "")).trim();
        String keyword = String.valueOf(filters.getOrDefault("keyword", "")).trim();
        boolean videoOnly = Boolean.parseBoolean(String.valueOf(filters.getOrDefault("videoOnly", false)));
        boolean recordHistory = Boolean.parseBoolean(String.valueOf(filters.getOrDefault("recordHistory", true)));

        if (recordHistory && (!"All".equalsIgnoreCase(category) || !city.isBlank() || !keyword.isBlank())) {
            SearchHistoryEntry she = new SearchHistoryEntry();
            she.setCompanyId(company.getId());
            she.setCategory("All".equalsIgnoreCase(category) ? null : category);
            she.setCity(city.isBlank() ? null : city);
            she.setKeyword(keyword.isBlank() ? null : keyword);
            searchHistoryRepo.save(she);
        }

        Set<Long> unlockedIds = getUnlockedContactIds(company.getId());
        Set<Long> savedIds = getSavedArtistIds(company.getId());
        String cityLc = city.toLowerCase(Locale.ROOT);
        String langLc = language.toLowerCase(Locale.ROOT);
        String kwLc = keyword.toLowerCase(Locale.ROOT);

        List<ArtistProfile> matched = artistRepo.findAll().stream()
                .filter(a -> "All".equalsIgnoreCase(category) || a.getCategory().equalsIgnoreCase(category)
                        || (a.getSecondaryRoles() != null && a.getSecondaryRoles().toLowerCase(Locale.ROOT).contains(category.toLowerCase(Locale.ROOT))))
                .filter(a -> "All".equalsIgnoreCase(gender) || a.getGender().equalsIgnoreCase(gender))
                .filter(a -> "All".equalsIgnoreCase(agency) || a.getAgencyName().equalsIgnoreCase(agency))
                .filter(a -> a.getAge() >= ageMin && a.getAge() <= ageMax)
                .filter(a -> cityLc.isEmpty() || "any".equals(cityLc) || a.getCity().toLowerCase(Locale.ROOT).contains(cityLc))
                .filter(a -> "All".equalsIgnoreCase(expertise) || a.getExpertise().equalsIgnoreCase(expertise))
                .filter(a -> langLc.isEmpty() || a.getLanguages().toLowerCase(Locale.ROOT).contains(langLc))
                .filter(a -> !videoOnly || a.isHasVideo())
                .filter(a -> kwLc.isEmpty()
                        || a.getFullName().toLowerCase(Locale.ROOT).contains(kwLc)
                        || (a.getKeywords() != null && a.getKeywords().toLowerCase(Locale.ROOT).contains(kwLc))
                        || (a.getBio() != null && a.getBio().toLowerCase(Locale.ROOT).contains(kwLc)))
                .collect(Collectors.toCollection(ArrayList::new));

        if ("Most Viewed".equalsIgnoreCase(sortBy)) {
            matched.sort(Comparator.comparingInt(ArtistProfile::getViewsCount).reversed());
        } else if ("Newest".equalsIgnoreCase(sortBy)) {
            matched.sort(Comparator.comparingLong(ArtistProfile::getId).reversed());
        } else {
            matched.sort(Comparator.comparing(ArtistProfile::isShowcaseBadge).reversed()
                    .thenComparingInt(ArtistProfile::getViewsCount).reversed());
        }

        return ResponseEntity.ok(Map.of(
                "total", matched.size(),
                "artists", matched.stream()
                        .map(a -> toArtistView(a, unlockedIds.contains(a.getId()), savedIds.contains(a.getId())))
                        .toList()
        ));
    }

    @GetMapping("/artists/recommended")
    public ResponseEntity<?> getRecommendedArtists(@RequestParam(defaultValue = "PLATINUM") String tier,
                                                   HttpSession session) {
        CastingCompany company = authHelper.requirePaidDashboardCompany(session);
        Set<Long> unlockedIds = getUnlockedContactIds(company.getId());
        Set<Long> savedIds = getSavedArtistIds(company.getId());

        List<SearchHistoryEntry> history = searchHistoryRepo.findTop10ByCompanyIdOrderBySearchedAtDesc(company.getId());
        Set<String> preferredCats = history.stream()
                .map(SearchHistoryEntry::getCategory)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<ArtistProfile> all = artistRepo.findAll();
        List<ArtistProfile> filtered = all.stream()
                .filter(a -> "FEATURED".equalsIgnoreCase(tier) ? a.isFeaturedTier() : a.isPlatinumTier())
                .sorted((a, b) -> {
                    int scoreA = preferredCats.contains(a.getCategory()) ? 1 : 0;
                    int scoreB = preferredCats.contains(b.getCategory()) ? 1 : 0;
                    return scoreB != scoreA ? Integer.compare(scoreB, scoreA)
                            : Integer.compare(b.getViewsCount(), a.getViewsCount());
                })
                .toList();
        if (filtered.isEmpty()) filtered = all.stream().limit(8).toList();

        ArtistProfile rising = all.stream().filter(ArtistProfile::isRisingStar).findFirst().orElse(all.get(0));

        return ResponseEntity.ok(Map.of(
                "tier", tier,
                "risingStar", toArtistView(rising, unlockedIds.contains(rising.getId()), savedIds.contains(rising.getId())),
                "searchSignals", history,
                "artists", filtered.stream()
                        .map(a -> toArtistView(a, unlockedIds.contains(a.getId()), savedIds.contains(a.getId())))
                        .toList()
        ));
    }

    @PostMapping("/artists/bulk-action")
    public ResponseEntity<?> bulkAction(@RequestBody Map<String, Object> body, HttpSession session) {
        CastingCompany company = authHelper.requirePaidDashboardCompany(session);
        String action = String.valueOf(body.getOrDefault("action", ""));
        List<?> rawIds = (List<?>) body.getOrDefault("artistIds", List.of());
        List<Long> artistIds = rawIds.stream().map(o -> Long.valueOf(String.valueOf(o))).toList();
        if (artistIds.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Select at least one artist first."));
        }

        if ("INVITE_AUDITION".equalsIgnoreCase(action)) {
            int remainingInvites = Math.max(0, company.getAuditionInvitesTotal() - company.getAuditionInvitesUsed());
            if (remainingInvites < artistIds.size()) {
                return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED)
                        .body(Map.of("error", "Not enough Audition Invites left on your pass. Please upgrade your subscription pass."));
            }
            Long projectId = body.get("projectId") != null ? Long.valueOf(String.valueOf(body.get("projectId"))) : null;
            List<CastingProject> companyProjects = projectRepo.findByCompanyIdOrderByPostedDateDescIdDesc(company.getId());
            if (projectId == null && !companyProjects.isEmpty()) {
                projectId = companyProjects.get(0).getId();
            }
            if (projectId != null) {
                for (Long aid : artistIds) {
                    ProjectApplication pa = appRepo.findByProjectIdAndArtistId(projectId, aid).orElseGet(ProjectApplication::new);
                    pa.setProjectId(projectId);
                    pa.setArtistId(aid);
                    if (pa.getRoleName() == null) pa.setRoleName("Invited Talent");
                    pa.setStatus("INVITED");
                    appRepo.save(pa);
                }
            }
            company.setAuditionInvitesUsed(company.getAuditionInvitesUsed() + artistIds.size());
            companyRepo.save(company);
            notificationService.notifyCompany(company.getId(), company.getCompanyEmail(),
                    "Audition Invites Dispatched",
                    artistIds.size() + " artist(s) were invited to submit their online audition.");
            return ResponseEntity.ok(Map.of("message", "Invited " + artistIds.size() + " artist(s) to audition."));
        }

        if ("TAG_FOLDER".equalsIgnoreCase(action)) {
            Long folderId = body.get("folderId") != null ? Long.valueOf(String.valueOf(body.get("folderId"))) : null;
            List<ArtistFolder> folders = folderRepo.findByCompanyIdOrderByIdAsc(company.getId());
            ArtistFolder targetFolder = (folderId != null)
                    ? folderRepo.findByIdAndCompanyId(folderId, company.getId()).orElse(folders.get(0))
                    : folders.get(0);
            int added = 0;
            for (Long aid : artistIds) {
                if (folderItemRepo.findByCompanyIdAndFolderIdAndArtistId(company.getId(), targetFolder.getId(), aid).isEmpty()) {
                    ArtistFolderItem item = new ArtistFolderItem();
                    item.setCompanyId(company.getId());
                    item.setFolderId(targetFolder.getId());
                    item.setArtistId(aid);
                    folderItemRepo.save(item);
                    added++;
                }
            }
            return ResponseEntity.ok(Map.of("message", "Tagged " + added + " artist(s) into folder '" + targetFolder.getFolderName() + "'."));
        }

        return ResponseEntity.badRequest().body(Map.of("error", "Unsupported bulk action: " + action));
    }

    @GetMapping("/artists/export-excel")
    public ResponseEntity<byte[]> exportExcel(@RequestParam(required = false) String ids, HttpSession session) {
        CastingCompany company = authHelper.requirePaidDashboardCompany(session);
        Set<Long> unlockedIds = getUnlockedContactIds(company.getId());
        Set<Long> filterIds = (ids == null || ids.isBlank()) ? Set.of() :
                Arrays.stream(ids.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .map(Long::valueOf)
                        .collect(Collectors.toSet());

        List<ArtistProfile> rows = artistRepo.findAll().stream()
                .filter(a -> filterIds.isEmpty() || filterIds.contains(a.getId()))
                .toList();

        StringBuilder sb = new StringBuilder("\uFEFF"); // UTF-8 BOM for clean Excel opening
        sb.append("Artist ID\tFull Name\tCategory\tGender\tAge\tCity\tExpertise\tLanguages\tAgency\tHas Video\tContact Email\tContact Phone\n");
        for (ArtistProfile a : rows) {
            boolean unlocked = unlockedIds.contains(a.getId());
            sb.append(a.getId()).append('\t')
              .append(cleanCell(a.getFullName())).append('\t')
              .append(cleanCell(a.getCategory())).append('\t')
              .append(cleanCell(a.getGender())).append('\t')
              .append(a.getAge()).append('\t')
              .append(cleanCell(a.getCity())).append('\t')
              .append(cleanCell(a.getExpertise())).append('\t')
              .append(cleanCell(a.getLanguages())).append('\t')
              .append(cleanCell(a.getAgencyName())).append('\t')
              .append(a.isHasVideo() ? "Yes" : "No").append('\t')
              .append(unlocked ? cleanCell(a.getEmail()) : "Locked (Unlock Contact)").append('\t')
              .append(unlocked ? cleanCell(a.getPhone()) : maskPhone(a.getPhone())).append('\n');
        }

        byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"entertainment-world-artists.xls\"")
                .contentType(MediaType.parseMediaType("application/vnd.ms-excel; charset=UTF-8"))
                .body(bytes);
    }

    @PostMapping("/artists/{artistId}/view-portfolio")
    public ResponseEntity<?> viewPortfolio(@PathVariable Long artistId, HttpSession session) {
        CastingCompany company = authHelper.requirePaidDashboardCompany(session);
        ArtistProfile artist = artistRepo.findById(artistId)
                .orElseThrow(() -> new IllegalArgumentException("Artist not found"));

        boolean alreadyViewed = historyRepo.existsByCompanyIdAndArtistIdAndViewType(company.getId(), artistId, "PORTFOLIO_VIEW");
        if (!alreadyViewed) {
            if (company.getPortfolioViewsTotal() >= 0 && company.getPortfolioViewsUsed() >= company.getPortfolioViewsTotal()) {
                return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED)
                        .body(Map.of("error", "Portfolio view quota exhausted. Please upgrade your subscription pass."));
            }
            company.setPortfolioViewsUsed(company.getPortfolioViewsUsed() + 1);
            companyRepo.save(company);
            ContactPortfolioHistory h = new ContactPortfolioHistory();
            h.setCompanyId(company.getId());
            h.setArtistId(artistId);
            h.setViewType("PORTFOLIO_VIEW");
            h.setTierType("PAID");
            historyRepo.save(h);
        }

        boolean unlocked = historyRepo.existsByCompanyIdAndArtistIdAndViewType(company.getId(), artistId, "CONTACT_VIEW");
        boolean saved = getSavedArtistIds(company.getId()).contains(artistId);
        return ResponseEntity.ok(Map.of(
                "artist", toArtistView(artist, unlocked, saved),
                "company", authHelper.toSafeCompanyMap(company)
        ));
    }

    @PostMapping("/artists/{artistId}/unlock-contact")
    public ResponseEntity<?> unlockContact(@PathVariable Long artistId, HttpSession session) {
        CastingCompany company = authHelper.requirePaidDashboardCompany(session);
        ArtistProfile artist = artistRepo.findById(artistId)
                .orElseThrow(() -> new IllegalArgumentException("Artist not found"));

        boolean alreadyUnlocked = historyRepo.existsByCompanyIdAndArtistIdAndViewType(company.getId(), artistId, "CONTACT_VIEW");
        if (!alreadyUnlocked) {
            if (company.getContactViewsUsed() >= company.getContactViewsTotal()) {
                return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED)
                        .body(Map.of("error", "Contact view limit reached (" + company.getContactViewsTotal() + "). Upgrade your pass to unlock more contacts."));
            }
            company.setContactViewsUsed(company.getContactViewsUsed() + 1);
            companyRepo.save(company);

            ContactPortfolioHistory h = new ContactPortfolioHistory();
            h.setCompanyId(company.getId());
            h.setArtistId(artistId);
            h.setViewType("CONTACT_VIEW");
            h.setTierType("PAID");
            historyRepo.save(h);
        }

        boolean saved = getSavedArtistIds(company.getId()).contains(artistId);
        return ResponseEntity.ok(Map.of(
                "artist", toArtistView(artist, true, saved),
                "company", authHelper.toSafeCompanyMap(company)
        ));
    }

    @GetMapping("/folders")
    public ResponseEntity<?> getFoldersAndArtists(
            @RequestParam(required = false) Long folderId,
            @RequestParam(required = false, defaultValue = "All") String category,
            @RequestParam(required = false, defaultValue = "") String city,
            HttpSession session
    ) {
        CastingCompany company = authHelper.requirePaidDashboardCompany(session);
        List<ArtistFolder> folders = folderRepo.findByCompanyIdOrderByIdAsc(company.getId());
        List<ArtistFolderItem> items = (folderId == null || folderId <= 0)
                ? folderItemRepo.findByCompanyIdOrderBySavedAtDesc(company.getId())
                : folderItemRepo.findByCompanyIdAndFolderIdOrderBySavedAtDesc(company.getId(), folderId);

        Map<Long, ArtistProfile> artistById = artistRepo.findAll().stream()
                .collect(Collectors.toMap(ArtistProfile::getId, a -> a));
        Set<Long> unlockedIds = getUnlockedContactIds(company.getId());
        String cityLc = city.trim().toLowerCase(Locale.ROOT);

        // Deduplicate artists when viewing "All Artists"
        Map<Long, ArtistProfile> orderedUnique = new LinkedHashMap<>();
        for (ArtistFolderItem it : items) {
            ArtistProfile a = artistById.get(it.getArtistId());
            if (a != null) orderedUnique.putIfAbsent(a.getId(), a);
        }

        List<Map<String, Object>> artists = orderedUnique.values().stream()
                .filter(a -> "All".equalsIgnoreCase(category) || a.getCategory().equalsIgnoreCase(category))
                .filter(a -> cityLc.isEmpty() || a.getCity().toLowerCase(Locale.ROOT).contains(cityLc))
                .map(a -> toArtistView(a, unlockedIds.contains(a.getId()), true))
                .toList();

        List<Map<String, Object>> folderViews = folders.stream().map(f -> Map.<String, Object>of(
                "id", f.getId(),
                "folderName", f.getFolderName(),
                "count", folderItemRepo.findByCompanyIdAndFolderIdOrderBySavedAtDesc(company.getId(), f.getId()).size()
        )).toList();

        return ResponseEntity.ok(Map.of(
                "folders", folderViews,
                "totalSaved", folderItemRepo.findByCompanyIdOrderBySavedAtDesc(company.getId()).stream().map(ArtistFolderItem::getArtistId).distinct().count(),
                "artists", artists
        ));
    }

    @PostMapping("/folders")
    public ResponseEntity<?> createFolder(@RequestBody Map<String, String> body, HttpSession session) {
        CastingCompany company = authHelper.requirePaidDashboardCompany(session);
        String name = body.getOrDefault("folderName", "").trim();
        if (name.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Folder name is required."));
        }
        if (folderRepo.findByCompanyIdAndFolderNameIgnoreCase(company.getId(), name).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "Folder already exists."));
        }
        ArtistFolder folder = new ArtistFolder();
        folder.setCompanyId(company.getId());
        folder.setFolderName(name);
        return ResponseEntity.status(HttpStatus.CREATED).body(folderRepo.save(folder));
    }

    @DeleteMapping("/folders/{id}")
    @Transactional
    public ResponseEntity<?> deleteFolder(@PathVariable Long id, HttpSession session) {
        CastingCompany company = authHelper.requirePaidDashboardCompany(session);
        ArtistFolder folder = folderRepo.findByIdAndCompanyId(id, company.getId())
                .orElseThrow(() -> new IllegalArgumentException("Folder not found"));
        folderItemRepo.deleteByCompanyIdAndFolderId(company.getId(), folder.getId());
        folderRepo.delete(folder);
        return ResponseEntity.ok(Map.of("message", "Folder deleted"));
    }

    @PostMapping("/folders/toggle/{artistId}")
    @Transactional
    public ResponseEntity<?> toggleArtistSave(@PathVariable Long artistId,
                                              @RequestParam(required = false) Long folderId,
                                              HttpSession session) {
        CastingCompany company = authHelper.requirePaidDashboardCompany(session);
        List<ArtistFolder> folders = folderRepo.findByCompanyIdOrderByIdAsc(company.getId());
        ArtistFolder target = (folderId != null)
                ? folderRepo.findByIdAndCompanyId(folderId, company.getId()).orElse(folders.get(0))
                : folders.get(0);

        Optional<ArtistFolderItem> existing = folderItemRepo.findByCompanyIdAndFolderIdAndArtistId(
                company.getId(), target.getId(), artistId);
        if (existing.isPresent()) {
            folderItemRepo.delete(existing.get());
            return ResponseEntity.ok(Map.of("saved", false, "folderName", target.getFolderName()));
        } else {
            ArtistFolderItem item = new ArtistFolderItem();
            item.setCompanyId(company.getId());
            item.setFolderId(target.getId());
            item.setArtistId(artistId);
            folderItemRepo.save(item);
            return ResponseEntity.ok(Map.of("saved", true, "folderName", target.getFolderName()));
        }
    }

    @GetMapping("/transactions")
    public ResponseEntity<?> getTransactions(
            @RequestParam(defaultValue = "CONTACT_VIEW") String viewType,
            @RequestParam(defaultValue = "PAID") String tier,
            @RequestParam(defaultValue = "RECENT") String sort,
            HttpSession session
    ) {
        CastingCompany company = authHelper.requirePaidDashboardCompany(session);
        List<ContactPortfolioHistory> logs = historyRepo.findByCompanyIdAndViewTypeOrderByViewedAtDesc(company.getId(), viewType);
        Map<Long, ArtistProfile> artistById = artistRepo.findAll().stream()
                .collect(Collectors.toMap(ArtistProfile::getId, a -> a));
        Set<Long> unlockedIds = getUnlockedContactIds(company.getId());
        Set<Long> savedIds = getSavedArtistIds(company.getId());

        List<Map<String, Object>> viewedItems = logs.stream()
                .filter(h -> "ALL".equalsIgnoreCase(tier) || h.getTierType().equalsIgnoreCase(tier))
                .map(h -> {
                    ArtistProfile a = artistById.get(h.getArtistId());
                    if (a == null) return null;
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("historyId", h.getId());
                    m.put("viewType", h.getViewType());
                    m.put("tierType", h.getTierType());
                    m.put("viewedAt", h.getViewedAt());
                    m.put("artist", toArtistView(a, unlockedIds.contains(a.getId()), savedIds.contains(a.getId())));
                    return m;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(ArrayList::new));

        if ("OLDEST".equalsIgnoreCase(sort)) {
            Collections.reverse(viewedItems);
        }

        return ResponseEntity.ok(Map.of(
                "company", authHelper.toSafeCompanyMap(company),
                "passes", passRepo.findAllByOrderByDisplayOrderAsc(),
                "viewedItems", viewedItems,
                "purchaseHistory", paymentRepo.findByCompanyIdOrderByPaidAtDesc(company.getId())
        ));
    }

    @PostMapping("/notifications/read-all")
    public ResponseEntity<?> markNotificationsRead(HttpSession session) {
        CastingCompany company = authHelper.requirePaidDashboardCompany(session);
        List<CompanyNotification> all = notificationRepo.findByCompanyIdOrderByCreatedAtDesc(company.getId());
        all.forEach(n -> n.setReadFlag(true));
        notificationRepo.saveAll(all);
        return ResponseEntity.ok(Map.of("unreadNotifications", 0));
    }

    private Set<Long> getUnlockedContactIds(Long companyId) {
        return historyRepo.findByCompanyIdAndViewTypeOrderByViewedAtDesc(companyId, "CONTACT_VIEW")
                .stream().map(ContactPortfolioHistory::getArtistId).collect(Collectors.toSet());
    }

    private Set<Long> getSavedArtistIds(Long companyId) {
        return folderItemRepo.findByCompanyIdOrderBySavedAtDesc(companyId)
                .stream().map(ArtistFolderItem::getArtistId).collect(Collectors.toSet());
    }

    private Map<String, Object> toProjectSummary(CastingProject p, CastingCompany company) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", p.getId());
        m.put("projectTitle", p.getProjectTitle());
        m.put("projectType", p.getProjectType());
        m.put("productionHouseName", p.getProductionHouseName());
        m.put("castingCompanyName", p.getCastingCompanyName() != null ? p.getCastingCompanyName() : company.getCompanyName());
        m.put("projectDetails", p.getProjectDetails());
        m.put("projectLanguage", p.getProjectLanguage());
        m.put("projectStage", p.getProjectStage());

        m.put("targetCategory", p.getTargetCategory());
        m.put("targetGender", p.getTargetGender());
        m.put("ageMin", p.getAgeMin());
        m.put("ageMax", p.getAgeMax());
        m.put("heightRequirement", p.getHeightRequirement());
        m.put("preferredAppearance", p.getPreferredAppearance());
        m.put("experienceLevel", p.getExperienceLevel());
        m.put("artistsRequiredCount", p.getArtistsRequiredCount());
        m.put("roleDescription", p.getRoleDescription());
        m.put("characterName", p.getCharacterName());

        m.put("location", p.getLocation());
        m.put("auditionLocation", p.getAuditionLocation());
        m.put("auditionDate", p.getAuditionDate());
        m.put("auditionType", p.getAuditionType());
        m.put("shootingStartDate", p.getShootingStartDate());
        m.put("shootingEndDate", p.getShootingEndDate());
        m.put("shootingDuration", p.getShootingDuration());
        m.put("availabilityRequirements", p.getAvailabilityRequirements());
        m.put("durationType", p.getDurationType());

        m.put("paymentType", p.getPaymentType());
        m.put("budget", p.getBudget());
        m.put("paymentBasis", p.getPaymentBasis());
        m.put("accommodationProvided", p.getAccommodationProvided());
        m.put("foodProvided", p.getFoodProvided());
        m.put("travelExpensesCovered", p.getTravelExpensesCovered());

        m.put("auditionProcess", p.getAuditionProcess());
        m.put("auditionInstructions", p.getAuditionInstructions());
        m.put("selfIntroVideoRequired", p.getSelfIntroVideoRequired());
        m.put("portfolioRequired", p.getPortfolioRequired());
        m.put("auditionScript", p.getAuditionScript());
        m.put("lastDateToApply", p.getLastDateToApply());
        m.put("onlineAuditionEnabled", p.isOnlineAuditionEnabled());

        m.put("requiredPhotographs", p.getRequiredPhotographs());
        m.put("introVideoRequirement", p.getIntroVideoRequirement());
        m.put("actingPortfolioRequirement", p.getActingPortfolioRequirement());
        m.put("previousWorkLinksReq", p.getPreviousWorkLinksReq());
        m.put("applicantContactReq", p.getApplicantContactReq());
        m.put("additionalDocumentsReq", p.getAdditionalDocumentsReq());

        m.put("castingDirectorName", p.getCastingDirectorName());
        m.put("contactEmail", p.getContactEmail());
        m.put("contactNumber", p.getContactNumber());
        m.put("whatsappNumber", p.getWhatsappNumber());
        m.put("websiteOrSocialLinks", p.getWebsiteOrSocialLinks());

        m.put("projectPosterPath", p.getProjectPosterPath());
        m.put("referenceImagePathsJson", p.getReferenceImagePathsJson());
        m.put("characterLookPathsJson", p.getCharacterLookPathsJson());
        m.put("auditionScriptPdfPath", p.getAuditionScriptPdfPath());
        m.put("projectPromotionalImagePath", p.getProjectPromotionalImagePath());

        m.put("tags", p.getTags());
        m.put("promoted", p.isPromoted());
        m.put("status", p.getStatus());
        m.put("postedDate", p.getPostedDate());
        m.put("expiryDate", p.getExpiryDate());
        m.put("lastViewedDate", p.getLastViewedDate());
        m.put("applicationsCount", appRepo.countByProjectId(p.getId()));
        m.put("companyName", company.getCompanyName());
        m.put("companyVerified", company.isVerifiedBadge());
        return m;
    }

    private String trimOrNull(String val) {
        return (val == null || val.isBlank()) ? null : val.trim();
    }

    private LocalDate parseDateOrNull(String val) {
        if (val == null || val.isBlank()) return null;
        return LocalDate.parse(val.trim());
    }

    private String storeMultiPathsJson(MultipartFile[] files, String subFolder) {
        if (files == null || files.length == 0) return null;
        List<String> paths = new ArrayList<>();
        for (MultipartFile f : files) {
            if (f != null && !f.isEmpty()) {
                paths.add(fileStorage.store(f, subFolder));
            }
        }
        if (paths.isEmpty()) return null;
        return "[" + paths.stream()
                .map(s -> "\"" + s.replace("\"", "\\\"") + "\"")
                .collect(Collectors.joining(",")) + "]";
    }

    private Map<String, Object> toArtistView(ArtistProfile a, boolean contactUnlocked, boolean saved) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", a.getId());
        m.put("fullName", a.getFullName());
        m.put("category", a.getCategory());
        m.put("secondaryRoles", a.getSecondaryRoles());
        m.put("gender", a.getGender());
        m.put("age", a.getAge());
        m.put("city", a.getCity());
        m.put("agencyName", a.getAgencyName());
        m.put("expertise", a.getExpertise());
        m.put("languages", a.getLanguages());
        m.put("bio", a.getBio());
        m.put("avatarUrl", a.getAvatarUrl());
        m.put("hasVideo", a.isHasVideo());
        m.put("videoTitle", a.getVideoTitle());
        m.put("videoUrl", a.getVideoUrl());
        m.put("showcaseBadge", a.isShowcaseBadge());
        m.put("platinumTier", a.isPlatinumTier());
        m.put("featuredTier", a.isFeaturedTier());
        m.put("risingStar", a.isRisingStar());
        m.put("viewsCount", a.getViewsCount());
        m.put("contactUnlocked", contactUnlocked);
        m.put("saved", saved);
        m.put("email", contactUnlocked ? a.getEmail() : maskEmail(a.getEmail()));
        m.put("phone", contactUnlocked ? a.getPhone() : maskPhone(a.getPhone()));
        return m;
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 6) return "+91 ******";
        return phone.substring(0, 6) + "*****";
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) return "****@artists.ew.in";
        int idx = email.indexOf('@');
        return email.substring(0, Math.min(3, idx)) + "****" + email.substring(idx);
    }

    private String cleanCell(String value) {
        if (value == null) return "";
        return value.replace('\t', ' ').replace('\n', ' ').replace('\r', ' ');
    }
}
