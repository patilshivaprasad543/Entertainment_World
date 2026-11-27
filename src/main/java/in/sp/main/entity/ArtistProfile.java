package in.sp.main.entity;

import jakarta.persistence.*;

/**
 * Talent profile browsed, shortlisted, invited, and exported by Casting Companies.
 */
@Entity
@Table(name = "artist_profiles")
public class ArtistProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 140)
    private String fullName;

    @Column(nullable = false, length = 80)
    private String category;

    @Column(length = 180)
    private String secondaryRoles;

    @Column(nullable = false, length = 20)
    private String gender;

    @Column(nullable = false)
    private int age;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(nullable = false, length = 120)
    private String agencyName;

    @Column(nullable = false, length = 50)
    private String expertise;

    @Column(nullable = false, length = 180)
    private String languages;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(nullable = false, length = 400)
    private String avatarUrl;

    @Column(nullable = false)
    private boolean hasVideo;

    @Column(length = 160)
    private String videoTitle;

    @Column(length = 400)
    private String videoUrl;

    @Column(nullable = false)
    private boolean showcaseBadge;

    @Column(nullable = false)
    private boolean platinumTier;

    @Column(nullable = false)
    private boolean featuredTier;

    @Column(nullable = false)
    private boolean risingStar;

    @Column(nullable = false, length = 160)
    private String email;

    @Column(nullable = false, length = 40)
    private String phone;

    @Column(nullable = false)
    private int viewsCount = 120;

    @Column(length = 300)
    private String keywords;

    @Column(nullable = false)
    private boolean profileVerified = false;

    @Column(length = 120)
    private String verificationStatus = "Pending"; // Pending, Verified, Rejected

    @Column(length = 400)
    private String verificationNotes;

    @Column(length = 400)
    private String portfolioUrl;

    @Column(length = 300)
    private String skills;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getSecondaryRoles() { return secondaryRoles; }
    public void setSecondaryRoles(String secondaryRoles) { this.secondaryRoles = secondaryRoles; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getAgencyName() { return agencyName; }
    public void setAgencyName(String agencyName) { this.agencyName = agencyName; }

    public String getExpertise() { return expertise; }
    public void setExpertise(String expertise) { this.expertise = expertise; }

    public String getLanguages() { return languages; }
    public void setLanguages(String languages) { this.languages = languages; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public boolean isHasVideo() { return hasVideo; }
    public void setHasVideo(boolean hasVideo) { this.hasVideo = hasVideo; }

    public String getVideoTitle() { return videoTitle; }
    public void setVideoTitle(String videoTitle) { this.videoTitle = videoTitle; }

    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }

    public boolean isShowcaseBadge() { return showcaseBadge; }
    public void setShowcaseBadge(boolean showcaseBadge) { this.showcaseBadge = showcaseBadge; }

    public boolean isPlatinumTier() { return platinumTier; }
    public void setPlatinumTier(boolean platinumTier) { this.platinumTier = platinumTier; }

    public boolean isFeaturedTier() { return featuredTier; }
    public void setFeaturedTier(boolean featuredTier) { this.featuredTier = featuredTier; }

    public boolean isRisingStar() { return risingStar; }
    public void setRisingStar(boolean risingStar) { this.risingStar = risingStar; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public int getViewsCount() { return viewsCount; }
    public void setViewsCount(int viewsCount) { this.viewsCount = viewsCount; }

    public String getKeywords() { return keywords; }
    public void setKeywords(String keywords) { this.keywords = keywords; }

    public boolean isProfileVerified() { return profileVerified; }
    public void setProfileVerified(boolean profileVerified) { this.profileVerified = profileVerified; }

    public String getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(String verificationStatus) { this.verificationStatus = verificationStatus; }

    public String getVerificationNotes() { return verificationNotes; }
    public void setVerificationNotes(String verificationNotes) { this.verificationNotes = verificationNotes; }

    public String getPortfolioUrl() { return portfolioUrl; }
    public void setPortfolioUrl(String portfolioUrl) { this.portfolioUrl = portfolioUrl; }

    public String getSkills() { return skills; }
    public void setSkills(String skills) { this.skills = skills; }
}
