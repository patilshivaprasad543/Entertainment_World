package com.example.demo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "vendor_profiles")
public class VendorProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "user_account_id", nullable = false, unique = true)
    private UserAccount userAccount;

    @Column(nullable = false, length = 150)
    private String companyName;

    @Column(nullable = false, length = 120)
    private String contactPerson;

    @Column(nullable = false, length = 150)
    private String businessEmail;

    @Column(length = 120)
    private String designation;

    @Column(length = 30)
    private String phone;

    @Column(length = 30)
    private String alternatePhone;

    private Integer yearsInBusiness;

    private Integer venuesManaged;

    @Column(length = 255)
    private String address;

    @Column(length = 80)
    private String country;

    @Column(length = 80)
    private String state;

    @Column(length = 80)
    private String city;

    @Column(length = 20)
    private String pincode;

    @Column(length = 255)
    private String websiteUrl;

    @Column(length = 40)
    private String gstNumber;

    @Column(length = 500)
    private String bio;

    @Column(length = 2000)
    private String aboutUs;

    @Column(length = 1000)
    private String description;

    @Column(length = 255)
    private String logoFileName;

    @Column(length = 255)
    private String coverImageFileName;

    private Boolean acceptedTerms = false;

    private Boolean acceptedPrivacyPolicy = false;

    private LocalDateTime updatedAt;

    public VendorProfile() {
    }

    public Long getId() {
        return id;
    }

    public UserAccount getUserAccount() {
        return userAccount;
    }

    public void setUserAccount(UserAccount userAccount) {
        this.userAccount = userAccount;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }

    public String getBusinessEmail() {
        return businessEmail;
    }

    public void setBusinessEmail(String businessEmail) {
        this.businessEmail = businessEmail;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLogoFileName() {
        return logoFileName;
    }

    public void setLogoFileName(String logoFileName) {
        this.logoFileName = logoFileName;
    }

    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }

    public String getAlternatePhone() { return alternatePhone; }
    public void setAlternatePhone(String alternatePhone) { this.alternatePhone = alternatePhone; }

    public Integer getYearsInBusiness() { return yearsInBusiness; }
    public void setYearsInBusiness(Integer yearsInBusiness) { this.yearsInBusiness = yearsInBusiness; }

    public Integer getVenuesManaged() { return venuesManaged; }
    public void setVenuesManaged(Integer venuesManaged) { this.venuesManaged = venuesManaged; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getPincode() { return pincode; }
    public void setPincode(String pincode) { this.pincode = pincode; }

    public String getWebsiteUrl() { return websiteUrl; }
    public void setWebsiteUrl(String websiteUrl) { this.websiteUrl = websiteUrl; }

    public String getGstNumber() { return gstNumber; }
    public void setGstNumber(String gstNumber) { this.gstNumber = gstNumber; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getAboutUs() { return aboutUs; }
    public void setAboutUs(String aboutUs) { this.aboutUs = aboutUs; }

    public String getCoverImageFileName() { return coverImageFileName; }
    public void setCoverImageFileName(String coverImageFileName) { this.coverImageFileName = coverImageFileName; }

    public boolean isAcceptedTerms() { return Boolean.TRUE.equals(acceptedTerms); }
    public void setAcceptedTerms(boolean acceptedTerms) { this.acceptedTerms = acceptedTerms; }

    public boolean isAcceptedPrivacyPolicy() { return Boolean.TRUE.equals(acceptedPrivacyPolicy); }
    public void setAcceptedPrivacyPolicy(boolean acceptedPrivacyPolicy) { this.acceptedPrivacyPolicy = acceptedPrivacyPolicy; }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
