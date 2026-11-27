package in.sp.main.controller;

import in.sp.main.entity.*;
import in.sp.main.repository.*;
import in.sp.main.service.FileStorageService;
import in.sp.main.service.NotificationService;
import in.sp.main.service.SessionAuthHelper;
import in.sp.main.service.SimplePdfExportService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Operational modules for the Casting Company Dashboard:
 * Audition Scheduling & Reviews, Side-by-Side Comparison, Shortlist/Selection/Backup Pipeline,
 * Artist Messaging & Invitations, Analytics & PDF/Excel Reporting, Team Access Management,
 * Contracts, and Production Expense Tracking.
 */
@RestController
@RequestMapping("/api/dashboard/ops")
public class CastingOperationsController {

    private final CastingProjectRepository projectRepo;
    private final ProjectApplicationRepository appRepo;
    private final ArtistProfileRepository artistRepo;
    private final AuditionScheduleRepository auditionRepo;
    private final CommunicationMessageRepository messageRepo;
    private final CastingTeamMemberRepository teamRepo;
    private final ArtistContractRepository contractRepo;
    private final ProductionExpenseRepository expenseRepo;
    private final SubscriptionPaymentRepository paymentRepo;
    private final ContactPortfolioHistoryRepository historyRepo;
    private final ArtistFolderItemRepository folderItemRepo;
    private final CompanyNotificationRepository notificationRepo;
    private final FileStorageService fileStorage;
    private final NotificationService notificationService;
    private final SimplePdfExportService pdfExportService;
    private final SessionAuthHelper authHelper;

    public CastingOperationsController(CastingProjectRepository projectRepo,
                                       ProjectApplicationRepository appRepo,
                                       ArtistProfileRepository artistRepo,
                                       AuditionScheduleRepository auditionRepo,
                                       CommunicationMessageRepository messageRepo,
                                       CastingTeamMemberRepository teamRepo,
                                       ArtistContractRepository contractRepo,
                                       ProductionExpenseRepository expenseRepo,
                                       SubscriptionPaymentRepository paymentRepo,
                                       ContactPortfolioHistoryRepository historyRepo,
                                       ArtistFolderItemRepository folderItemRepo,
                                       CompanyNotificationRepository notificationRepo,
                                       FileStorageService fileStorage,
                                       NotificationService notificationService,
                                       SimplePdfExportService pdfExportService,
                                       SessionAuthHelper authHelper) {
        this.projectRepo = projectRepo;
        this.appRepo = appRepo;
        this.artistRepo = artistRepo;
        this.auditionRepo = auditionRepo;
        this.messageRepo = messageRepo;
        this.teamRepo = teamRepo;
        this.contractRepo = contractRepo;
        this.expenseRepo = expenseRepo;
        this.paymentRepo = paymentRepo;
        this.historyRepo = historyRepo;
        this.folderItemRepo = folderItemRepo;
        this.notificationRepo = notificationRepo;
        this.fileStorage = fileStorage;
        this.notificationService = notificationService;
        this.pdfExportService = pdfExportService;
        this.authHelper = authHelper;
    }

    // ==================== 1. AUDITION MANAGEMENT ====================

    @GetMapping("/auditions")
    public ResponseEntity<?> listAuditions(
            @RequestParam(value = "projectId", required = false) Long projectId,
            @RequestParam(value = "status", required = false, defaultValue = "ALL") String status,
            @RequestParam(value = "mode", required = false, defaultValue = "ALL") String mode,
            HttpSession session
    ) {
        CastingCompany company = authHelper.requirePaidDashboardCompany(session);
        List<CastingProject> projects = projectRepo.findByCompanyIdOrderByPostedDateDescIdDesc(company.getId());
        Map<Long, CastingProject> projectById = projects.stream()
                .collect(Collectors.toMap(CastingProject::getId, p -> p));
        Map<Long, ArtistProfile> artistById = artistRepo.findAll().stream()
                .collect(Collectors.toMap(ArtistProfile::getId, a -> a));

        List<AuditionSchedule> all = auditionRepo.findByCompanyIdOrderByAuditionDateAscIdDesc(company.getId());
        List<Map<String, Object>> filtered = all.stream()
                .filter(au -> projectId == null || projectId.equals(au.getProjectId()))
                .filter(au -> "ALL".equalsIgnoreCase(status) || status.equalsIgnoreCase(au.getStatus()))
                .filter(au -> "ALL".equalsIgnoreCase(mode) || mode.equalsIgnoreCase(au.getAuditionMode()))
                .map(au -> toAuditionMap(au, projectById.get(au.getProjectId()), artistById.get(au.getArtistId())))
                .toList();

        long scheduledCount = all.stream().filter(a -> "SCHEDULED".equalsIgnoreCase(a.getStatus()) || "RESCHEDULED".equalsIgnoreCase(a.getStatus())).count();
        long completedCount = all.stream().filter(a -> "COMPLETED".equalsIgnoreCase(a.getStatus())).count();
        long cancelledCount = all.stream().filter(a -> "CANCELLED".equalsIgnoreCase(a.getStatus())).count();
        long onlineCount = all.stream().filter(a -> "Online".equalsIgnoreCase(a.getAuditionMode())).count();

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("total", all.size());
        stats.put("scheduled", scheduledCount);
        stats.put("completed", completedCount);
        stats.put("cancelled", cancelledCount);
        stats.put("online", onlineCount);
        stats.put("offline", all.size() - onlineCount);

        return ResponseEntity.ok(Map.of(
                "auditions", filtered,
                "stats", stats,
                "projects", projects.stream().map(this::toCompactProject).toList(),
                "artists", artistRepo.findAll().stream().map(this::toCompactArtist).toList()
        ));
    }

    @PostMapping("/auditions")
    public ResponseEntity<?> scheduleAudition(@RequestBody Map<String, Object> body, HttpSession session) {
        CastingCompany company = authHelper.requirePaidDashboardCompany(session);
        Long projectId = parseLong(body.get("projectId"));
        Long artistId = parseLong(body.get("artistId"));
        if (projectId == null || artistId == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Project and Artist are required."));
        }
        CastingProject project = projectRepo.findByIdAndCompanyId(projectId, company.getId())
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));
        ArtistProfile artist = artistRepo.findById(artistId)
                .orElseThrow(() -> new IllegalArgumentException("Artist not found"));

        AuditionSchedule au = new AuditionSchedule();
        au.setCompanyId(company.getId());
        au.setProjectId(project.getId());
        au.setArtistId(artist.getId());
        au.setApplicationId(parseLong(body.get("applicationId")));
        String roleName = str(body.get("roleName"), project.getTargetCategory());
        au.setRoleName(roleName);
        au.setAuditionMode(str(body.get("auditionMode"), "Online"));
        au.setAuditionDate(parseDate(body.get("auditionDate"), LocalDate.now().plusDays(2)));
        au.setAuditionTime(str(body.get("auditionTime"), "11:00 AM"));
        au.setLocationOrLink(str(body.get("locationOrLink"),
                "Online".equalsIgnoreCase(au.getAuditionMode())
                        ? "https://meet.entertainmentworld.in/room-" + (100 + artist.getId())
                        : (project.getAuditionLocation() != null ? project.getAuditionLocation() : company.getCompanyAddress())));
        String inviteMsg = str(body.get("invitationMessage"),
                "You are invited for an audition for '" + project.getProjectTitle() + "' (" + roleName + ") on "
                        + au.getAuditionDate() + " at " + au.getAuditionTime() + ".");
        au.setInvitationMessage(inviteMsg);
        au.setInvitationSent(true);
        au.setAuditionVideoUrl(str(body.get("auditionVideoUrl"),
                artist.getVideoUrl() != null ? artist.getVideoUrl() : "/assets/video/audition-1.mp4"));
        au.setStatus("SCHEDULED");
        au.setCreatedAt(LocalDateTime.now());
        AuditionSchedule saved = auditionRepo.save(au);

        // Update matching application status to INVITED if still APPLIED / SUGGESTED
        appRepo.findByProjectIdAndArtistId(project.getId(), artist.getId()).ifPresent(pa -> {
            if ("APPLIED".equalsIgnoreCase(pa.getStatus()) || "SUGGESTED".equalsIgnoreCase(pa.getStatus())) {
                pa.setStatus("INVITED");
                pa.setUpdatedAt(LocalDateTime.now());
                appRepo.save(pa);
            }
        });

        // Log communication message
        CommunicationMessage msg = new CommunicationMessage();
        msg.setCompanyId(company.getId());
        msg.setArtistId(artist.getId());
        msg.setProjectId(project.getId());
        msg.setChannel("AUDITION_INVITE");
        msg.setSubject("Audition Invitation: " + project.getProjectTitle());
        msg.setMessageBody(inviteMsg + " Venue/Link: " + au.getLocationOrLink());
        msg.setSenderName(company.getRepresentativeName() + " (" + company.getCompanyName() + ")");
        messageRepo.save(msg);

        notificationService.notifyCompany(company.getId(), company.getCompanyEmail(),
                "Audition Scheduled",
                "Scheduled " + au.getAuditionMode() + " audition for " + artist.getFullName() + " on " + au.getAuditionDate() + ".");

        return ResponseEntity.status(HttpStatus.CREATED).body(toAuditionMap(saved, project, artist));
    }

    @PutMapping("/auditions/{id}")
    public ResponseEntity<?> updateAudition(@PathVariable("id") Long id,
                                            @RequestBody Map<String, Object> body,
                                            HttpSession session) {
        CastingCompany company = authHelper.requirePaidDashboardCompany(session);
        AuditionSchedule au = auditionRepo.findByIdAndCompanyId(id, company.getId())
                .orElseThrow(() -> new IllegalArgumentException("Audition not found"));

        if (body.containsKey("auditionMode")) au.setAuditionMode(str(body.get("auditionMode"), au.getAuditionMode()));
        if (body.containsKey("auditionDate")) au.setAuditionDate(parseDate(body.get("auditionDate"), au.getAuditionDate()));
        if (body.containsKey("auditionTime")) au.setAuditionTime(str(body.get("auditionTime"), au.getAuditionTime()));
        if (body.containsKey("locationOrLink")) au.setLocationOrLink(str(body.get("locationOrLink"), au.getLocationOrLink()));
        if (body.containsKey("status")) au.setStatus(str(body.get("status"), au.getStatus()).toUpperCase(Locale.ROOT));
        if (body.containsKey("rating") && body.get("rating") != null && !String.valueOf(body.get("rating")).isBlank()) {
            int r = Integer.parseInt(String.valueOf(body.get("rating")));
            au.setRating(Math.max(1, Math.min(5, r)));
        }
        if (body.containsKey("feedback")) au.setFeedback(str(body.get("feedback"), null));
        if (body.containsKey("auditionVideoUrl")) au.setAuditionVideoUrl(str(body.get("auditionVideoUrl"), au.getAuditionVideoUrl()));
        au.setUpdatedAt(LocalDateTime.now());
        AuditionSchedule saved = auditionRepo.save(au);

        CastingProject project = projectRepo.findById(saved.getProjectId()).orElse(null);
        ArtistProfile artist = artistRepo.findById(saved.getArtistId()).orElse(null);
        if (artist != null && ("RESCHEDULED".equalsIgnoreCase(saved.getStatus()) || "CANCELLED".equalsIgnoreCase(saved.getStatus()))) {
            notificationService.notifyCompany(company.getId(), company.getCompanyEmail(),
                    "Audition " + saved.getStatus(),
                    artist.getFullName() + " audition (" + (project != null ? project.getProjectTitle() : "Project")
                            + ") marked " + saved.getStatus() + " for " + saved.getAuditionDate() + " " + saved.getAuditionTime() + ".");
        }
        return ResponseEntity.ok(toAuditionMap(saved, project, artist));
    }

    @DeleteMapping("/auditions/{id}")
    public ResponseEntity<?> deleteAudition(@PathVariable("id") Long id, HttpSession session) {
        CastingCompany company = authHelper.requirePaidDashboardCompany(session);
        AuditionSchedule au = auditionRepo.findByIdAndCompanyId(id, company.getId())
                .orElseThrow(() -> new IllegalArgumentException("Audition not found"));
        auditionRepo.delete(au);
        return ResponseEntity.ok(Map.of("message", "Audition schedule deleted"));
    }

    // ==================== 2. COMPARE & SELECTION PIPELINE ====================

    @GetMapping("/compare")
    public ResponseEntity<?> compareArtists(@RequestParam(value = "artistIds", required = false) String artistIds, HttpSession session) {
        CastingCompany company = authHelper.requirePaidDashboardCompany(session);
        Set<Long> unlockedIds = getUnlockedContactIds(company.getId());
        Set<Long> savedIds = getSavedArtistIds(company.getId());
        List<CastingProject> projects = projectRepo.findByCompanyIdOrderByPostedDateDescIdDesc(company.getId());
        List<Long> projectIds = projects.stream().map(CastingProject::getId).toList();
        List<ProjectApplication> companyApps = projectIds.isEmpty() ? List.of() : appRepo.findByProjectIdInOrderByAppliedAtDesc(projectIds);
        List<AuditionSchedule> companyAuditions = auditionRepo.findByCompanyIdOrderByAuditionDateAscIdDesc(company.getId());

        List<Long> requestedIds = (artistIds == null || artistIds.isBlank()) ? List.of() :
                Arrays.stream(artistIds.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .map(Long::valueOf)
                        .limit(4)
                        .toList();

        List<ArtistProfile> selectedArtists;
        if (!requestedIds.isEmpty()) {
            selectedArtists = requestedIds.stream()
                    .map(artistRepo::findById)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .toList();
        } else {
            selectedArtists = artistRepo.findAll().stream().limit(3).toList();
        }

        List<Map<String, Object>> comparisonCards = selectedArtists.stream().map(a -> {
            List<ProjectApplication> artistApps = companyApps.stream().filter(pa -> a.getId().equals(pa.getArtistId())).toList();
            List<AuditionSchedule> artistAuds = companyAuditions.stream().filter(au -> a.getId().equals(au.getArtistId())).toList();

            List<Integer> ratings = new ArrayList<>();
            artistApps.forEach(pa -> { if (pa.getRating() != null) ratings.add(pa.getRating()); });
            artistAuds.forEach(au -> { if (au.getRating() != null) ratings.add(au.getRating()); });
            Double avgRating = ratings.isEmpty() ? null :
                    Math.round(ratings.stream().mapToInt(Integer::intValue).average().orElse(0.0) * 10.0) / 10.0;

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
            m.put("skills", a.getSkills());
            m.put("bio", a.getBio());
            m.put("avatarUrl", a.getAvatarUrl());
            m.put("portfolioUrl", a.getPortfolioUrl());
            m.put("hasVideo", a.isHasVideo());
            m.put("videoTitle", a.getVideoTitle());
            m.put("videoUrl", a.getVideoUrl());
            m.put("showcaseBadge", a.isShowcaseBadge());
            m.put("platinumTier", a.isPlatinumTier());
            m.put("featuredTier", a.isFeaturedTier());
            m.put("profileVerified", a.isProfileVerified());
            m.put("verificationStatus", a.getVerificationStatus());
            m.put("viewsCount", a.getViewsCount());
            m.put("applicationsCount", artistApps.size());
            m.put("auditionsCount", artistAuds.size());
            m.put("avgRating", avgRating);
            m.put("latestStatus", artistApps.isEmpty() ? "NOT_APPLIED" : artistApps.get(0).getStatus());
            m.put("contactUnlocked", unlockedIds.contains(a.getId()));
            m.put("saved", savedIds.contains(a.getId()));
            m.put("email", unlockedIds.contains(a.getId()) ? a.getEmail() : maskEmail(a.getEmail()));
            m.put("phone", unlockedIds.contains(a.getId()) ? a.getPhone() : maskPhone(a.getPhone()));
            return m;
        }).toList();

        return ResponseEntity.ok(Map.of(
                "compared", comparisonCards,
                "availableArtists", artistRepo.findAll().stream().map(this::toCompactArtist).toList()
        ));
    }

    @GetMapping("/selection/summary")
    public ResponseEntity<?> getSelectionSummary(@RequestParam(value = "projectId", required = false) Long projectId, HttpSession session) {
        CastingCompany company = authHelper.requirePaidDashboardCompany(session);
        List<CastingProject> projects = projectRepo.findByCompanyIdOrderByPostedDateDescIdDesc(company.getId());
        Map<Long, CastingProject> projectById = projects.stream()
                .collect(Collectors.toMap(CastingProject::getId, p -> p));
        Map<Long, ArtistProfile> artistById = artistRepo.findAll().stream()
                .collect(Collectors.toMap(ArtistProfile::getId, a -> a));
        Set<Long> unlockedIds = getUnlockedContactIds(company.getId());
        Set<Long> savedIds = getSavedArtistIds(company.getId());

        List<Long> targetProjectIds = (projectId != null)
                ? (projectById.containsKey(projectId) ? List.of(projectId) : List.of())
                : projects.stream().map(CastingProject::getId).toList();

        List<ProjectApplication> apps = targetProjectIds.isEmpty() ? List.of() : appRepo.findByProjectIdInOrderByAppliedAtDesc(targetProjectIds);
        List<AuditionSchedule> auds = auditionRepo.findByCompanyIdOrderByAuditionDateAscIdDesc(company.getId());

        List<Map<String, Object>> shortlisted = new ArrayList<>();
        List<Map<String, Object>> selected = new ArrayList<>();
        List<Map<String, Object>> backups = new ArrayList<>();
        List<Map<String, Object>> invited = new ArrayList<>();
        List<Map<String, Object>> rejected = new ArrayList<>();

        for (ProjectApplication pa : apps) {
            ArtistProfile a = artistById.get(pa.getArtistId());
            CastingProject p = projectById.get(pa.getProjectId());
            if (a == null || p == null) continue;

            AuditionSchedule latestAud = auds.stream()
                    .filter(au -> p.getId().equals(au.getProjectId()) && a.getId().equals(au.getArtistId()))
                    .findFirst().orElse(null);

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("applicationId", pa.getId());
            item.put("projectId", p.getId());
            item.put("projectTitle", p.getProjectTitle());
            item.put("roleName", pa.getRoleName());
            item.put("assignedRole", pa.getAssignedRole() != null ? pa.getAssignedRole() : pa.getRoleName());
            item.put("status", pa.getStatus());
            item.put("rating", pa.getRating());
            item.put("internalComment", pa.getInternalComment());
            item.put("backupCandidate", pa.isBackupCandidate());
            item.put("auditionVideoUrl", pa.getAuditionVideoUrl());
            item.put("auditionDate", latestAud != null ? latestAud.getAuditionDate() : null);
            item.put("auditionStatus", latestAud != null ? latestAud.getStatus() : null);
            item.put("appliedAt", pa.getAppliedAt());
            item.put("artistId", a.getId());
            item.put("artistName", a.getFullName());
            item.put("artistCategory", a.getCategory());
            item.put("artistCity", a.getCity());
            item.put("artistAge", a.getAge());
            item.put("artistGender", a.getGender());
            item.put("artistAvatar", a.getAvatarUrl());
            item.put("artistVideoUrl", a.getVideoUrl());
            item.put("profileVerified", a.isProfileVerified());
            item.put("contactUnlocked", unlockedIds.contains(a.getId()));
            item.put("saved", savedIds.contains(a.getId()));

            if (pa.isBackupCandidate()) {
                backups.add(item);
            }
            String st = pa.getStatus() == null ? "" : pa.getStatus().toUpperCase(Locale.ROOT);
            switch (st) {
                case "SELECTED" -> { if (!pa.isBackupCandidate()) selected.add(item); }
                case "SHORTLISTED" -> shortlisted.add(item);
                case "INVITED" -> invited.add(item);
                case "REJECTED" -> rejected.add(item);
                default -> { }
            }
        }

        return ResponseEntity.ok(Map.of(
                "projects", projects.stream().map(this::toCompactProject).toList(),
                "shortlisted", shortlisted,
                "selected", selected,
                "backups", backups,
                "invited", invited,
                "rejected", rejected
        ));
    }

    // ==================== 3. COMMUNICATION & NOTIFICATIONS ====================

    @GetMapping("/messages")
    public ResponseEntity<?> listMessages(
            @RequestParam(value = "channel", required = false, defaultValue = "ALL") String channel,
            @RequestParam(value = "artistId", required = false) Long artistId,
            HttpSession session
    ) {
        CastingCompany company = authHelper.requirePaidDashboardCompany(session);
        Map<Long, ArtistProfile> artistById = artistRepo.findAll().stream()
                .collect(Collectors.toMap(ArtistProfile::getId, a -> a));
        Map<Long, CastingProject> projectById = projectRepo.findByCompanyIdOrderByPostedDateDescIdDesc(company.getId())
                .stream().collect(Collectors.toMap(CastingProject::getId, p -> p));

        List<CommunicationMessage> raw = artistId != null
                ? messageRepo.findByCompanyIdAndArtistIdOrderBySentAtDesc(company.getId(), artistId)
                : messageRepo.findByCompanyIdOrderBySentAtDesc(company.getId());

        List<Map<String, Object>> messages = raw.stream()
                .filter(m -> "ALL".equalsIgnoreCase(channel) || channel.equalsIgnoreCase(m.getChannel()))
                .map(m -> {
                    ArtistProfile a = artistById.get(m.getArtistId());
                    CastingProject p = m.getProjectId() != null ? projectById.get(m.getProjectId()) : null;
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("id", m.getId());
                    row.put("artistId", m.getArtistId());
                    row.put("artistName", a != null ? a.getFullName() : "Artist #" + m.getArtistId());
                    row.put("artistCategory", a != null ? a.getCategory() : "");
                    row.put("artistAvatar", a != null ? a.getAvatarUrl() : "/assets/images/cat-actor.jpg");
                    row.put("projectId", m.getProjectId());
                    row.put("projectTitle", p != null ? p.getProjectTitle() : "General Inquiry");
                    row.put("channel", m.getChannel());
                    row.put("subject", m.getSubject());
                    row.put("messageBody", m.getMessageBody());
                    row.put("senderName", m.getSenderName());
                    row.put("readByRecipient", m.isReadByRecipient());
                    row.put("sentAt", m.getSentAt());
                    return row;
                }).toList();

        long unreadNotifs = notificationRepo.countByCompanyIdAndReadFlagFalse(company.getId());
        return ResponseEntity.ok(Map.of(
                "messages", messages,
                "unreadNotifications", unreadNotifs,
                "notifications", notificationRepo.findByCompanyIdOrderByCreatedAtDesc(company.getId()).stream().limit(20).toList(),
                "artists", artistRepo.findAll().stream().map(this::toCompactArtist).toList(),
                "projects", projectById.values().stream().map(this::toCompactProject).toList()
        ));
    }

    @PostMapping("/messages")
    public ResponseEntity<?> sendMessage(@RequestBody Map<String, Object> body, HttpSession session) {
        CastingCompany company = authHelper.requirePaidDashboardCompany(session);
        Long artistId = parseLong(body.get("artistId"));
        if (artistId == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Artist is required."));
        }
        ArtistProfile artist = artistRepo.findById(artistId)
                .orElseThrow(() -> new IllegalArgumentException("Artist not found"));

        Long projectId = parseLong(body.get("projectId"));
        if (projectId != null) {
            projectRepo.findByIdAndCompanyId(projectId, company.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Project not found"));
        }

        String channel = str(body.get("channel"), "PORTAL_MESSAGE").toUpperCase(Locale.ROOT);
        String subject = str(body.get("subject"), "");
        String msgBody = str(body.get("messageBody"), "");
        if (subject.isBlank() || msgBody.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Subject and message body are required."));
        }

        CommunicationMessage m = new CommunicationMessage();
        m.setCompanyId(company.getId());
        m.setArtistId(artist.getId());
        m.setProjectId(projectId);
        m.setChannel(channel);
        m.setSubject(subject);
        m.setMessageBody(msgBody);
        m.setSenderName(company.getRepresentativeName() + " (" + company.getCompanyName() + ")");
        m.setSentAt(LocalDateTime.now());
        CommunicationMessage saved = messageRepo.save(m);

        notificationService.notifyCompany(company.getId(), company.getCompanyEmail(),
                "Message Sent to " + artist.getFullName(),
                "[" + channel + "] " + subject);

        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // ==================== 4. REPORTS & ANALYTICS (JSON / EXCEL / PDF) ====================

    @GetMapping("/reports")
    public ResponseEntity<?> getReportsAnalytics(HttpSession session) {
        CastingCompany company = authHelper.requirePaidDashboardCompany(session);
        return ResponseEntity.ok(buildAnalyticsPayload(company));
    }

    @GetMapping("/reports/export-excel")
    public ResponseEntity<byte[]> exportReportsExcel(HttpSession session) {
        CastingCompany company = authHelper.requirePaidDashboardCompany(session);
        Map<String, Object> report = buildAnalyticsPayload(company);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> projectRows = (List<Map<String, Object>>) report.get("projectReport");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> selectionRows = (List<Map<String, Object>>) report.get("selectionReport");
        @SuppressWarnings("unchecked")
        Map<String, Object> actorRep = (Map<String, Object>) report.get("actorReport");
        @SuppressWarnings("unchecked")
        Map<String, Object> audRep = (Map<String, Object>) report.get("auditionReport");
        @SuppressWarnings("unchecked")
        Map<String, Object> finRep = (Map<String, Object>) report.get("financialSummary");

        StringBuilder sb = new StringBuilder("\uFEFF");
        sb.append("ENTERTAINMENT WORLD - CASTING ANALYTICS REPORT\t").append(cleanCell(company.getCompanyName())).append('\n');
        sb.append("Generated On\t").append(LocalDate.now()).append('\n').append('\n');

        sb.append("ACTOR DATABASE SUMMARY\n");
        sb.append("Total Registered Actors\tVerified Actors\tPending Verification\tWith Video Showreel\n");
        sb.append(actorRep.get("totalActors")).append('\t')
          .append(actorRep.get("verifiedActors")).append('\t')
          .append(actorRep.get("pendingVerification")).append('\t')
          .append(actorRep.get("withVideo")).append('\n').append('\n');

        sb.append("PROJECT-WISE APPLICATION REPORT\n");
        sb.append("Project Title\tStatus\tCategory\tTotal Applications\tApplied\tShortlisted\tInvited\tSelected\tBackups\tRejected\n");
        for (Map<String, Object> pr : projectRows) {
            sb.append(cleanCell(String.valueOf(pr.get("projectTitle")))).append('\t')
              .append(cleanCell(String.valueOf(pr.get("status")))).append('\t')
              .append(cleanCell(String.valueOf(pr.get("targetCategory")))).append('\t')
              .append(pr.get("totalApplications")).append('\t')
              .append(pr.get("applied")).append('\t')
              .append(pr.get("shortlisted")).append('\t')
              .append(pr.get("invited")).append('\t')
              .append(pr.get("selected")).append('\t')
              .append(pr.get("backups")).append('\t')
              .append(pr.get("rejected")).append('\n');
        }
        sb.append('\n');

        sb.append("AUDITION STATISTICS\n");
        sb.append("Total Auditions\tScheduled\tCompleted\tCancelled\tOnline\tOffline\tAverage Rating\n");
        sb.append(audRep.get("total")).append('\t')
          .append(audRep.get("scheduled")).append('\t')
          .append(audRep.get("completed")).append('\t')
          .append(audRep.get("cancelled")).append('\t')
          .append(audRep.get("online")).append('\t')
          .append(audRep.get("offline")).append('\t')
          .append(audRep.get("averageRating")).append('\n').append('\n');

        sb.append("ACTOR SELECTION & BACKUP REPORT\n");
        sb.append("Artist Name\tProject Title\tAssigned Role\tStatus\tBackup Candidate\tRating\n");
        for (Map<String, Object> sel : selectionRows) {
            sb.append(cleanCell(String.valueOf(sel.get("artistName")))).append('\t')
              .append(cleanCell(String.valueOf(sel.get("projectTitle")))).append('\t')
              .append(cleanCell(String.valueOf(sel.get("assignedRole")))).append('\t')
              .append(cleanCell(String.valueOf(sel.get("status")))).append('\t')
              .append(Boolean.TRUE.equals(sel.get("backupCandidate")) ? "Yes" : "No").append('\t')
              .append(sel.get("rating") != null ? sel.get("rating") : "-").append('\n');
        }
        sb.append('\n');

        sb.append("FINANCIAL SUMMARY\n");
        sb.append("Subscription Paid (INR)\tExpenses Paid (INR)\tExpenses Pending (INR)\tActive Contracts\tCommitted Contracts (INR)\n");
        sb.append(finRep.get("subscriptionPaidInr")).append('\t')
          .append(finRep.get("expensesPaidInr")).append('\t')
          .append(finRep.get("expensesPendingInr")).append('\t')
          .append(finRep.get("activeContractsCount")).append('\t')
          .append(finRep.get("committedCompensationInr")).append('\n');

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"entertainment-world-casting-report.xls\"")
                .contentType(MediaType.parseMediaType("application/vnd.ms-excel; charset=UTF-8"))
                .body(sb.toString().getBytes(StandardCharsets.UTF_8));
    }

    @GetMapping("/reports/export-pdf")
    public ResponseEntity<byte[]> exportReportsPdf(HttpSession session) {
        CastingCompany company = authHelper.requirePaidDashboardCompany(session);
        Map<String, Object> report = buildAnalyticsPayload(company);
        @SuppressWarnings("unchecked")
        Map<String, Object> actorRep = (Map<String, Object>) report.get("actorReport");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> projectRows = (List<Map<String, Object>>) report.get("projectReport");
        @SuppressWarnings("unchecked")
        Map<String, Object> audRep = (Map<String, Object>) report.get("auditionReport");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> selectionRows = (List<Map<String, Object>>) report.get("selectionReport");
        @SuppressWarnings("unchecked")
        Map<String, Object> finRep = (Map<String, Object>) report.get("financialSummary");

        List<SimplePdfExportService.PdfSection> sections = new ArrayList<>();
        sections.add(new SimplePdfExportService.PdfSection("1. Actor Registration Report", List.of(
                "Total Registered Actors: " + actorRep.get("totalActors")
                        + " | Verified: " + actorRep.get("verifiedActors")
                        + " | Pending Verification: " + actorRep.get("pendingVerification"),
                "Profiles with Self-Tape / Showreel Videos: " + actorRep.get("withVideo")
        )));

        List<String> projectLines = new ArrayList<>();
        for (Map<String, Object> pr : projectRows) {
            projectLines.add(pr.get("projectTitle") + " [" + pr.get("status") + "] - Total: "
                    + pr.get("totalApplications") + ", Shortlisted: " + pr.get("shortlisted")
                    + ", Invited: " + pr.get("invited") + ", Selected: " + pr.get("selected")
                    + ", Rejected: " + pr.get("rejected"));
        }
        if (projectLines.isEmpty()) projectLines.add("No casting projects created yet.");
        sections.add(new SimplePdfExportService.PdfSection("2. Project-Wise Application Report", projectLines));

        sections.add(new SimplePdfExportService.PdfSection("3. Audition Statistics", List.of(
                "Total Scheduled: " + audRep.get("total")
                        + " | Upcoming/Scheduled: " + audRep.get("scheduled")
                        + " | Completed: " + audRep.get("completed")
                        + " | Cancelled: " + audRep.get("cancelled"),
                "Mode Breakdown - Online: " + audRep.get("online") + ", Offline: " + audRep.get("offline")
                        + " | Average Panel Rating: " + audRep.get("averageRating") + " / 5"
        )));

        List<String> selectionLines = new ArrayList<>();
        for (Map<String, Object> sel : selectionRows) {
            selectionLines.add(sel.get("artistName") + " -> " + sel.get("projectTitle")
                    + " (" + sel.get("assignedRole") + ") | Status: " + sel.get("status")
                    + (Boolean.TRUE.equals(sel.get("backupCandidate")) ? " [BACKUP]" : "")
                    + (sel.get("rating") != null ? " | Rating: " + sel.get("rating") + "/5" : ""));
        }
        if (selectionLines.isEmpty()) selectionLines.add("No final selections or backup candidates marked yet.");
        sections.add(new SimplePdfExportService.PdfSection("4. Actor Selection & Backup Report", selectionLines));

        sections.add(new SimplePdfExportService.PdfSection("5. Contracts & Financial Summary", List.of(
                "Subscription Spend: Rs. " + finRep.get("subscriptionPaidInr")
                        + " | Production Expenses Paid: Rs. " + finRep.get("expensesPaidInr")
                        + " | Pending Payouts: Rs. " + finRep.get("expensesPendingInr"),
                "Active / Signed Contracts: " + finRep.get("activeContractsCount")
                        + " | Total Committed Compensation: Rs. " + finRep.get("committedCompensationInr")
        )));

        byte[] pdfBytes = pdfExportService.buildReportPdf(
                "Entertainment World - Casting Company Report",
                company.getCompanyName() + " (" + company.getCity() + ") | Generated " + LocalDate.now(),
                sections
        );

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"entertainment-world-casting-report.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    // ==================== 5. TEAM MANAGEMENT ====================

    @GetMapping("/team")
    public ResponseEntity<?> listTeam(HttpSession session) {
        CastingCompany company = authHelper.requirePaidDashboardCompany(session);
        List<CastingTeamMember> members = teamRepo.findByCompanyIdOrderByIdDesc(company.getId());
        List<CastingProject> projects = projectRepo.findByCompanyIdOrderByPostedDateDescIdDesc(company.getId());
        return ResponseEntity.ok(Map.of(
                "members", members,
                "projects", projects.stream().map(this::toCompactProject).toList()
        ));
    }

    @PostMapping("/team")
    public ResponseEntity<?> addTeamMember(@RequestBody Map<String, Object> body, HttpSession session) {
        CastingCompany company = authHelper.requirePaidDashboardCompany(session);
        String fullName = str(body.get("fullName"), "");
        String email = str(body.get("email"), "");
        if (fullName.isBlank() || email.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Full name and email are required."));
        }
        CastingTeamMember tm = new CastingTeamMember();
        tm.setCompanyId(company.getId());
        tm.setFullName(fullName);
        tm.setEmail(email);
        tm.setPhone(str(body.get("phone"), null));
        tm.setRoleTitle(str(body.get("roleTitle"), "Casting Coordinator"));
        tm.setPermissionsJson(str(body.get("permissionsJson"), "VIEW_APPLICATIONS,SHORTLIST_ARTISTS,SCHEDULE_AUDITIONS"));
        tm.setActive(true);
        tm.setAddedAt(LocalDateTime.now());
        assignProjectLinks(tm, str(body.get("assignedProjectIds"), ""), company.getId());
        CastingTeamMember saved = teamRepo.save(tm);

        notificationService.notifyCompany(company.getId(), company.getCompanyEmail(),
                "Team Member Added", saved.getFullName() + " joined as " + saved.getRoleTitle() + ".");
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/team/{id}")
    public ResponseEntity<?> updateTeamMember(@PathVariable("id") Long id,
                                              @RequestBody Map<String, Object> body,
                                              HttpSession session) {
        CastingCompany company = authHelper.requirePaidDashboardCompany(session);
        CastingTeamMember tm = teamRepo.findByIdAndCompanyId(id, company.getId())
                .orElseThrow(() -> new IllegalArgumentException("Team member not found"));
        if (body.containsKey("fullName")) tm.setFullName(str(body.get("fullName"), tm.getFullName()));
        if (body.containsKey("email")) tm.setEmail(str(body.get("email"), tm.getEmail()));
        if (body.containsKey("phone")) tm.setPhone(str(body.get("phone"), tm.getPhone()));
        if (body.containsKey("roleTitle")) tm.setRoleTitle(str(body.get("roleTitle"), tm.getRoleTitle()));
        if (body.containsKey("permissionsJson")) tm.setPermissionsJson(str(body.get("permissionsJson"), tm.getPermissionsJson()));
        if (body.containsKey("active")) tm.setActive(Boolean.parseBoolean(String.valueOf(body.get("active"))));
        if (body.containsKey("assignedProjectIds")) {
            assignProjectLinks(tm, str(body.get("assignedProjectIds"), ""), company.getId());
        }
        return ResponseEntity.ok(teamRepo.save(tm));
    }

    @DeleteMapping("/team/{id}")
    public ResponseEntity<?> deleteTeamMember(@PathVariable("id") Long id, HttpSession session) {
        CastingCompany company = authHelper.requirePaidDashboardCompany(session);
        CastingTeamMember tm = teamRepo.findByIdAndCompanyId(id, company.getId())
                .orElseThrow(() -> new IllegalArgumentException("Team member not found"));
        teamRepo.delete(tm);
        return ResponseEntity.ok(Map.of("message", "Team member removed"));
    }

    // ==================== 6. CONTRACT MANAGEMENT ====================

    @GetMapping("/contracts")
    public ResponseEntity<?> listContracts(HttpSession session) {
        CastingCompany company = authHelper.requirePaidDashboardCompany(session);
        Map<Long, CastingProject> projectById = projectRepo.findByCompanyIdOrderByPostedDateDescIdDesc(company.getId())
                .stream().collect(Collectors.toMap(CastingProject::getId, p -> p));
        Map<Long, ArtistProfile> artistById = artistRepo.findAll()
                .stream().collect(Collectors.toMap(ArtistProfile::getId, a -> a));

        List<Map<String, Object>> list = contractRepo.findByCompanyIdOrderByIdDesc(company.getId())
                .stream()
                .map(c -> toContractMap(c, projectById.get(c.getProjectId()), artistById.get(c.getArtistId())))
                .toList();

        return ResponseEntity.ok(Map.of(
                "contracts", list,
                "projects", projectById.values().stream().map(this::toCompactProject).toList(),
                "artists", artistById.values().stream().map(this::toCompactArtist).toList()
        ));
    }

    @PostMapping(value = "/contracts", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createContractJson(@RequestBody Map<String, Object> body, HttpSession session) {
        return createContractInternal(
                parseLong(body.get("projectId")),
                parseLong(body.get("artistId")),
                str(body.get("roleName"), null),
                str(body.get("contractTitle"), ""),
                parseDecimal(body.get("compensationAmount"), new BigDecimal("50000")),
                str(body.get("paymentTerms"), "50% advance, 50% on wrap"),
                str(body.get("contractStatus"), "DRAFT"),
                parseDate(body.get("startDate"), LocalDate.now()),
                parseDate(body.get("endDate"), LocalDate.now().plusMonths(1)),
                str(body.get("notes"), null),
                null,
                session
        );
    }

    @PostMapping(value = "/contracts", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createContractMultipart(
            @RequestParam("projectId") Long projectId,
            @RequestParam("artistId") Long artistId,
            @RequestParam(value = "roleName", required = false) String roleName,
            @RequestParam("contractTitle") String contractTitle,
            @RequestParam(value = "compensationAmount", required = false) String compensationAmount,
            @RequestParam(value = "paymentTerms", required = false) String paymentTerms,
            @RequestParam(value = "contractStatus", required = false, defaultValue = "DRAFT") String contractStatus,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate,
            @RequestParam(value = "notes", required = false) String notes,
            @RequestParam(value = "document", required = false) MultipartFile document,
            HttpSession session
    ) {
        return createContractInternal(
                projectId,
                artistId,
                roleName,
                contractTitle,
                parseDecimal(compensationAmount, new BigDecimal("50000")),
                paymentTerms,
                contractStatus,
                parseDate(startDate, LocalDate.now()),
                parseDate(endDate, LocalDate.now().plusMonths(1)),
                notes,
                document,
                session
        );
    }

    private ResponseEntity<?> createContractInternal(Long projectId, Long artistId, String roleName,
                                                   String contractTitle, BigDecimal compensation,
                                                   String paymentTerms, String contractStatus,
                                                   LocalDate startDate, LocalDate endDate,
                                                   String notes, MultipartFile document, HttpSession session) {
        CastingCompany company = authHelper.requirePaidDashboardCompany(session);
        if (projectId == null || artistId == null || contractTitle == null || contractTitle.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Project, Artist, and Contract Title are required."));
        }
        CastingProject project = projectRepo.findByIdAndCompanyId(projectId, company.getId())
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));
        ArtistProfile artist = artistRepo.findById(artistId)
                .orElseThrow(() -> new IllegalArgumentException("Artist not found"));

        ArtistContract c = new ArtistContract();
        c.setCompanyId(company.getId());
        c.setProjectId(project.getId());
        c.setArtistId(artist.getId());
        c.setRoleName(roleName != null && !roleName.isBlank() ? roleName.trim() : project.getTargetCategory());
        c.setContractTitle(contractTitle.trim());
        c.setCompensationAmount(compensation);
        c.setPaymentTerms(paymentTerms != null && !paymentTerms.isBlank() ? paymentTerms.trim() : "50% advance, 50% on wrap");
        c.setContractStatus(contractStatus != null && !contractStatus.isBlank() ? contractStatus.trim().toUpperCase(Locale.ROOT) : "DRAFT");
        c.setStartDate(startDate);
        c.setEndDate(endDate);
        c.setNotes(notes);
        if (document != null && !document.isEmpty()) {
            c.setDocumentPath(fileStorage.store(document, "contracts"));
        }
        ArtistContract saved = contractRepo.save(c);
        return ResponseEntity.status(HttpStatus.CREATED).body(toContractMap(saved, project, artist));
    }

    @PutMapping("/contracts/{id}")
    public ResponseEntity<?> updateContract(@PathVariable("id") Long id,
                                            @RequestBody Map<String, Object> body,
                                            HttpSession session) {
        CastingCompany company = authHelper.requirePaidDashboardCompany(session);
        ArtistContract c = contractRepo.findByIdAndCompanyId(id, company.getId())
                .orElseThrow(() -> new IllegalArgumentException("Contract not found"));
        if (body.containsKey("contractTitle")) c.setContractTitle(str(body.get("contractTitle"), c.getContractTitle()));
        if (body.containsKey("contractStatus")) c.setContractStatus(str(body.get("contractStatus"), c.getContractStatus()).toUpperCase(Locale.ROOT));
        if (body.containsKey("compensationAmount")) c.setCompensationAmount(parseDecimal(body.get("compensationAmount"), c.getCompensationAmount()));
        if (body.containsKey("paymentTerms")) c.setPaymentTerms(str(body.get("paymentTerms"), c.getPaymentTerms()));
        if (body.containsKey("startDate")) c.setStartDate(parseDate(body.get("startDate"), c.getStartDate()));
        if (body.containsKey("endDate")) c.setEndDate(parseDate(body.get("endDate"), c.getEndDate()));
        if (body.containsKey("notes")) c.setNotes(str(body.get("notes"), c.getNotes()));
        ArtistContract saved = contractRepo.save(c);

        CastingProject p = projectRepo.findById(saved.getProjectId()).orElse(null);
        ArtistProfile a = artistRepo.findById(saved.getArtistId()).orElse(null);
        return ResponseEntity.ok(toContractMap(saved, p, a));
    }

    @DeleteMapping("/contracts/{id}")
    public ResponseEntity<?> deleteContract(@PathVariable("id") Long id, HttpSession session) {
        CastingCompany company = authHelper.requirePaidDashboardCompany(session);
        ArtistContract c = contractRepo.findByIdAndCompanyId(id, company.getId())
                .orElseThrow(() -> new IllegalArgumentException("Contract not found"));
        contractRepo.delete(c);
        return ResponseEntity.ok(Map.of("message", "Contract deleted"));
    }

    // ==================== 7. PAYMENT & EXPENSE TRACKING ====================

    @GetMapping("/expenses")
    public ResponseEntity<?> listExpenses(HttpSession session) {
        CastingCompany company = authHelper.requirePaidDashboardCompany(session);
        Map<Long, CastingProject> projectById = projectRepo.findByCompanyIdOrderByPostedDateDescIdDesc(company.getId())
                .stream().collect(Collectors.toMap(CastingProject::getId, p -> p));
        Map<Long, ArtistProfile> artistById = artistRepo.findAll()
                .stream().collect(Collectors.toMap(ArtistProfile::getId, a -> a));

        List<ProductionExpense> raw = expenseRepo.findByCompanyIdOrderByExpenseDateDescIdDesc(company.getId());
        List<Map<String, Object>> expenses = raw.stream()
                .map(e -> toExpenseMap(e,
                        e.getProjectId() != null ? projectById.get(e.getProjectId()) : null,
                        e.getArtistId() != null ? artistById.get(e.getArtistId()) : null))
                .toList();

        BigDecimal totalPaid = raw.stream()
                .filter(e -> "PAID".equalsIgnoreCase(e.getPaymentStatus()) || "REIMBURSED".equalsIgnoreCase(e.getPaymentStatus()))
                .map(ProductionExpense::getAmountInr).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalPending = raw.stream()
                .filter(e -> "PENDING".equalsIgnoreCase(e.getPaymentStatus()))
                .map(ProductionExpense::getAmountInr).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal subscriptionPaid = paymentRepo.findByCompanyIdOrderByPaidAtDesc(company.getId()).stream()
                .map(SubscriptionPayment::getTotalPaidInr).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);

        return ResponseEntity.ok(Map.of(
                "expenses", expenses,
                "summary", Map.of(
                        "totalEntries", raw.size(),
                        "paidInr", totalPaid,
                        "pendingInr", totalPending,
                        "totalExpensesInr", totalPaid.add(totalPending),
                        "subscriptionPaidInr", subscriptionPaid
                ),
                "projects", projectById.values().stream().map(this::toCompactProject).toList(),
                "artists", artistById.values().stream().map(this::toCompactArtist).toList()
        ));
    }

    @PostMapping("/expenses")
    public ResponseEntity<?> addExpense(@RequestBody Map<String, Object> body, HttpSession session) {
        CastingCompany company = authHelper.requirePaidDashboardCompany(session);
        String title = str(body.get("title"), "");
        BigDecimal amount = parseDecimal(body.get("amountInr"), null);
        if (title.isBlank() || amount == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Expense title and amount are required."));
        }
        Long projectId = parseLong(body.get("projectId"));
        if (projectId != null) {
            projectRepo.findByIdAndCompanyId(projectId, company.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Project not found"));
        }
        Long artistId = parseLong(body.get("artistId"));
        if (artistId != null) {
            artistRepo.findById(artistId).orElseThrow(() -> new IllegalArgumentException("Artist not found"));
        }

        ProductionExpense ex = new ProductionExpense();
        ex.setCompanyId(company.getId());
        ex.setProjectId(projectId);
        ex.setArtistId(artistId);
        ex.setCategory(str(body.get("category"), "ARTIST_FEE").toUpperCase(Locale.ROOT));
        ex.setTitle(title);
        ex.setAmountInr(amount);
        ex.setPaymentStatus(str(body.get("paymentStatus"), "PENDING").toUpperCase(Locale.ROOT));
        ex.setPaymentMethod(str(body.get("paymentMethod"), "UPI"));
        ex.setExpenseDate(parseDate(body.get("expenseDate"), LocalDate.now()));
        ex.setNotes(str(body.get("notes"), null));
        ProductionExpense saved = expenseRepo.save(ex);

        CastingProject p = projectId != null ? projectRepo.findById(projectId).orElse(null) : null;
        ArtistProfile a = artistId != null ? artistRepo.findById(artistId).orElse(null) : null;
        return ResponseEntity.status(HttpStatus.CREATED).body(toExpenseMap(saved, p, a));
    }

    @PutMapping("/expenses/{id}")
    public ResponseEntity<?> updateExpense(@PathVariable("id") Long id,
                                           @RequestBody Map<String, Object> body,
                                           HttpSession session) {
        CastingCompany company = authHelper.requirePaidDashboardCompany(session);
        ProductionExpense ex = expenseRepo.findByIdAndCompanyId(id, company.getId())
                .orElseThrow(() -> new IllegalArgumentException("Expense not found"));
        if (body.containsKey("title")) ex.setTitle(str(body.get("title"), ex.getTitle()));
        if (body.containsKey("category")) ex.setCategory(str(body.get("category"), ex.getCategory()).toUpperCase(Locale.ROOT));
        if (body.containsKey("amountInr")) ex.setAmountInr(parseDecimal(body.get("amountInr"), ex.getAmountInr()));
        if (body.containsKey("paymentStatus")) ex.setPaymentStatus(str(body.get("paymentStatus"), ex.getPaymentStatus()).toUpperCase(Locale.ROOT));
        if (body.containsKey("paymentMethod")) ex.setPaymentMethod(str(body.get("paymentMethod"), ex.getPaymentMethod()));
        if (body.containsKey("expenseDate")) ex.setExpenseDate(parseDate(body.get("expenseDate"), ex.getExpenseDate()));
        if (body.containsKey("notes")) ex.setNotes(str(body.get("notes"), ex.getNotes()));
        ProductionExpense saved = expenseRepo.save(ex);

        CastingProject p = saved.getProjectId() != null ? projectRepo.findById(saved.getProjectId()).orElse(null) : null;
        ArtistProfile a = saved.getArtistId() != null ? artistRepo.findById(saved.getArtistId()).orElse(null) : null;
        return ResponseEntity.ok(toExpenseMap(saved, p, a));
    }

    @DeleteMapping("/expenses/{id}")
    public ResponseEntity<?> deleteExpense(@PathVariable("id") Long id, HttpSession session) {
        CastingCompany company = authHelper.requirePaidDashboardCompany(session);
        ProductionExpense ex = expenseRepo.findByIdAndCompanyId(id, company.getId())
                .orElseThrow(() -> new IllegalArgumentException("Expense not found"));
        expenseRepo.delete(ex);
        return ResponseEntity.ok(Map.of("message", "Expense deleted"));
    }

    // ==================== ANALYTICS & MAPPERS ====================

    private Map<String, Object> buildAnalyticsPayload(CastingCompany company) {
        List<ArtistProfile> allArtists = artistRepo.findAll();
        Map<Long, ArtistProfile> artistById = allArtists.stream()
                .collect(Collectors.toMap(ArtistProfile::getId, a -> a));
        List<CastingProject> projects = projectRepo.findByCompanyIdOrderByPostedDateDescIdDesc(company.getId());
        Map<Long, CastingProject> projectById = projects.stream()
                .collect(Collectors.toMap(CastingProject::getId, p -> p));
        List<Long> projectIds = projects.stream().map(CastingProject::getId).toList();
        List<ProjectApplication> allApps = projectIds.isEmpty() ? List.of() : appRepo.findByProjectIdInOrderByAppliedAtDesc(projectIds);
        List<AuditionSchedule> allAuds = auditionRepo.findByCompanyIdOrderByAuditionDateAscIdDesc(company.getId());
        List<ArtistContract> contracts = contractRepo.findByCompanyIdOrderByIdDesc(company.getId());
        List<ProductionExpense> expenses = expenseRepo.findByCompanyIdOrderByExpenseDateDescIdDesc(company.getId());
        List<SubscriptionPayment> payments = paymentRepo.findByCompanyIdOrderByPaidAtDesc(company.getId());

        long verifiedCount = allArtists.stream().filter(ArtistProfile::isProfileVerified).count();
        long withVideo = allArtists.stream().filter(ArtistProfile::isHasVideo).count();
        Map<String, Long> byCategory = allArtists.stream()
                .collect(Collectors.groupingBy(a -> a.getCategory() != null ? a.getCategory() : "Other", LinkedHashMap::new, Collectors.counting()));
        Map<String, Long> byCity = allArtists.stream()
                .collect(Collectors.groupingBy(a -> a.getCity() != null ? a.getCity() : "Unknown", LinkedHashMap::new, Collectors.counting()));

        Map<String, Object> actorReport = new LinkedHashMap<>();
        actorReport.put("totalActors", allArtists.size());
        actorReport.put("verifiedActors", verifiedCount);
        actorReport.put("pendingVerification", allArtists.size() - verifiedCount);
        actorReport.put("withVideo", withVideo);
        actorReport.put("byCategory", byCategory);
        actorReport.put("byCity", byCity);

        List<Map<String, Object>> projectReport = new ArrayList<>();
        for (CastingProject p : projects) {
            List<ProjectApplication> pApps = allApps.stream().filter(pa -> p.getId().equals(pa.getProjectId())).toList();
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("projectId", p.getId());
            row.put("projectTitle", p.getProjectTitle());
            row.put("status", p.getStatus());
            row.put("targetCategory", p.getTargetCategory());
            row.put("totalApplications", pApps.size());
            row.put("applied", pApps.stream().filter(a -> "APPLIED".equalsIgnoreCase(a.getStatus())).count());
            row.put("shortlisted", pApps.stream().filter(a -> "SHORTLISTED".equalsIgnoreCase(a.getStatus())).count());
            row.put("invited", pApps.stream().filter(a -> "INVITED".equalsIgnoreCase(a.getStatus())).count());
            row.put("selected", pApps.stream().filter(a -> "SELECTED".equalsIgnoreCase(a.getStatus()) && !a.isBackupCandidate()).count());
            row.put("backups", pApps.stream().filter(ProjectApplication::isBackupCandidate).count());
            row.put("rejected", pApps.stream().filter(a -> "REJECTED".equalsIgnoreCase(a.getStatus())).count());
            projectReport.add(row);
        }

        long audScheduled = allAuds.stream().filter(a -> "SCHEDULED".equalsIgnoreCase(a.getStatus()) || "RESCHEDULED".equalsIgnoreCase(a.getStatus())).count();
        long audCompleted = allAuds.stream().filter(a -> "COMPLETED".equalsIgnoreCase(a.getStatus())).count();
        long audCancelled = allAuds.stream().filter(a -> "CANCELLED".equalsIgnoreCase(a.getStatus())).count();
        long audOnline = allAuds.stream().filter(a -> "Online".equalsIgnoreCase(a.getAuditionMode())).count();
        double avgRating = allAuds.stream().map(AuditionSchedule::getRating).filter(Objects::nonNull)
                .mapToInt(Integer::intValue).average().orElse(0.0);

        Map<String, Object> auditionReport = new LinkedHashMap<>();
        auditionReport.put("total", allAuds.size());
        auditionReport.put("scheduled", audScheduled);
        auditionReport.put("completed", audCompleted);
        auditionReport.put("cancelled", audCancelled);
        auditionReport.put("online", audOnline);
        auditionReport.put("offline", allAuds.size() - audOnline);
        auditionReport.put("averageRating", Math.round(avgRating * 10.0) / 10.0);

        List<Map<String, Object>> selectionReport = allApps.stream()
                .filter(pa -> "SELECTED".equalsIgnoreCase(pa.getStatus())
                        || "SHORTLISTED".equalsIgnoreCase(pa.getStatus())
                        || pa.isBackupCandidate())
                .map(pa -> {
                    ArtistProfile a = artistById.get(pa.getArtistId());
                    CastingProject p = projectById.get(pa.getProjectId());
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("applicationId", pa.getId());
                    row.put("artistId", pa.getArtistId());
                    row.put("artistName", a != null ? a.getFullName() : "Artist #" + pa.getArtistId());
                    row.put("projectTitle", p != null ? p.getProjectTitle() : "Project #" + pa.getProjectId());
                    row.put("assignedRole", pa.getAssignedRole() != null ? pa.getAssignedRole() : pa.getRoleName());
                    row.put("status", pa.getStatus());
                    row.put("backupCandidate", pa.isBackupCandidate());
                    row.put("rating", pa.getRating());
                    return row;
                }).toList();

        List<Map<String, Object>> monthlyActivity = new ArrayList<>();
        DateTimeFormatter monthFmt = DateTimeFormatter.ofPattern("MMM yyyy", Locale.ENGLISH);
        YearMonth nowYm = YearMonth.now();
        for (int i = 5; i >= 0; i--) {
            YearMonth ym = nowYm.minusMonths(i);
            long mApps = allApps.stream().filter(a -> a.getAppliedAt() != null && YearMonth.from(a.getAppliedAt()).equals(ym)).count();
            long mAuds = allAuds.stream().filter(a -> a.getAuditionDate() != null && YearMonth.from(a.getAuditionDate()).equals(ym)).count();
            long mSel = allApps.stream().filter(a -> "SELECTED".equalsIgnoreCase(a.getStatus()) && a.getAppliedAt() != null && YearMonth.from(a.getAppliedAt()).equals(ym)).count();
            BigDecimal mExp = expenses.stream()
                    .filter(e -> e.getExpenseDate() != null && YearMonth.from(e.getExpenseDate()).equals(ym))
                    .map(ProductionExpense::getAmountInr).reduce(BigDecimal.ZERO, BigDecimal::add);
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("month", ym.format(monthFmt));
            row.put("applications", mApps);
            row.put("auditions", mAuds);
            row.put("selections", mSel);
            row.put("expensesInr", mExp);
            monthlyActivity.add(row);
        }

        BigDecimal subscriptionPaid = payments.stream()
                .map(SubscriptionPayment::getTotalPaidInr).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal expensesPaid = expenses.stream()
                .filter(e -> "PAID".equalsIgnoreCase(e.getPaymentStatus()) || "REIMBURSED".equalsIgnoreCase(e.getPaymentStatus()))
                .map(ProductionExpense::getAmountInr).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal expensesPending = expenses.stream()
                .filter(e -> "PENDING".equalsIgnoreCase(e.getPaymentStatus()))
                .map(ProductionExpense::getAmountInr).reduce(BigDecimal.ZERO, BigDecimal::add);
        long activeContractsCount = contracts.stream()
                .filter(c -> "ACTIVE".equalsIgnoreCase(c.getContractStatus()) || "SIGNED".equalsIgnoreCase(c.getContractStatus()))
                .count();
        BigDecimal committedCompensation = contracts.stream()
                .filter(c -> !"TERMINATED".equalsIgnoreCase(c.getContractStatus()))
                .map(ArtistContract::getCompensationAmount).filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> financialSummary = new LinkedHashMap<>();
        financialSummary.put("subscriptionPaidInr", subscriptionPaid);
        financialSummary.put("expensesPaidInr", expensesPaid);
        financialSummary.put("expensesPendingInr", expensesPending);
        financialSummary.put("activeContractsCount", activeContractsCount);
        financialSummary.put("committedCompensationInr", committedCompensation);

        return Map.of(
                "actorReport", actorReport,
                "projectReport", projectReport,
                "auditionReport", auditionReport,
                "selectionReport", selectionReport,
                "monthlyActivity", monthlyActivity,
                "financialSummary", financialSummary
        );
    }

    private void assignProjectLinks(CastingTeamMember tm, String csvIds, Long companyId) {
        if (csvIds == null || csvIds.isBlank()) {
            tm.setAssignedProjectIds("");
            tm.setAssignedProjectNames("");
            return;
        }
        List<Long> validIds = new ArrayList<>();
        List<String> validNames = new ArrayList<>();
        for (String part : csvIds.split(",")) {
            String trimmed = part.trim();
            if (trimmed.isEmpty()) continue;
            try {
                Long pid = Long.valueOf(trimmed);
                projectRepo.findByIdAndCompanyId(pid, companyId).ifPresent(p -> {
                    validIds.add(p.getId());
                    validNames.add(p.getProjectTitle());
                });
            } catch (NumberFormatException ignored) { }
        }
        tm.setAssignedProjectIds(validIds.stream().map(String::valueOf).collect(Collectors.joining(",")));
        tm.setAssignedProjectNames(String.join(", ", validNames));
    }

    private Map<String, Object> toAuditionMap(AuditionSchedule au, CastingProject p, ArtistProfile a) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", au.getId());
        m.put("projectId", au.getProjectId());
        m.put("projectTitle", p != null ? p.getProjectTitle() : "Project #" + au.getProjectId());
        m.put("artistId", au.getArtistId());
        m.put("artistName", a != null ? a.getFullName() : "Artist #" + au.getArtistId());
        m.put("artistCategory", a != null ? a.getCategory() : "");
        m.put("artistCity", a != null ? a.getCity() : "");
        m.put("artistAvatar", a != null ? a.getAvatarUrl() : "/assets/images/cat-actor.jpg");
        m.put("applicationId", au.getApplicationId());
        m.put("roleName", au.getRoleName());
        m.put("auditionMode", au.getAuditionMode());
        m.put("auditionDate", au.getAuditionDate());
        m.put("auditionTime", au.getAuditionTime());
        m.put("locationOrLink", au.getLocationOrLink());
        m.put("invitationMessage", au.getInvitationMessage());
        m.put("invitationSent", au.isInvitationSent());
        m.put("auditionVideoUrl", au.getAuditionVideoUrl());
        m.put("status", au.getStatus());
        m.put("rating", au.getRating());
        m.put("feedback", au.getFeedback());
        m.put("createdAt", au.getCreatedAt());
        m.put("updatedAt", au.getUpdatedAt());
        return m;
    }

    private Map<String, Object> toContractMap(ArtistContract c, CastingProject p, ArtistProfile a) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", c.getId());
        m.put("projectId", c.getProjectId());
        m.put("projectTitle", p != null ? p.getProjectTitle() : "Project #" + c.getProjectId());
        m.put("artistId", c.getArtistId());
        m.put("artistName", a != null ? a.getFullName() : "Artist #" + c.getArtistId());
        m.put("artistCategory", a != null ? a.getCategory() : "");
        m.put("artistAvatar", a != null ? a.getAvatarUrl() : "/assets/images/cat-actor.jpg");
        m.put("roleName", c.getRoleName());
        m.put("contractTitle", c.getContractTitle());
        m.put("compensationAmount", c.getCompensationAmount());
        m.put("paymentTerms", c.getPaymentTerms());
        m.put("contractStatus", c.getContractStatus());
        m.put("startDate", c.getStartDate());
        m.put("endDate", c.getEndDate());
        m.put("documentPath", c.getDocumentPath());
        m.put("notes", c.getNotes());
        m.put("createdAt", c.getCreatedAt());
        return m;
    }

    private Map<String, Object> toExpenseMap(ProductionExpense e, CastingProject p, ArtistProfile a) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", e.getId());
        m.put("projectId", e.getProjectId());
        m.put("projectTitle", p != null ? p.getProjectTitle() : "General Casting");
        m.put("artistId", e.getArtistId());
        m.put("artistName", a != null ? a.getFullName() : null);
        m.put("category", e.getCategory());
        m.put("title", e.getTitle());
        m.put("amountInr", e.getAmountInr());
        m.put("paymentStatus", e.getPaymentStatus());
        m.put("paymentMethod", e.getPaymentMethod());
        m.put("expenseDate", e.getExpenseDate());
        m.put("notes", e.getNotes());
        m.put("createdAt", e.getCreatedAt());
        return m;
    }

    private Map<String, Object> toCompactProject(CastingProject p) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", p.getId());
        m.put("projectTitle", p.getProjectTitle() != null ? p.getProjectTitle() : "Untitled Project");
        m.put("targetCategory", p.getTargetCategory() != null ? p.getTargetCategory() : "Actor");
        m.put("status", p.getStatus() != null ? p.getStatus() : "ACTIVE");
        return m;
    }

    private Map<String, Object> toCompactArtist(ArtistProfile a) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", a.getId());
        m.put("fullName", a.getFullName());
        m.put("category", a.getCategory());
        m.put("city", a.getCity());
        m.put("avatarUrl", a.getAvatarUrl());
        m.put("profileVerified", a.isProfileVerified());
        return m;
    }

    private Set<Long> getUnlockedContactIds(Long companyId) {
        return historyRepo.findByCompanyIdAndViewTypeOrderByViewedAtDesc(companyId, "CONTACT_VIEW")
                .stream().map(ContactPortfolioHistory::getArtistId).collect(Collectors.toSet());
    }

    private Set<Long> getSavedArtistIds(Long companyId) {
        return folderItemRepo.findByCompanyIdOrderBySavedAtDesc(companyId)
                .stream().map(ArtistFolderItem::getArtistId).collect(Collectors.toSet());
    }

    private Long parseLong(Object val) {
        if (val == null) return null;
        String s = String.valueOf(val).trim();
        if (s.isEmpty() || "null".equalsIgnoreCase(s)) return null;
        return Long.valueOf(s);
    }

    private BigDecimal parseDecimal(Object val, BigDecimal fallback) {
        if (val == null) return fallback;
        String s = String.valueOf(val).trim();
        if (s.isEmpty() || "null".equalsIgnoreCase(s)) return fallback;
        return new BigDecimal(s);
    }

    private LocalDate parseDate(Object val, LocalDate fallback) {
        if (val == null) return fallback;
        String s = String.valueOf(val).trim();
        if (s.isEmpty() || "null".equalsIgnoreCase(s)) return fallback;
        return LocalDate.parse(s);
    }

    private String str(Object val, String fallback) {
        if (val == null) return fallback;
        String s = String.valueOf(val).trim();
        return s.isEmpty() || "null".equalsIgnoreCase(s) ? fallback : s;
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
