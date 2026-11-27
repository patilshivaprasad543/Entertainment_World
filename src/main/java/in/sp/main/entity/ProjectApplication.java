package in.sp.main.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Artist submission / invitation / suggestion linked to a CastingProject.
 */
@Entity
@Table(name = "project_applications")
public class ProjectApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long projectId;

    @Column(nullable = false)
    private Long artistId;

    @Column(nullable = false, length = 120)
    private String roleName;

    @Column(nullable = false, length = 30)
    private String status = "APPLIED"; // APPLIED, SHORTLISTED, INVITED, SUGGESTED

    @Column(length = 180)
    private String auditionSubmissionTitle;

    @Column(length = 400)
    private String auditionVideoUrl;

    @Column(nullable = false)
    private LocalDateTime appliedAt = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }

    public Long getArtistId() { return artistId; }
    public void setArtistId(Long artistId) { this.artistId = artistId; }

    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getAuditionSubmissionTitle() { return auditionSubmissionTitle; }
    public void setAuditionSubmissionTitle(String auditionSubmissionTitle) { this.auditionSubmissionTitle = auditionSubmissionTitle; }

    public String getAuditionVideoUrl() { return auditionVideoUrl; }
    public void setAuditionVideoUrl(String auditionVideoUrl) { this.auditionVideoUrl = auditionVideoUrl; }

    public LocalDateTime getAppliedAt() { return appliedAt; }
    public void setAppliedAt(LocalDateTime appliedAt) { this.appliedAt = appliedAt; }
}
