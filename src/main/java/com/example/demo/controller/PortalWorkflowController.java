package com.example.demo.controller;

import com.example.demo.model.UserRole;
import com.example.demo.repository.UserAccountRepository;
import in.sp.main.entity.ArtistProfile;
import in.sp.main.entity.CastingCompany;
import in.sp.main.entity.CastingProject;
import in.sp.main.entity.ProjectApplication;
import in.sp.main.repository.ArtistProfileRepository;
import in.sp.main.repository.CastingCompanyRepository;
import in.sp.main.repository.CastingProjectRepository;
import in.sp.main.repository.ProjectApplicationRepository;
import in.sp.main.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Unified backend workflow bridge connecting Super Admin, Professionals (Actors),
 * and Casting Productions (Studios).
 */
@RestController
@RequestMapping("/api/workflow")
public class PortalWorkflowController {

    private final CastingCompanyRepository companyRepo;
    private final CastingProjectRepository projectRepo;
    private final ArtistProfileRepository artistRepo;
    private final ProjectApplicationRepository applicationRepo;
    private final UserAccountRepository userAccountRepo;
    private final NotificationService notificationService;

    public PortalWorkflowController(CastingCompanyRepository companyRepo,
                                    CastingProjectRepository projectRepo,
                                    ArtistProfileRepository artistRepo,
                                    ProjectApplicationRepository applicationRepo,
                                    UserAccountRepository userAccountRepo,
                                    NotificationService notificationService) {
        this.companyRepo = companyRepo;
        this.projectRepo = projectRepo;
        this.artistRepo = artistRepo;
        this.applicationRepo = applicationRepo;
        this.userAccountRepo = userAccountRepo;
        this.notificationService = notificationService;
    }

    /**
     * Lists active casting projects published by Admin-approved, non-suspended Casting Productions
     * so Professionals can browse and apply to real studio calls.
     */
    @GetMapping("/opportunities")
    public List<Map<String, Object>> listApprovedOpportunities() {
        Map<Long, CastingCompany> approvedCompanies = companyRepo.findAll().stream()
                .filter(c -> "Approved".equalsIgnoreCase(c.getAccountStatus())
                        && !"Suspended".equalsIgnoreCase(c.getAccountSuspension()))
                .collect(Collectors.toMap(CastingCompany::getId, Function.identity()));

        return projectRepo.findByStatusIgnoreCaseOrderByPostedDateDescIdDesc("ACTIVE").stream()
                .filter(p -> approvedCompanies.containsKey(p.getCompanyId()))
                .map(p -> {
                    CastingCompany studio = approvedCompanies.get(p.getCompanyId());
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("id", "cp-" + p.getId());
                    item.put("backendProjectId", p.getId());
                    item.put("title", p.getProjectTitle());
                    item.put("company", studio.getCompanyName());
                    item.put("companyId", studio.getId());
                    item.put("location", p.getLocation() != null && !p.getLocation().isBlank() ? p.getLocation() : studio.getCity());
                    item.put("category", p.getTargetCategory() != null ? p.getTargetCategory() : "Acting");
                    item.put("budget", p.getBudget() != null && !p.getBudget().isBlank() ? p.getBudget() : p.getPaymentType());
                    item.put("postedAt", p.getPostedDate() != null ? p.getPostedDate().toString() : "Active");
                    item.put("description", p.getProjectDetails());
                    item.put("roleName", p.getCharacterName() != null && !p.getCharacterName().isBlank() ? p.getCharacterName() : p.getTargetCategory());
                    item.put("auditionType", p.getAuditionType());
                    item.put("auditionDate", p.getAuditionDate() != null ? p.getAuditionDate().toString() : "");
                    return item;
                })
                .toList();
    }

    /**
     * Returns all casting applications and studio invitations for the signed-in Professional.
     */
    @GetMapping("/professional/applications")
    public List<Map<String, Object>> listProfessionalApplications(@RequestParam String email) {
        if (email == null || email.isBlank()) {
            return List.of();
        }
        Optional<ArtistProfile> artistOpt = artistRepo.findByEmailIgnoreCase(email.trim());
        if (artistOpt.isEmpty()) {
            return List.of();
        }

        Map<Long, CastingProject> projectsById = projectRepo.findAll().stream()
                .collect(Collectors.toMap(CastingProject::getId, Function.identity()));
        Map<Long, CastingCompany> companiesById = companyRepo.findAll().stream()
                .collect(Collectors.toMap(CastingCompany::getId, Function.identity()));

        return applicationRepo.findByArtistIdOrderByAppliedAtDesc(artistOpt.get().getId()).stream()
                .map(app -> {
                    CastingProject project = projectsById.get(app.getProjectId());
                    CastingCompany studio = project != null ? companiesById.get(project.getCompanyId()) : null;
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("applicationId", app.getId());
                    row.put("backendProjectId", app.getProjectId());
                    row.put("title", project != null ? project.getProjectTitle() : app.getRoleName());
                    row.put("company", studio != null ? studio.getCompanyName() : "Casting Studio");
                    row.put("location", project != null && project.getLocation() != null ? project.getLocation() : "India");
                    row.put("category", project != null ? project.getTargetCategory() : "Acting");
                    row.put("roleName", app.getRoleName());
                    row.put("status", app.getStatus());
                    row.put("auditionType", project != null ? project.getAuditionType() : "Online");
                    row.put("auditionDate", project != null && project.getAuditionDate() != null ? project.getAuditionDate().toString() : "");
                    row.put("appliedAt", app.getAppliedAt() != null ? app.getAppliedAt().toLocalDate().toString() : "Recently");
                    return row;
                })
                .toList();
    }

    /**
     * Submits a Professional's application to a Casting Production project and notifies the studio.
     */
    @PostMapping("/professional/apply")
    public ResponseEntity<?> applyToCastingProject(@RequestBody Map<String, Object> payload) {
        Object projectIdRaw = payload.get("projectId");
        String email = stringVal(payload.get("email"));
        if (projectIdRaw == null || email.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "projectId and email are required."));
        }

        Long projectId = Long.valueOf(String.valueOf(projectIdRaw));
        CastingProject project = projectRepo.findById(projectId).orElse(null);
        if (project == null) {
            return ResponseEntity.notFound().build();
        }

        CastingCompany company = companyRepo.findById(project.getCompanyId()).orElse(null);
        if (company == null || !"Approved".equalsIgnoreCase(company.getAccountStatus())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Casting studio is not verified by Super Admin."));
        }

        ArtistProfile artist = findOrCreateArtist(payload, email);
        Optional<ProjectApplication> existing = applicationRepo.findByProjectIdAndArtistId(projectId, artist.getId());
        if (existing.isPresent()) {
            return ResponseEntity.ok(Map.of(
                    "message", "Already applied to this casting call.",
                    "applicationId", existing.get().getId(),
                    "status", existing.get().getStatus()
            ));
        }

        ProjectApplication app = new ProjectApplication();
        app.setProjectId(projectId);
        app.setArtistId(artist.getId());
        String roleName = stringVal(payload.get("roleName"));
        app.setRoleName(!roleName.isBlank() ? roleName : project.getTargetCategory());
        app.setStatus("APPLIED");
        app.setAuditionSubmissionTitle(stringVal(payload.get("auditionSubmissionTitle")));
        app.setAuditionVideoUrl(stringVal(payload.get("auditionVideoUrl")));
        ProjectApplication saved = applicationRepo.save(app);

        notificationService.notifyCompany(
                company.getId(),
                company.getCompanyEmail(),
                "New Talent Application: " + project.getProjectTitle(),
                artist.getFullName() + " (" + artist.getCategory() + ") applied for " + saved.getRoleName() + "."
        );

        return ResponseEntity.ok(Map.of(
                "message", "Application submitted to " + company.getCompanyName(),
                "applicationId", saved.getId(),
                "status", saved.getStatus()
        ));
    }

    /**
     * Allows a Professional to respond to an audition invitation from a Casting Production.
     */
    @PostMapping("/professional/invitations/{applicationId}/respond")
    public ResponseEntity<?> respondToInvitation(@PathVariable Long applicationId,
                                                 @RequestBody Map<String, String> payload) {
        ProjectApplication app = applicationRepo.findById(applicationId).orElse(null);
        if (app == null) {
            return ResponseEntity.notFound().build();
        }
        String status = payload.getOrDefault("status", "ACCEPTED").toUpperCase(Locale.ROOT);
        app.setStatus(status);
        applicationRepo.save(app);

        projectRepo.findById(app.getProjectId()).ifPresent(project ->
                companyRepo.findById(project.getCompanyId()).ifPresent(company ->
                        notificationService.notifyCompany(
                                company.getId(),
                                company.getCompanyEmail(),
                                "Invitation Response: " + project.getProjectTitle(),
                                "Talent updated invitation status to " + status + " for " + app.getRoleName() + "."
                        )
                )
        );

        return ResponseEntity.ok(Map.of("applicationId", app.getId(), "status", app.getStatus()));
    }

    /**
     * Syncs a Professional's profile into ArtistProfile so Casting Productions can discover them.
     */
    @PostMapping("/professional/sync-profile")
    public ResponseEntity<?> syncProfessionalProfile(@RequestBody Map<String, Object> payload) {
        String email = stringVal(payload.get("email"));
        if (email.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "email is required."));
        }
        ArtistProfile artist = findOrCreateArtist(payload, email);
        return ResponseEntity.ok(Map.of("artistId", artist.getId(), "fullName", artist.getFullName()));
    }

    /**
     * Cross-portal workflow metrics for Super Admin (Admin <-> Casting <-> Professionals).
     */
    @GetMapping("/admin/overview")
    public Map<String, Object> adminWorkflowOverview() {
        List<CastingCompany> studios = companyRepo.findAll();
        long approvedStudios = studios.stream().filter(c -> "Approved".equalsIgnoreCase(c.getAccountStatus())).count();
        long pendingStudios = studios.stream().filter(c -> "Pending".equalsIgnoreCase(c.getAccountStatus())).count();

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("professionalsCount", Math.max(artistRepo.count(), userAccountRepo.findByRoleInOrderByIdDesc(List.of(UserRole.ACTOR)).size()));
        summary.put("castingStudiosCount", studios.size());
        summary.put("approvedStudiosCount", approvedStudios);
        summary.put("pendingStudiosCount", pendingStudios);
        summary.put("activeProjectsCount", projectRepo.findByStatusIgnoreCaseOrderByPostedDateDescIdDesc("ACTIVE").size());
        summary.put("applicationsCount", applicationRepo.count());
        return summary;
    }

    private ArtistProfile findOrCreateArtist(Map<String, Object> payload, String email) {
        ArtistProfile artist = artistRepo.findByEmailIgnoreCase(email.trim()).orElseGet(ArtistProfile::new);
        String fullName = stringVal(payload.get("fullName"));
        String category = stringVal(payload.get("category"));
        String city = stringVal(payload.get("city"));
        String phone = stringVal(payload.get("phone"));
        String bio = stringVal(payload.get("bio"));
        String videoTitle = stringVal(payload.get("videoTitle"));
        String videoUrl = stringVal(payload.get("videoUrl"));

        artist.setEmail(email.trim());
        if (!fullName.isBlank()) artist.setFullName(fullName);
        else if (artist.getFullName() == null) artist.setFullName(email.split("@")[0]);

        if (!category.isBlank()) artist.setCategory(category);
        else if (artist.getCategory() == null) artist.setCategory("Actor");

        if (!city.isBlank()) artist.setCity(city);
        else if (artist.getCity() == null) artist.setCity("Mumbai");

        if (!phone.isBlank()) artist.setPhone(phone);
        else if (artist.getPhone() == null) artist.setPhone("+91 90000 00000");

        if (!bio.isBlank()) artist.setBio(bio);
        if (artist.getGender() == null) artist.setGender("Any");
        if (artist.getAge() <= 0) artist.setAge(25);
        if (artist.getAgencyName() == null) artist.setAgencyName("Independent Talent");
        if (artist.getExpertise() == null) artist.setExpertise("Experienced");
        if (artist.getLanguages() == null) artist.setLanguages("Hindi, English");
        if (artist.getAvatarUrl() == null) artist.setAvatarUrl("/images/avatars/actor-male-1.jpg");

        if (!videoUrl.isBlank()) {
            artist.setHasVideo(true);
            artist.setVideoUrl(videoUrl);
            artist.setVideoTitle(!videoTitle.isBlank() ? videoTitle : "Showreel");
        }
        return artistRepo.save(artist);
    }

    private static String stringVal(Object obj) {
        return obj == null ? "" : String.valueOf(obj).trim();
    }
}
