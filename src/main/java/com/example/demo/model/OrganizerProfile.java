package com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "organizer_profiles")
public class OrganizerProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "user_account_id", nullable = false, unique = true)
    private UserAccount userAccount;

    @Column(nullable = false, length = 150)
    private String companyName;

    @Column(length = 255)
    private String logoFileName;

    @Column(length = 255)
    private String coverImageFileName;

    @Column(length = 2000)
    private String aboutUs;

    @Column(length = 500)
    private String bio;

    @Column(length = 200)
    private String specialization;

    @Column(length = 80)
    private String country = "India";

    @Column(length = 80)
    private String state;

    @Column(length = 80)
    private String city;

    @Column(length = 120)
    private String area;

    @Column(length = 500)
    private String address;

    @Column(length = 30)
    private String contactNumber;

    @Column(length = 150)
    private String email;

    @Column(length = 255)
    private String website;

    @Column(length = 500)
    private String socialLinks;

    private LocalDateTime updatedAt;

    public OrganizerProfile() {}

    public Long getId() { return id; }
    public UserAccount getUserAccount() { return userAccount; }
    public void setUserAccount(UserAccount userAccount) { this.userAccount = userAccount; }
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public String getLogoFileName() { return logoFileName; }
    public void setLogoFileName(String logoFileName) { this.logoFileName = logoFileName; }
    public String getCoverImageFileName() { return coverImageFileName; }
    public void setCoverImageFileName(String coverImageFileName) { this.coverImageFileName = coverImageFileName; }
    public String getAboutUs() { return aboutUs; }
    public void setAboutUs(String aboutUs) { this.aboutUs = aboutUs; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }
    public String getSocialLinks() { return socialLinks; }
    public void setSocialLinks(String socialLinks) { this.socialLinks = socialLinks; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
