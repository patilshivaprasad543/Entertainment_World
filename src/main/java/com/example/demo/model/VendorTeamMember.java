package com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "vendor_team_members", indexes = {
        @Index(name = "idx_vendor_team_vendor", columnList = "vendor_account_id")
})
public class VendorTeamMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "vendor_account_id", nullable = false)
    private UserAccount vendor;

    @Column(nullable = false, length = 120)
    private String fullName;

    @Column(nullable = false, length = 150)
    private String email;

    @Column(length = 30)
    private String phone;

    @Column(nullable = false, length = 40)
    private String role = "STAFF"; // OWNER, MANAGER, BOOKING_MANAGER, STAFF

    @Column(length = 500)
    private String permissions;

    @Column(nullable = false)
    private boolean active = true;

    private LocalDateTime invitedAt;
    private LocalDateTime updatedAt;

    public VendorTeamMember() {
    }

    public Long getId() { return id; }
    public UserAccount getVendor() { return vendor; }
    public void setVendor(UserAccount vendor) { this.vendor = vendor; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getPermissions() { return permissions; }
    public void setPermissions(String permissions) { this.permissions = permissions; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public LocalDateTime getInvitedAt() { return invitedAt; }
    public void setInvitedAt(LocalDateTime invitedAt) { this.invitedAt = invitedAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
