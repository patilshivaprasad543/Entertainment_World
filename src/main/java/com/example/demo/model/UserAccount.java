package com.example.demo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
    name = "user_accounts",
    uniqueConstraints = @UniqueConstraint(columnNames = {"email", "role"})
)
public class UserAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32, columnDefinition = "VARCHAR(32)")
    private UserRole role;

    // Professional profile fields
    @Column(length = 60)
    private String professionalCategory;

    private Integer actingStartAge;

    @Column(length = 20)
    private String dateOfBirth;

    @Column(length = 60)
    private String experienceCategory;

    @Column(length = 30)
    private String gender;

    @Column(length = 255)
    private String profilePhotoFileName;

    @Column(length = 30)
    private String phone;

    @Column(length = 80)
    private String city;

    @Column(length = 500)
    private String portfolioOrBio;

    // Document verification fields (Vendors / Event Management, Casting Productions, Brand Promotions)
    @Column(length = 150)
    private String organizationName;

    @Column(length = 100)
    private String documentType;

    @Column(length = 100)
    private String documentNumber;

    @Column(length = 255)
    private String documentFileName;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private VerificationStatus verificationStatus;

    protected UserAccount() {
    }

    public UserAccount(String fullName, String email, String password, UserRole role) {
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.role = role;
        this.verificationStatus = VerificationStatus.APPROVED;
    }

    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public UserRole getRole() {
        return role;
    }

    public String getProfessionalCategory() {
        return professionalCategory;
    }

    public void setProfessionalCategory(String professionalCategory) {
        this.professionalCategory = professionalCategory;
    }

    public Integer getActingStartAge() {
        return actingStartAge;
    }

    public void setActingStartAge(Integer actingStartAge) {
        this.actingStartAge = actingStartAge;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getExperienceCategory() {
        return experienceCategory;
    }

    public void setExperienceCategory(String experienceCategory) {
        this.experienceCategory = experienceCategory;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getProfilePhotoFileName() {
        return profilePhotoFileName;
    }

    public void setProfilePhotoFileName(String profilePhotoFileName) {
        this.profilePhotoFileName = profilePhotoFileName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPortfolioOrBio() {
        return portfolioOrBio;
    }

    public void setPortfolioOrBio(String portfolioOrBio) {
        this.portfolioOrBio = portfolioOrBio;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public String getDocumentFileName() {
        return documentFileName;
    }

    public void setDocumentFileName(String documentFileName) {
        this.documentFileName = documentFileName;
    }

    public VerificationStatus getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(VerificationStatus verificationStatus) {
        this.verificationStatus = verificationStatus;
    }
}
