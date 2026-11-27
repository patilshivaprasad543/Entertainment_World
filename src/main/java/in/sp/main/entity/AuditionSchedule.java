package in.sp.main.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Scheduled online or offline audition session for a candidate and project.
 */
@Entity
@Table(name = "audition_schedules")
public class AuditionSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long companyId;

    @Column(nullable = false)
    private Long projectId;

    @Column(nullable = false)
    private Long artistId;

    private Long applicationId;

    @Column(length = 120)
    private String roleName;

    @Column(nullable = false, length = 20)
    private String auditionMode = "Online"; // Online / Offline

    @Column(nullable = false)
    private LocalDate auditionDate;

    @Column(nullable = false, length = 40)
    private String auditionTime;

    @Column(length = 320)
    private String locationOrLink;

    @Column(columnDefinition = "TEXT")
    private String invitationMessage;

    @Column(nullable = false)
    private boolean invitationSent = true;

    @Column(length = 400)
    private String auditionVideoUrl;

    @Column(nullable = false, length = 30)
    private String status = "SCHEDULED"; // SCHEDULED, RESCHEDULED, COMPLETED, CANCELLED

    private Integer rating; // 1 to 5

    @Column(columnDefinition = "TEXT")
    private String feedback;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCompanyId() { return companyId; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }

    public Long getArtistId() { return artistId; }
    public void setArtistId(Long artistId) { this.artistId = artistId; }

    public Long getApplicationId() { return applicationId; }
    public void setApplicationId(Long applicationId) { this.applicationId = applicationId; }

    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }

    public String getAuditionMode() { return auditionMode; }
    public void setAuditionMode(String auditionMode) { this.auditionMode = auditionMode; }

    public LocalDate getAuditionDate() { return auditionDate; }
    public void setAuditionDate(LocalDate auditionDate) { this.auditionDate = auditionDate; }

    public String getAuditionTime() { return auditionTime; }
    public void setAuditionTime(String auditionTime) { this.auditionTime = auditionTime; }

    public String getLocationOrLink() { return locationOrLink; }
    public void setLocationOrLink(String locationOrLink) { this.locationOrLink = locationOrLink; }

    public String getInvitationMessage() { return invitationMessage; }
    public void setInvitationMessage(String invitationMessage) { this.invitationMessage = invitationMessage; }

    public boolean isInvitationSent() { return invitationSent; }
    public void setInvitationSent(boolean invitationSent) { this.invitationSent = invitationSent; }

    public String getAuditionVideoUrl() { return auditionVideoUrl; }
    public void setAuditionVideoUrl(String auditionVideoUrl) { this.auditionVideoUrl = auditionVideoUrl; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
