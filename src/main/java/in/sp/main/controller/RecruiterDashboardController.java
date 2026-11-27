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
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implements Casting Company Dashboard capabilities:
 * Verified Profile + Showreels, Analytics Overview, Post Project + 8-Section Brief, Project Management,
 * Application Pipeline + Ratings/Comments, Smart Search + Actor Verification, Custom Folders,
 * AI & Tier Recommendations, Get Package, and Transaction History.
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
    private final AuditionScheduleRepository auditionRepo;
    private final CastingTeamMemberRepository teamRepo;
    private final ArtistContractRepository contractRepo;
    private final ProductionExpenseRepository expenseRepo;
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
                                      AuditionScheduleRepository auditionRepo,
                                      CastingTeamMemberRepository teamRepo,
                                      ArtistContractRepository contractRepo,
                                      ProductionExpenseRepository expenseRepo,
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
        this.auditionRepo = auditionRepo;
        this.teamRepo = teamRepo;
        this.contractRepo = contractRepo;
        this.expenseRepo = expenseRepo;
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

        List<ArtistProfile> allArtists = artistRepo.findAll();
        long verifiedActors = allArtists.stream().filter(ArtistProfile::isProfileVerified).count();
        List<Long> projectIds = projects.stream().map(CastingProject::getId).toList();
        List<ProjectApplication> allApps = projectIds.isEmpty() ? List.of() : appRepo.findByProjectIdInOrderByAppliedAtDesc(projectIds);
        long shortlistedCount = allApps.stream().filter(pa -> "SHORTLISTED".equalsIgnoreCase(pa.getStatus())).count();
        long selectedCount = allApps.stream().filter(pa -> "SELECTED".equalsIgnoreCase(pa.getStatus()) && !pa.isBackupCandidate()).count();

        List<AuditionSchedule> auditions = auditionRepo.findByCompanyIdOrderByAuditionDateAscIdDesc(company.getId());
        long upcomingAuditionsCount = auditions.stream()
                .filter(au -> "SCHEDULED".equalsIgnoreCase(au.getStatus()) || "RESCHEDULED".equalsIgnoreCase(au.getStatus()))
                .filter(au -> au.getAuditionDate() != null && !au.getAuditionDate().isBefore(LocalDate.now()))
                .count();
        long favoriteActorsCount = folderItemRepo.findByCompanyIdOrderBySavedAtDesc(company.getId())
                .stream().map(ArtistFolderItem::getArtistId).distinct().count();
        long teamMembersCount = teamRepo.findByCompanyIdOrderByIdDesc(company.getId()).size();
        long activeContractsCount = contractRepo.findByCompanyIdOrderByIdDesc(company.getId())
                .stream().filter(c -> !"TERMINATED".equalsIgnoreCase(c.getContractStatus())).count();

        Map<Long, ArtistProfile> artistById = allArtists.stream()
                .collect(Collectors.toMap(ArtistProfile::getId, a -> a));
        Map<Long, CastingProject> projectById = projects.stream()
                .collect(Collectors.toMap(CastingProject::getId, p -> p));

        List<Map<String, Object>> recentActivities = new ArrayList<>();
        allApps.stream().limit(4).forEach(pa -> {
            ArtistProfile a = artistById.get(pa.getArtistId());
            CastingProject p = projectById.get(pa.getProjectId());
            Map<String, Object> act = new LinkedHashMap<>();
            act.put("type", "APPLICATION");
            act.put("title", (a != null ? a.getFullName() : "Artist") + " — " + pa.getStatus());
            act.put("subtitle", (p != null ? p.getProjectTitle() : "Project") + " (" + pa.getRoleName() + ")");
            act.put("timestamp", pa.getAppliedAt() != null ? pa.getAppliedAt().toString() : LocalDate.now().toString());
            recentActivities.add(act);
        });
        auditions.stream().limit(3).forEach(au -> {
            ArtistProfile a = artistById.get(au.getArtistId());
            CastingProject p = projectById.get(au.getProjectId());
            Map<String, Object> act = new LinkedHashMap<>();
            act.put("type", "AUDITION");
            act.put("title", au.getAuditionMode() + " Audition (" + au.getStatus() + "): " + (a != null ? a.getFullName() : "Artist"));
            act.put("subtitle", (p != null ? p.getProjectTitle() : "Project") + " on " + au.getAuditionDate() + " at " + au.getAuditionTime());
            act.put("timestamp", au.getAuditionDate() != null ? au.getAuditionDate().toString() : "");
            recentActivities.add(act);
        });

        List<Map<String, Object>> monthlyChart = new ArrayList<>();
        DateTimeFormatter mFmt = DateTimeFormatter.ofPattern("MMM", Locale.ENGLISH);
        YearMonth nowYm = YearMonth.now();
        for (int i = 5; i >= 0; i--) {
            YearMonth ym = nowYm.minusMonths(i);
            long appsInMonth = allApps.stream().filter(a -> a.getAppliedAt() != null && YearMonth.from(a.getAppliedAt()).equals(ym)).count();
            long audsInMonth = auditions.stream().filter(a -> a.getAuditionDate() != null && YearMonth.from(a.getAuditionDate()).equals(ym)).count();
            monthlyChart.add(Map.of(
                    "label", ym.format(mFmt),
                    "applications", appsInMonth,
                    "auditions", audsInMonth
            ));
        }

        Map<String, Long> appsByStatus = new LinkedHashMap<>();
        appsByStatus.put("APPLIED", allApps.stream().filter(a -> "APPLIED".equalsIgnoreCase(a.getStatus())).count());
        appsByStatus.put("SHORTLISTED", shortlistedCount);
        appsByStatus.put("INVITED", allApps.stream().filter(a -> "INVITED".equalsIgnoreCase(a.getStatus())).count());
        appsByStatus.put("SELECTED", selectedCount);
        appsByStatus.put("REJECTED", allApps.stream().filter(a -> "REJECTED".equalsIgnoreCase(a.getStatus())).count());

        Map<String, Long> actorsByCategory = allArtists.stream()
                .collect(Collectors.groupingBy(a -> a.getCategory() != null ? a.getCategory() : "Other", LinkedHashMap::new, Collectors.counting()));

        List<Map<String, Object>> projectSummaries = projects.stream().map(p -> toProjectSummary(p, company)).toList();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("company", authHelper.toSafeCompanyMap(company));
        response.put("activeProjectsCount", activeProjects);
        response.put("totalProjectsCount", projects.size());
        response.put("profilesViewedCount", profilesViewed);
        response.put("totalActorsCount", allArtists.size());
        response.put("verifiedActorsCount", verifiedActors);
        response.put("totalApplicationsCount", allApps.size());
        response.put("shortlistedCount", shortlistedCount);
        response.put("selectedCount", selectedCount);
        response.put("upcomingAuditionsCount", upcomingAuditionsCount);
        response.put("favoriteActorsCount", favoriteActorsCount);
        response.put("teamMembersCount", teamMembersCount);
        response.put("activeContractsCount", activeContractsCount);
        response.put("unreadNotifications", unreadCount);
        response.put("notifications", notifications.stream().limit(12).toList());
        response.put("recentProjects", projectSummaries.stream().limit(6).toList());
        response.put("recentActivities", recentActivities.stream().limit(8).toList());
        response.put("chartData", Map.of(
                "monthlyActivity", monthlyChart,
                "applicationsByStatus", appsByStatus,
                "actorsByCategory", actorsByCategory
        ));
        return ResponseEntity.ok(response);
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
    public ResponseEntity<?> listProjects(@RequestParam(value = "filter", required = false, defaultValue = "ALL") String filter,
                                          HttpSession session) {
        CastingCompany company = authHelper.requirePaidDashboardCompany(session);
        List<CastingProject> projects = projectRepo.findByCompanyIdOrderByPostedDateDescIdDesc(company.getId());
        List<Map<String, Object>> filtered = projects.stream()
                .filter(p -> "ALL".equalsIgnoreCase(filter)
                        || ("PROMOTED".equalsIgnoreCase(filter) && p.isPromoted())
                        || (p.getStatus() != null && p.getStatus().equalsIgnoreCase(filter)))
                .map(p -> toProjectSummary(p, company))
                .toList();
        return ResponseEntity.ok(filtered);
    }

    @PostMapping(value = "/projects", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> postProject(
            // 1. Basic project information
            @RequestParam("projectTitle") String projectTitle,
            @RequestParam("projectType") String projectType,
            @RequestParam("productionHouseName") String productionHouseName,
            @RequestParam("castingCompanyName") String castingCompanyName,
            @RequestParam("projectDetails") String projectDetails,
            @RequestParam("projectLanguage") String projectLanguage,
            @RequestParam("projectStage") String projectStage,
            // 2. Talent requirements
            @RequestParam("targetCategory") String targetCategory,
            @RequestParam(value = "targetGender", defaultValue = "Any") String targetGender,
            @RequestParam(value = "ageMin", defaultValue = "18") int ageMin,
            @RequestParam(value = "ageMax", defaultValue = "35") int ageMax,
            @RequestParam(value = "heightRequirement", required = false) String heightRequirement,
            @RequestParam(value = "preferredAppearance", required = false) String preferredAppearance,
            @RequestParam(value = "experienceLevel", defaultValue = "Both") String experienceLevel,
            @RequestParam(value = "artistsRequiredCount", defaultValue = "1") int artistsRequiredCount,
            @RequestParam(value = "roleDescription", required = false) String roleDescription,
            @RequestParam(value = "characterName", required = false) String characterName,
            // 3. Project location and schedule
            @RequestParam(value = "location", required = false) String location,
            @RequestParam(value = "auditionLocation", required = false) String auditionLocation,
            @RequestParam(value = "auditionDate", required = false) String auditionDate,
            @RequestParam(value = "auditionType", defaultValue = "Online") String auditionType,
            @RequestParam(value = "shootingStartDate", required = false) String shootingStartDate,
            @RequestParam(value = "shootingEndDate", required = false) String shootingEndDate,
            @RequestParam(value = "shootingDuration", required = false) String shootingDuration,
            @RequestParam(value = "availabilityRequirements", required = false) String availabilityRequirements,
            @RequestParam(value = "durationType", defaultValue = "Full-Time") String durationType,
            // 4. Payment details
            @RequestParam(value = "paymentType", defaultValue = "Paid") String paymentType,
            @RequestParam(value = "budget", required = false) String budget,
            @RequestParam(value = "paymentBasis", defaultValue = "Per day") String paymentBasis,
            @RequestParam(value = "accommodationProvided", defaultValue = "No") String accommodationProvided,
            @RequestParam(value = "foodProvided", defaultValue = "No") String foodProvided,
            @RequestParam(value = "travelExpensesCovered", defaultValue = "No") String travelExpensesCovered,
            // 5. Audition details
            @RequestParam(value = "auditionProcess", required = false) String auditionProcess,
            @RequestParam(value = "auditionInstructions", required = false) String auditionInstructions,
            @RequestParam(value = "selfIntroVideoRequired", defaultValue = "Yes") String selfIntroVideoRequired,
            @RequestParam(value = "portfolioRequired", defaultValue = "Yes") String portfolioRequired,
            @RequestParam(value = "auditionScript", required = false) String auditionScript,
            @RequestParam(value = "lastDateToApply", required = false) String lastDateToApply,
            @RequestParam(value = "onlineAuditionEnabled", defaultValue = "true") boolean onlineAuditionEnabled,
            // 6. Application requirements
            @RequestParam(value = "requiredPhotographs", required = false) String requiredPhotographs,
            @RequestParam(value = "introVideoRequirement", required = false) String introVideoRequirement,
            @RequestParam(value = "actingPortfolioRequirement", required = false) String actingPortfolioRequirement,
            @RequestParam(value = "previousWorkLinksReq", required = false) String previousWorkLinksReq,
            @RequestParam(value = "applicantContactReq", required = false) String applicantContactReq,
            @RequestParam(value = "additionalDocumentsReq", required = false) String additionalDocumentsReq,
            // 7. Contact information
            @RequestParam(value = "castingDirectorName", required = false) String castingDirectorName,
            @RequestParam(value = "contactEmail", required = false) String contactEmail,
            @RequestParam(value = "contactNumber", required = false) String contactNumber,
            @RequestParam(value = "whatsappNumber", required = false) String whatsappNumber,
            @RequestParam(value = "websiteOrSocialLinks", required = false) String websiteOrSocialLinks,
            // 8. Media and attachments
            @RequestParam(value = "projectPoster", required = false) MultipartFile projectPoster,
            @RequestParam(value = "referenceImages", required = false) MultipartFile[] referenceImages,
            @RequestParam(value = "characterLookReferences", required = false) MultipartFile[] characterLookReferences,
            @RequestParam(value = "auditionScriptPdf", required = false) MultipartFile auditionScriptPdf,
            @RequestParam(value = "projectPromotionalImage", required = false) MultipartFile projectPromotionalImage,
            @RequestParam(value = "tags", required = false) String tags,
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
    public ResponseEntity<?> promoteProject(@PathVariable("id") Long id, HttpSession session) {
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

    @PutMapping("/projects/{id}")
    public ResponseEntity<?> updateProject(@PathVariable("id") Long id,
                                           @RequestBody Map<String, Object> body,
                                           HttpSession session) {
        CastingCompany company = authHelper.requirePaidDashboardCompany(session);
        CastingProject p = projectRepo.findByIdAndCompanyId(id, company.getId())
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

        if (body.containsKey("projectTitle") && !String.valueOf(body.get("projectTitle")).isBlank()) {
            p.setProjectTitle(String.valueOf(body.get("projectTitle")).trim());
        }
        if (body.containsKey("projectType")) p.setProjectType(String.valueOf(body.get("projectType")).trim());
        if (body.containsKey("productionHouseName")) p.setProductionHouseName(trimOrNull(String.valueOf(body.get("productionHouseName"))));
        if (body.containsKey("castingCompanyName")) p.setCastingCompanyName(trimOrNull(String.valueOf(body.get("castingCompanyName"))));
        if (body.containsKey("projectDetails") && !String.valueOf(body.get("projectDetails")).isBlank()) {
            p.setProjectDetails(String.valueOf(body.get("projectDetails")).trim());
        }
        if (body.containsKey("projectLanguage")) p.setProjectLanguage(String.valueOf(body.get("projectLanguage")).trim());
        if (body.containsKey("projectStage")) p.setProjectStage(String.valueOf(body.get("projectStage")).trim());
        if (body.containsKey("targetCategory")) p.setTargetCategory(String.valueOf(body.get("targetCategory")).trim());
        if (body.containsKey("targetGender")) p.setTargetGender(String.valueOf(body.get("targetGender")).trim());
        if (body.containsKey("ageMin") && body.get("ageMin") != null && !String.valueOf(body.get("ageMin")).isBlank()) {
            p.setAgeMin(Integer.parseInt(String.valueOf(body.get("ageMin"))));
        }
        if (body.containsKey("ageMax") && body.get("ageMax") != null && !String.valueOf(body.get("ageMax")).isBlank()) {
            p.setAgeMax(Integer.parseInt(String.valueOf(body.get("ageMax"))));
        }
        if (body.containsKey("artistsRequiredCount") && body.get("artistsRequiredCount") != null && !String.valueOf(body.get("artistsRequiredCount")).isBlank()) {
            p.setArtistsRequiredCount(Math.max(1, Integer.parseInt(String.valueOf(body.get("artistsRequiredCount")))));
        }
        if (body.containsKey("experienceLevel")) p.setExperienceLevel(String.valueOf(body.get("experienceLevel")).trim());
        if (body.containsKey("roleDescription")) p.setRoleDescription(trimOrNull(String.valueOf(body.get("roleDescription"))));
        if (body.containsKey("characterName")) p.setCharacterName(trimOrNull(String.valueOf(body.get("characterName"))));
        if (body.containsKey("location")) p.setLocation(trimOrNull(String.valueOf(body.get("location"))));
        if (body.containsKey("auditionLocation")) p.setAuditionLocation(trimOrNull(String.valueOf(body.get("auditionLocation"))));
        if (body.containsKey("auditionType")) p.setAuditionType(String.valueOf(body.get("auditionType")).trim());
        if (body.containsKey("auditionDate")) p.setAuditionDate(parseDateOrNull(String.valueOf(body.get("auditionDate"))));
        if (body.containsKey("shootingStartDate")) p.setShootingStartDate(parseDateOrNull(String.valueOf(body.get("shootingStartDate"))));
        if (body.containsKey("shootingEndDate")) p.setShootingEndDate(parseDateOrNull(String.valueOf(body.get("shootingEndDate"))));
        if (body.containsKey("lastDateToApply")) {
            LocalDate deadline = parseDateOrNull(String.valueOf(body.get("lastDateToApply")));
            p.setLastDateToApply(deadline);
            if (deadline != null) p.setExpiryDate(deadline);
        }
        if (body.containsKey("budget")) p.setBudget(trimOrNull(String.valueOf(body.get("budget"))));
        if (body.containsKey("paymentType")) p.setPaymentType(String.valueOf(body.get("paymentType")).trim());
        if (body.containsKey("paymentBasis")) p.setPaymentBasis(String.valueOf(body.get("paymentBasis")).trim());
        if (body.containsKey("auditionInstructions")) p.setAuditionInstructions(trimOrNull(String.valueOf(body.get("auditionInstructions"))));
        if (body.containsKey("status")) p.setStatus(String.valueOf(body.get("status")).trim().toUpperCase(Locale.ROOT));
        if (body.containsKey("tags")) p.setTags(trimOrNull(String.valueOf(body.get("tags"))));

        CastingProject saved = projectRepo.save(p);
        notificationService.notifyCompany(company.getId(), company.getCompanyEmail(),
                "Project Updated", "'" + saved.getProjectTitle() + "' details were updated.");
        return ResponseEntity.ok(Map.of("message", "Project updated successfully.", "project", toProjectSummary(saved, company)));
    }

    @PatchMapping("/projects/{id}/status")
    public ResponseEntity<?> updateProjectStatus(@PathVariable("id") Long id,
                                                 @RequestBody Map<String, String> body,
                                                 HttpSession session) {
        CastingCompany company = authHelper.requirePaidDashboardCompany(session);
        CastingProject p = projectRepo.findByIdAndCompanyId(id, company.getId())
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));
        String nextStatus = body.getOrDefault("status", "ACTIVE").trim().toUpperCase(Locale.ROOT);
        if (!Set.of("ACTIVE", "PAUSED", "CLOSED").contains(nextStatus)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Status must be ACTIVE, PAUSED, or CLOSED."));
        }
        p.setStatus(nextStatus);
        if ("CLOSED".equals(nextStatus)) {
            p.setProjectStage("Closed");
        } else if ("ACTIVE".equals(nextStatus) && "Closed".equalsIgnoreCase(p.getProjectStage())) {
            p.setProjectStage("Casting Open");
        }
        CastingProject saved = projectRepo.save(p);
        notificationService.notifyCompany(company.getId(), company.getCompanyEmail(),
                "Casting Status Changed", "'" + saved.getProjectTitle() + "' is now " + nextStatus + ".");
        return ResponseEntity.ok(Map.of("message", "Project status changed to " + nextStatus, "project", toProjectSummary(saved, company)));
    }

    @DeleteMapping("/projects/{id}")
    @Transactional
    public ResponseEntity<?> deleteProject(@PathVariable("id") Long id, HttpSession session) {
        CastingCompany company = authHelper.requirePaidDashboardCompany(session);
        CastingProject p = projectRepo.findByIdAndCompanyId(id, company.getId())
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));
        String title = p.getProjectTitle();
        appRepo.deleteByProjectId(id);
        auditionRepo.deleteByProjectIdAndCompanyId(id, company.getId());
        projectRepo.delete(p);
        notificationService.notifyCompany(company.getId(), company.getCompanyEmail(),
                "Project Deleted", "'" + title + "' and its applications were removed.");
        return ResponseEntity.ok(Map.of("message", "Project deleted successfully."));
    }

    @GetMapping("/applications")
    public ResponseEntity<?> viewApplications(
            @RequestParam(value = "projectId", required = false) Long projectId,
            @RequestParam(value = "tab", required = false, defaultValue = "ALL") String tab,
            @RequestParam(value = "role", required = false, defaultValue = "ALL") String role,
            @RequestParam(value = "q", required = false, defaultValue = "") String q,
            @RequestParam(value = "sort", required = false, defaultValue = "RELEVANCE") String sort,
            HttpSession session
    ) {
        CastingCompany company = authHelper.requirePaidDashboardCompany(session);
        List<CastingProject> projects = projectRepo.findByCompanyIdOrderByPostedDateDescIdDesc(company.getId());
        if (projects.isEmpty()) {
            return ResponseEntity.ok(Map.of("projects", List.of(), "applications", List.of(), "roles", List.of(), "statusCounts", Map.of()));
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
        List<String> roles = rawApps.stream().map(ProjectApplication::getRoleName).filter(Objects::nonNull).distinct().toList();
        String query = q.trim().toLowerCase(Locale.ROOT);

        Map<String, Long> statusCounts = new LinkedHashMap<>();
        statusCounts.put("ALL", (long) rawApps.size());
        statusCounts.put("APPLIED", rawApps.stream().filter(pa -> "APPLIED".equalsIgnoreCase(pa.getStatus())).count());
        statusCounts.put("SHORTLISTED", rawApps.stream().filter(pa -> "SHORTLISTED".equalsIgnoreCase(pa.getStatus())).count());
        statusCounts.put("INVITED", rawApps.stream().filter(pa -> "INVITED".equalsIgnoreCase(pa.getStatus())).count());
        statusCounts.put("SUGGESTED", rawApps.stream().filter(pa -> "SUGGESTED".equalsIgnoreCase(pa.getStatus())).count());
        statusCounts.put("SELECTED", rawApps.stream().filter(pa -> "SELECTED".equalsIgnoreCase(pa.getStatus())).count());
        statusCounts.put("REJECTED", rawApps.stream().filter(pa -> "REJECTED".equalsIgnoreCase(pa.getStatus())).count());
        statusCounts.put("BACKUPS", rawApps.stream().filter(ProjectApplication::isBackupCandidate).count());

        List<Map<String, Object>> items = rawApps.stream()
                .filter(pa -> "ALL".equalsIgnoreCase(tab)
                        || ("BACKUPS".equalsIgnoreCase(tab) && pa.isBackupCandidate())
                        || (pa.getStatus() != null && pa.getStatus().equalsIgnoreCase(tab)))
                .filter(pa -> "ALL".equalsIgnoreCase(role) || (pa.getRoleName() != null && pa.getRoleName().equalsIgnoreCase(role)))
                .map(pa -> {
                    ArtistProfile a = artistById.get(pa.getArtistId());
                    if (a == null) return null;
                    if (!query.isEmpty()
                            && !(a.getFullName() != null && a.getFullName().toLowerCase(Locale.ROOT).contains(query))
                            && !(a.getCity() != null && a.getCity().toLowerCase(Locale.ROOT).contains(query))
                            && !(a.getCategory() != null && a.getCategory().toLowerCase(Locale.ROOT).contains(query))) {
                        return null;
                    }
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("applicationId", pa.getId());
                    m.put("projectId", pa.getProjectId());
                    m.put("roleName", pa.getRoleName());
                    m.put("assignedRole", pa.getAssignedRole());
                    m.put("applicationStatus", pa.getStatus());
                    m.put("rating", pa.getRating());
                    m.put("internalComment", pa.getInternalComment());
                    m.put("backupCandidate", pa.isBackupCandidate());
                    m.put("updatedAt", pa.getUpdatedAt());
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
                "statusCounts", statusCounts,
                "applications", items
        ));
    }

    @PostMapping("/applications/{id}/status")
    public ResponseEntity<?> updateApplicationStatus(@PathVariable("id") Long id,
                                                   @RequestBody Map<String, String> body,
                                                   HttpSession session) {
        CastingCompany company = authHelper.requirePaidDashboardCompany(session);
        ProjectApplication pa = appRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));
        projectRepo.findByIdAndCompanyId(pa.getProjectId(), company.getId())
                .orElseThrow(() -> new IllegalArgumentException("Unauthorized project access"));
        String status = body.getOrDefault("status", "SHORTLISTED").toUpperCase(Locale.ROOT);
        pa.setStatus(status);
        pa.setUpdatedAt(LocalDateTime.now());
        appRepo.save(pa);
        return ResponseEntity.ok(pa);
    }

    @PostMapping("/applications/{id}/review")
    public ResponseEntity<?> reviewApplication(@PathVariable("id") Long id,
                                               @RequestBody Map<String, Object> body,
                                               HttpSession session) {
        CastingCompany company = authHelper.requirePaidDashboardCompany(session);
        ProjectApplication pa = appRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));
        CastingProject project = projectRepo.findByIdAndCompanyId(pa.getProjectId(), company.getId())
                .orElseThrow(() -> new IllegalArgumentException("Unauthorized project access"));

        if (body.containsKey("status") && body.get("status") != null && !String.valueOf(body.get("status")).isBlank()) {
            String st = String.valueOf(body.get("status")).trim().toUpperCase(Locale.ROOT);
            if (!Set.of("APPLIED", "SHORTLISTED", "INVITED", "SUGGESTED", "SELECTED", "REJECTED").contains(st)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Unsupported application status: " + st));
            }
            pa.setStatus(st);
        }
        if (body.containsKey("rating") && body.get("rating") != null && !String.valueOf(body.get("rating")).isBlank()) {
            int rating = Integer.parseInt(String.valueOf(body.get("rating")));
            pa.setRating(Math.max(1, Math.min(5, rating)));
        }
        if (body.containsKey("internalComment")) {
            Object rawComment = body.get("internalComment");
            pa.setInternalComment(rawComment == null ? null : trimOrNull(String.valueOf(rawComment)));
        }
        if (body.containsKey("assignedRole")) {
            Object rawRole = body.get("assignedRole");
            pa.setAssignedRole(rawRole == null ? null : trimOrNull(String.valueOf(rawRole)));
        }
        if (body.containsKey("backupCandidate") && body.get("backupCandidate") != null) {
            pa.setBackupCandidate(Boolean.parseBoolean(String.valueOf(body.get("backupCandidate"))));
        }
        pa.setUpdatedAt(LocalDateTime.now());
        ProjectApplication saved = appRepo.save(pa);

        ArtistProfile artist = artistRepo.findById(saved.getArtistId()).orElse(null);
        if (artist != null && ("SELECTED".equalsIgnoreCase(saved.getStatus()) || "REJECTED".equalsIgnoreCase(saved.getStatus()))) {
            notificationService.notifyCompany(company.getId(), company.getCompanyEmail(),
                    "Application " + saved.getStatus(),
                    artist.getFullName() + " marked " + saved.getStatus() + " for '" + project.getProjectTitle() + "'.");
        }

        return ResponseEntity.ok(Map.of("application", saved, "message", "Application review saved."));
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
        String skills = String.valueOf(filters.getOrDefault("skills", "")).trim();
        String keyword = String.valueOf(filters.getOrDefault("keyword", "")).trim();
        boolean videoOnly = Boolean.parseBoolean(String.valueOf(filters.getOrDefault("videoOnly", false)));
        boolean verifiedOnly = Boolean.parseBoolean(String.valueOf(filters.getOrDefault("verifiedOnly", false)));
        boolean recordHistory = Boolean.parseBoolean(String.valueOf(filters.getOrDefault("recordHistory", true)));

        if (recordHistory && (!"All".equalsIgnoreCase(category) || !city.isBlank() || !keyword.isBlank() || !skills.isBlank())) {
            SearchHistoryEntry she = new SearchHistoryEntry();
            she.setCompanyId(company.getId());
            she.setCategory("All".equalsIgnoreCase(category) ? null : category);
            she.setCity(city.isBlank() ? null : city);
            she.setKeyword(!keyword.isBlank() ? keyword : (skills.isBlank() ? null : skills));
            searchHistoryRepo.save(she);
        }

        Set<Long> unlockedIds = getUnlockedContactIds(company.getId());
        Set<Long> savedIds = getSavedArtistIds(company.getId());
        String cityLc = city.toLowerCase(Locale.ROOT);
        String langLc = language.toLowerCase(Locale.ROOT);
        String skillsLc = skills.toLowerCase(Locale.ROOT);
        String kwLc = keyword.toLowerCase(Locale.ROOT);

        List<ArtistProfile> matched = artistRepo.findAll().stream()
                .filter(a -> "All".equalsIgnoreCase(category)
                        || (a.getCategory() != null && a.getCategory().equalsIgnoreCase(category))
                        || (a.getSecondaryRoles() != null && a.getSecondaryRoles().toLowerCase(Locale.ROOT).contains(category.toLowerCase(Locale.ROOT))))
                .filter(a -> "All".equalsIgnoreCase(gender) || (a.getGender() != null && a.getGender().equalsIgnoreCase(gender)))
                .filter(a -> "All".equalsIgnoreCase(agency) || (a.getAgencyName() != null && a.getAgencyName().equalsIgnoreCase(agency)))
                .filter(a -> a.getAge() >= ageMin && a.getAge() <= ageMax)
                .filter(a -> cityLc.isEmpty() || "any".equals(cityLc) || (a.getCity() != null && a.getCity().toLowerCase(Locale.ROOT).contains(cityLc)))
                .filter(a -> "All".equalsIgnoreCase(expertise) || (a.getExpertise() != null && a.getExpertise().equalsIgnoreCase(expertise)))
                .filter(a -> langLc.isEmpty() || (a.getLanguages() != null && a.getLanguages().toLowerCase(Locale.ROOT).contains(langLc)))
                .filter(a -> skillsLc.isEmpty()
                        || (a.getSkills() != null && a.getSkills().toLowerCase(Locale.ROOT).contains(skillsLc))
                        || (a.getKeywords() != null && a.getKeywords().toLowerCase(Locale.ROOT).contains(skillsLc)))
                .filter(a -> !videoOnly || a.isHasVideo())
                .filter(a -> !verifiedOnly || a.isProfileVerified())
                .filter(a -> kwLc.isEmpty()
                        || (a.getFullName() != null && a.getFullName().toLowerCase(Locale.ROOT).contains(kwLc))
                        || (a.getSkills() != null && a.getSkills().toLowerCase(Locale.ROOT).contains(kwLc))
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

    @PostMapping("/artists/{artistId}/verify")
    public ResponseEntity<?> verifyArtistProfile(@PathVariable("artistId") Long artistId,
                                                 @RequestBody Map<String, String> body,
                                                 HttpSession session) {
        CastingCompany company = authHelper.requirePaidDashboardCompany(session);
        ArtistProfile artist = artistRepo.findById(artistId)
                .orElseThrow(() -> new IllegalArgumentException("Artist not found"));

        String status = body.getOrDefault("status", "Verified").trim();
        if (!Set.of("Verified", "Rejected", "Pending").contains(status)) {
            status = "Verified".equalsIgnoreCase(status) ? "Verified"
                    : ("Rejected".equalsIgnoreCase(status) ? "Rejected" : "Pending");
        }
        artist.setVerificationStatus(status);
        artist.setProfileVerified("Verified".equals(status));
        if (body.containsKey("notes")) {
            artist.setVerificationNotes(trimOrNull(body.get("notes")));
        }
        ArtistProfile saved = artistRepo.save(artist);
        notificationService.notifyCompany(company.getId(), company.getCompanyEmail(),
                "Actor Verification Updated",
                saved.getFullName() + " profile marked as " + saved.getVerificationStatus() + ".");

        boolean unlocked = getUnlockedContactIds(company.getId()).contains(saved.getId());
        boolean savedFlag = getSavedArtistIds(company.getId()).contains(saved.getId());
        return ResponseEntity.ok(Map.of(
                "artist", toArtistView(saved, unlocked, savedFlag),
                "message", saved.getFullName() + " verification set to " + saved.getVerificationStatus()
        ));
    }

    @GetMapping("/artists/recommended")
    public ResponseEntity<?> getRecommendedArtists(@RequestParam(value = "tier", defaultValue = "PLATINUM") String tier,
                                                   @RequestParam(value = "projectId", required = false) Long projectId,
                                                   HttpSession session) {
        CastingCompany company = authHelper.requirePaidDashboardCompany(session);
        Set<Long> unlockedIds = getUnlockedContactIds(company.getId());
        Set<Long> savedIds = getSavedArtistIds(company.getId());

        List<SearchHistoryEntry> history = searchHistoryRepo.findTop10ByCompanyIdOrderBySearchedAtDesc(company.getId());
        Set<String> preferredCats = history.stream()
                .map(SearchHistoryEntry::getCategory)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<CastingProject> projects = projectRepo.findByCompanyIdOrderByPostedDateDescIdDesc(company.getId());
        CastingProject refProject = projectId != null
                ? projectRepo.findByIdAndCompanyId(projectId, company.getId()).orElse(projects.isEmpty() ? null : projects.get(0))
                : (projects.isEmpty() ? null : projects.get(0));

        List<ArtistProfile> all = artistRepo.findAll();
        List<ArtistProfile> candidates;
        if ("AI_MATCH".equalsIgnoreCase(tier)) {
            candidates = all;
        } else {
            candidates = all.stream()
                    .filter(a -> "FEATURED".equalsIgnoreCase(tier) ? a.isFeaturedTier() : a.isPlatinumTier())
                    .toList();
            if (candidates.isEmpty()) candidates = all.stream().limit(8).toList();
        }

        record ScoredArtist(ArtistProfile artist, int score, List<String> reasons) {}
        List<ScoredArtist> scored = candidates.stream().map(a -> {
            int score = 56;
            List<String> reasons = new ArrayList<>();
            if (refProject != null) {
                if (refProject.getTargetCategory() != null && (refProject.getTargetCategory().equalsIgnoreCase(a.getCategory())
                        || (a.getSecondaryRoles() != null && a.getSecondaryRoles().toLowerCase(Locale.ROOT).contains(refProject.getTargetCategory().toLowerCase(Locale.ROOT))))) {
                    score += 18;
                    reasons.add("Role match: " + refProject.getTargetCategory());
                }
                if (refProject.getTargetGender() != null && ("Any".equalsIgnoreCase(refProject.getTargetGender()) || refProject.getTargetGender().equalsIgnoreCase(a.getGender()))) {
                    score += 5;
                }
                if (a.getAge() >= refProject.getAgeMin() && a.getAge() <= refProject.getAgeMax()) {
                    score += 8;
                    reasons.add("Age " + a.getAge() + " fits (" + refProject.getAgeMin() + "-" + refProject.getAgeMax() + ")");
                }
                if (refProject.getProjectLanguage() != null && a.getLanguages() != null && a.getLanguages().toLowerCase(Locale.ROOT).contains(refProject.getProjectLanguage().toLowerCase(Locale.ROOT))) {
                    score += 7;
                    reasons.add("Speaks " + refProject.getProjectLanguage());
                }
                if (refProject.getLocation() != null && a.getCity() != null && refProject.getLocation().toLowerCase(Locale.ROOT).contains(a.getCity().toLowerCase(Locale.ROOT))) {
                    score += 5;
                    reasons.add("Based in " + a.getCity());
                }
            }
            if (preferredCats.contains(a.getCategory())) {
                score += 6;
                reasons.add("Recent search match");
            }
            if (a.isProfileVerified()) {
                score += 4;
                reasons.add("Verified profile");
            }
            if (a.isHasVideo()) {
                score += 3;
                reasons.add("Has showreel video");
            }
            return new ScoredArtist(a, Math.min(99, score), reasons);
        }).sorted((x, y) -> {
            int cmp = Integer.compare(y.score(), x.score());
            return cmp != 0 ? cmp : Integer.compare(y.artist().getViewsCount(), x.artist().getViewsCount());
        }).toList();

        ArtistProfile rising = all.stream().filter(ArtistProfile::isRisingStar).findFirst().orElse(all.get(0));
        List<Map<String, Object>> artistViews = scored.stream().map(sa -> {
            Map<String, Object> v = toArtistView(sa.artist(), unlockedIds.contains(sa.artist().getId()), savedIds.contains(sa.artist().getId()));
            v.put("matchScore", sa.score());
            v.put("matchReasons", sa.reasons());
            return v;
        }).toList();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("tier", tier);
        result.put("risingStar", toArtistView(rising, unlockedIds.contains(rising.getId()), savedIds.contains(rising.getId())));
        result.put("searchSignals", history);
        result.put("projects", projects.stream().map(p -> Map.of("id", p.getId(), "projectTitle", p.getProjectTitle())).toList());
        result.put("activeProjectId", refProject != null ? refProject.getId() : null);
        result.put("artists", artistViews);
        return ResponseEntity.ok(result);
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
    public ResponseEntity<byte[]> exportExcel(@RequestParam(value = "ids", required = false) String ids, HttpSession session) {
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
    public ResponseEntity<?> viewPortfolio(@PathVariable("artistId") Long artistId, HttpSession session) {
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
    public ResponseEntity<?> unlockContact(@PathVariable("artistId") Long artistId, HttpSession session) {
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
            @RequestParam(value = "folderId", required = false) Long folderId,
            @RequestParam(value = "category", required = false, defaultValue = "All") String category,
            @RequestParam(value = "city", required = false, defaultValue = "") String city,
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
                .filter(a -> "All".equalsIgnoreCase(category) || (a.getCategory() != null && a.getCategory().equalsIgnoreCase(category)))
                .filter(a -> cityLc.isEmpty() || (a.getCity() != null && a.getCity().toLowerCase(Locale.ROOT).contains(cityLc)))
                .map(a -> toArtistView(a, unlockedIds.contains(a.getId()), true))
                .toList();

        List<Map<String, Object>> folderViews = folders.stream().map(f -> Map.<String, Object>of(
                "id", f.getId(),
                "folderName", f.getFolderName() != null ? f.getFolderName() : "Saved",
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
    public ResponseEntity<?> deleteFolder(@PathVariable("id") Long id, HttpSession session) {
        CastingCompany company = authHelper.requirePaidDashboardCompany(session);
        ArtistFolder folder = folderRepo.findByIdAndCompanyId(id, company.getId())
                .orElseThrow(() -> new IllegalArgumentException("Folder not found"));
        folderItemRepo.deleteByCompanyIdAndFolderId(company.getId(), folder.getId());
        folderRepo.delete(folder);
        return ResponseEntity.ok(Map.of("message", "Folder deleted"));
    }

    @PostMapping("/folders/toggle/{artistId}")
    @Transactional
    public ResponseEntity<?> toggleArtistSave(@PathVariable("artistId") Long artistId,
                                              @RequestParam(value = "folderId", required = false) Long folderId,
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
            @RequestParam(value = "viewType", defaultValue = "CONTACT_VIEW") String viewType,
            @RequestParam(value = "tier", defaultValue = "PAID") String tier,
            @RequestParam(value = "sort", defaultValue = "RECENT") String sort,
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
        m.put("profileVerified", a.isProfileVerified());
        m.put("verificationStatus", a.getVerificationStatus());
        m.put("verificationNotes", a.getVerificationNotes());
        m.put("portfolioUrl", a.getPortfolioUrl());
        m.put("skills", a.getSkills());
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
