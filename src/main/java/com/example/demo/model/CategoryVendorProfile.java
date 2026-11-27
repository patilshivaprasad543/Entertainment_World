package com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "category_vendor_profiles")
public class CategoryVendorProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "user_account_id", nullable = false, unique = true)
    private UserAccount userAccount;

    @Column(nullable = false, length = 80)
    private String vendorCategory;

    @Column(nullable = false, length = 150)
    private String businessName;

    @Column(nullable = false, length = 120)
    private String contactPerson;

    @Column(length = 255)
    private String logoFileName;

    @Column(nullable = false, length = 150)
    private String email;

    @Column(length = 30)
    private String phone;

    @Column(length = 80)
    private String country = "India";

    @Column(length = 80)
    private String state;

    @Column(length = 80)
    private String city;

    @Column(length = 120)
    private String areaLocality;

    @Column(length = 500)
    private String address;

    @Column(length = 20)
    private String pincode;

    @Column(length = 500)
    private String mapLink;

    @Column(length = 2000)
    private String aboutBusiness;

    @Column(columnDefinition = "TEXT")
    private String categoryDetailsJson;

    @Column(length = 255)
    private String coverImageFileName;

    @Column(length = 30)
    private String alternatePhone;

    private Integer yearsExperience = 0;

    @Column(length = 255)
    private String websiteUrl;

    @Column(length = 500)
    private String socialLinks;

    @Column(length = 120)
    private String workingHours = "09:00 AM - 08:00 PM";

    @Column(length = 300)
    private String serviceAreas;

    @Column(length = 200)
    private String languagesSupported = "English, Hindi, Telugu";

    private boolean featuredListing = false;
    private boolean profileVisible = true;
    private boolean notifyNewEnquiry = true;
    private boolean notifyMessages = true;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public CategoryVendorProfile() {}

    public Long getId() { return id; }
    public UserAccount getUserAccount() { return userAccount; }
    public void setUserAccount(UserAccount userAccount) { this.userAccount = userAccount; }
    public String getVendorCategory() { return vendorCategory; }
    public void setVendorCategory(String vendorCategory) { this.vendorCategory = vendorCategory; }
    public String getBusinessName() { return businessName; }
    public void setBusinessName(String businessName) { this.businessName = businessName; }
    public String getContactPerson() { return contactPerson; }
    public void setContactPerson(String contactPerson) { this.contactPerson = contactPerson; }
    public String getLogoFileName() { return logoFileName; }
    public void setLogoFileName(String logoFileName) { this.logoFileName = logoFileName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getAreaLocality() { return areaLocality; }
    public void setAreaLocality(String areaLocality) { this.areaLocality = areaLocality; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getPincode() { return pincode; }
    public void setPincode(String pincode) { this.pincode = pincode; }
    public String getMapLink() { return mapLink; }
    public void setMapLink(String mapLink) { this.mapLink = mapLink; }
    public String getAboutBusiness() { return aboutBusiness; }
    public void setAboutBusiness(String aboutBusiness) { this.aboutBusiness = aboutBusiness; }
    public String getCategoryDetailsJson() { return categoryDetailsJson; }
    public void setCategoryDetailsJson(String categoryDetailsJson) { this.categoryDetailsJson = categoryDetailsJson; }
    public String getCoverImageFileName() { return coverImageFileName; }
    public void setCoverImageFileName(String coverImageFileName) { this.coverImageFileName = coverImageFileName; }
    public String getAlternatePhone() { return alternatePhone; }
    public void setAlternatePhone(String alternatePhone) { this.alternatePhone = alternatePhone; }
    public Integer getYearsExperience() { return yearsExperience; }
    public void setYearsExperience(Integer yearsExperience) { this.yearsExperience = yearsExperience; }
    public String getWebsiteUrl() { return websiteUrl; }
    public void setWebsiteUrl(String websiteUrl) { this.websiteUrl = websiteUrl; }
    public String getSocialLinks() { return socialLinks; }
    public void setSocialLinks(String socialLinks) { this.socialLinks = socialLinks; }
    public String getWorkingHours() { return workingHours; }
    public void setWorkingHours(String workingHours) { this.workingHours = workingHours; }
    public String getServiceAreas() { return serviceAreas; }
    public void setServiceAreas(String serviceAreas) { this.serviceAreas = serviceAreas; }
    public String getLanguagesSupported() { return languagesSupported; }
    public void setLanguagesSupported(String languagesSupported) { this.languagesSupported = languagesSupported; }
    public boolean isFeaturedListing() { return featuredListing; }
    public void setFeaturedListing(boolean featuredListing) { this.featuredListing = featuredListing; }
    public boolean isProfileVisible() { return profileVisible; }
    public void setProfileVisible(boolean profileVisible) { this.profileVisible = profileVisible; }
    public boolean isNotifyNewEnquiry() { return notifyNewEnquiry; }
    public void setNotifyNewEnquiry(boolean notifyNewEnquiry) { this.notifyNewEnquiry = notifyNewEnquiry; }
    public boolean isNotifyMessages() { return notifyMessages; }
    public void setNotifyMessages(boolean notifyMessages) { this.notifyMessages = notifyMessages; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
