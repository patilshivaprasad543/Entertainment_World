package com.talentplatform.brand.model;

import jakarta.persistence.*;

@Entity @Table(name = "team_members", uniqueConstraints = @UniqueConstraint(columnNames = {"brand_id", "email"}))
public class TeamMember {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "brand_id") private Long brandId;
    private String fullName;
    private String email;
    private String designation;
    private String role = "MEMBER";
    public Long getId() { return id; }
    public Long getBrandId() { return brandId; }
    public void setBrandId(Long brandId) { this.brandId = brandId; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
