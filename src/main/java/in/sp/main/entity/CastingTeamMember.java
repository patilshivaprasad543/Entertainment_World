package in.sp.main.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Casting assistant, coordinator, or scout assigned to a Casting Company's team.
 */
@Entity
@Table(name = "casting_team_members")
public class CastingTeamMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long companyId;

    @Column(nullable = false, length = 140)
    private String fullName;

    @Column(nullable = false, length = 160)
    private String email;

    @Column(length = 40)
    private String phone;

    @Column(nullable = false, length = 60)
    private String roleTitle = "Casting Coordinator";

    @Column(length = 300)
    private String assignedProjectIds;

    @Column(length = 400)
    private String assignedProjectNames;

    @Column(length = 400)
    private String permissionsJson = "VIEW_APPLICATIONS,SHORTLIST_ARTISTS,SCHEDULE_AUDITIONS";

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private LocalDateTime addedAt = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCompanyId() { return companyId; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getRoleTitle() { return roleTitle; }
    public void setRoleTitle(String roleTitle) { this.roleTitle = roleTitle; }

    public String getAssignedProjectIds() { return assignedProjectIds; }
    public void setAssignedProjectIds(String assignedProjectIds) { this.assignedProjectIds = assignedProjectIds; }

    public String getAssignedProjectNames() { return assignedProjectNames; }
    public void setAssignedProjectNames(String assignedProjectNames) { this.assignedProjectNames = assignedProjectNames; }

    public String getPermissionsJson() { return permissionsJson; }
    public void setPermissionsJson(String permissionsJson) { this.permissionsJson = permissionsJson; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public LocalDateTime getAddedAt() { return addedAt; }
    public void setAddedAt(LocalDateTime addedAt) { this.addedAt = addedAt; }
}
