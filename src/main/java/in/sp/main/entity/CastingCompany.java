package in.sp.main.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Casting Company account containing registration details, uploaded verification
 * documents, Super Admin approval status, and active subscription entitlements.
 */
@Entity
@Table(name = "casting_companies")
public class CastingCompany {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Representative & Authentication
    @Column(nullable = false, length = 150)
    private String representativeName;

    @Column(nullable = false, length = 255)
    private String passwordHash;

    @Column(length = 120)
    private String organizationType;

    @Column(length = 160)
    private String talentSought;

    @Column(length = 180)
    private String projectTypesInterest;

    // Registration Fields (exact prompt specification)
    @Column(nullable = false, length = 200)
    private String companyName;

    @Column(nullable = false, length = 500)
    private String companyLogoPath;

    @Column(nullable = false, unique = true, length = 160)
    private String companyEmail;

    @Column(nullable = false, length = 40)
    private String contactNumber;

    @Column(length = 300)
    private String website;

    @Column(nullable = false, length = 600)
    private String companyAddress;

    @Column(nullable = false, length = 120)
    private String city;

    @Column(nullable = false, length = 120)
    private String state;

    @Column(nullable = false, length = 120)
    private String country;

    private Integer yearEstablished;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String companyDescription;

    // Business & Legal Verification Documents
    @Column(length = 40)
    private String gstNumber;

    @Column(length = 500)
    private String gstCertificatePath;

    @Column(length = 500)
    private String companyRegistrationCertificatePath;

    @Column(length = 30)
    private String panNumber;

    @Column(length = 500)
    private String panDocumentPath;

    @Column(length = 500)
    private String businessAddressProofPath;

    @Column(length = 500)
    private String authorizationLetterPath;

    @Column(nullable = false)
    private boolean termsAccepted = true;

    // 5. Admin Verification and Approval Fields (exact prompt specification)
    @Column(nullable = false, length = 30)
    private String accountStatus = "Pending"; // Pending / Approved / Rejected

    @Column(nullable = false, length = 30)
    private String emailVerification = "Not verified"; // Verified / Not verified

    @Column(nullable = false, length = 30)
    private String mobileVerification = "Not verified"; // Verified / Not verified

    @Column(nullable = false, length = 30)
    private String documentVerification = "Pending"; // Pending / Verified / Rejected

    @Column(columnDefinition = "TEXT")
    private String adminRemarks;

    @Column(length = 160)
    private String verifiedBy;

    private LocalDate verificationDate;

    @Column(nullable = false, length = 30)
    private String accountSuspension = "Active"; // Active / Suspended

    // Recruiter Profile & Subscription Pass State
    @Column(nullable = false)
    private boolean verifiedBadge = false;

    @Column(nullable = false)
    private int followersCount = 46;

    @Column(length = 60)
    private String activePassCode;

    @Column(length = 120)
    private String activePassName;

    @Column(nullable = false)
    private boolean subscriptionActive = false;

    private LocalDate subscriptionExpiry;

    @Column(nullable = false)
    private int contactViewsTotal = 0;

    @Column(nullable = false)
    private int contactViewsUsed = 0;

    @Column(nullable = false)
    private int portfolioViewsTotal = 0; // -1 means Unlimited

    @Column(nullable = false)
    private int portfolioViewsUsed = 0;

    @Column(nullable = false)
    private int auditionInvitesTotal = 0;

    @Column(nullable = false)
    private int auditionInvitesUsed = 0;

    @Column(nullable = false)
    private int projectPromotionsRemaining = 0;

    @Column(columnDefinition = "TEXT")
    private String showreelVideosJson;

    @Column(columnDefinition = "TEXT")
    private String portfolioPhotosJson;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRepresentativeName() { return representativeName; }
    public void setRepresentativeName(String representativeName) { this.representativeName = representativeName; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getOrganizationType() { return organizationType; }
    public void setOrganizationType(String organizationType) { this.organizationType = organizationType; }

    public String getTalentSought() { return talentSought; }
    public void setTalentSought(String talentSought) { this.talentSought = talentSought; }

    public String getProjectTypesInterest() { return projectTypesInterest; }
    public void setProjectTypesInterest(String projectTypesInterest) { this.projectTypesInterest = projectTypesInterest; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getCompanyLogoPath() { return companyLogoPath; }
    public void setCompanyLogoPath(String companyLogoPath) { this.companyLogoPath = companyLogoPath; }

    public String getCompanyEmail() { return companyEmail; }
    public void setCompanyEmail(String companyEmail) { this.companyEmail = companyEmail; }

    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }

    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }

    public String getCompanyAddress() { return companyAddress; }
    public void setCompanyAddress(String companyAddress) { this.companyAddress = companyAddress; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public Integer getYearEstablished() { return yearEstablished; }
    public void setYearEstablished(Integer yearEstablished) { this.yearEstablished = yearEstablished; }

    public String getCompanyDescription() { return companyDescription; }
    public void setCompanyDescription(String companyDescription) { this.companyDescription = companyDescription; }

    public String getGstNumber() { return gstNumber; }
    public void setGstNumber(String gstNumber) { this.gstNumber = gstNumber; }

    public String getGstCertificatePath() { return gstCertificatePath; }
    public void setGstCertificatePath(String gstCertificatePath) { this.gstCertificatePath = gstCertificatePath; }

    public String getCompanyRegistrationCertificatePath() { return companyRegistrationCertificatePath; }
    public void setCompanyRegistrationCertificatePath(String companyRegistrationCertificatePath) { this.companyRegistrationCertificatePath = companyRegistrationCertificatePath; }

    public String getPanNumber() { return panNumber; }
    public void setPanNumber(String panNumber) { this.panNumber = panNumber; }

    public String getPanDocumentPath() { return panDocumentPath; }
    public void setPanDocumentPath(String panDocumentPath) { this.panDocumentPath = panDocumentPath; }

    public String getBusinessAddressProofPath() { return businessAddressProofPath; }
    public void setBusinessAddressProofPath(String businessAddressProofPath) { this.businessAddressProofPath = businessAddressProofPath; }

    public String getAuthorizationLetterPath() { return authorizationLetterPath; }
    public void setAuthorizationLetterPath(String authorizationLetterPath) { this.authorizationLetterPath = authorizationLetterPath; }

    public boolean isTermsAccepted() { return termsAccepted; }
    public void setTermsAccepted(boolean termsAccepted) { this.termsAccepted = termsAccepted; }

    public String getAccountStatus() { return accountStatus; }
    public void setAccountStatus(String accountStatus) { this.accountStatus = accountStatus; }

    public String getEmailVerification() { return emailVerification; }
    public void setEmailVerification(String emailVerification) { this.emailVerification = emailVerification; }

    public String getMobileVerification() { return mobileVerification; }
    public void setMobileVerification(String mobileVerification) { this.mobileVerification = mobileVerification; }

    public String getDocumentVerification() { return documentVerification; }
    public void setDocumentVerification(String documentVerification) { this.documentVerification = documentVerification; }

    public String getAdminRemarks() { return adminRemarks; }
    public void setAdminRemarks(String adminRemarks) { this.adminRemarks = adminRemarks; }

    public String getVerifiedBy() { return verifiedBy; }
    public void setVerifiedBy(String verifiedBy) { this.verifiedBy = verifiedBy; }

    public LocalDate getVerificationDate() { return verificationDate; }
    public void setVerificationDate(LocalDate verificationDate) { this.verificationDate = verificationDate; }

    public String getAccountSuspension() { return accountSuspension; }
    public void setAccountSuspension(String accountSuspension) { this.accountSuspension = accountSuspension; }

    public boolean isVerifiedBadge() { return verifiedBadge; }
    public void setVerifiedBadge(boolean verifiedBadge) { this.verifiedBadge = verifiedBadge; }

    public int getFollowersCount() { return followersCount; }
    public void setFollowersCount(int followersCount) { this.followersCount = followersCount; }

    public String getActivePassCode() { return activePassCode; }
    public void setActivePassCode(String activePassCode) { this.activePassCode = activePassCode; }

    public String getActivePassName() { return activePassName; }
    public void setActivePassName(String activePassName) { this.activePassName = activePassName; }

    public boolean isSubscriptionActive() { return subscriptionActive; }
    public void setSubscriptionActive(boolean subscriptionActive) { this.subscriptionActive = subscriptionActive; }

    public LocalDate getSubscriptionExpiry() { return subscriptionExpiry; }
    public void setSubscriptionExpiry(LocalDate subscriptionExpiry) { this.subscriptionExpiry = subscriptionExpiry; }

    public int getContactViewsTotal() { return contactViewsTotal; }
    public void setContactViewsTotal(int contactViewsTotal) { this.contactViewsTotal = contactViewsTotal; }

    public int getContactViewsUsed() { return contactViewsUsed; }
    public void setContactViewsUsed(int contactViewsUsed) { this.contactViewsUsed = contactViewsUsed; }

    public int getPortfolioViewsTotal() { return portfolioViewsTotal; }
    public void setPortfolioViewsTotal(int portfolioViewsTotal) { this.portfolioViewsTotal = portfolioViewsTotal; }

    public int getPortfolioViewsUsed() { return portfolioViewsUsed; }
    public void setPortfolioViewsUsed(int portfolioViewsUsed) { this.portfolioViewsUsed = portfolioViewsUsed; }

    public int getAuditionInvitesTotal() { return auditionInvitesTotal; }
    public void setAuditionInvitesTotal(int auditionInvitesTotal) { this.auditionInvitesTotal = auditionInvitesTotal; }

    public int getAuditionInvitesUsed() { return auditionInvitesUsed; }
    public void setAuditionInvitesUsed(int auditionInvitesUsed) { this.auditionInvitesUsed = auditionInvitesUsed; }

    public int getProjectPromotionsRemaining() { return projectPromotionsRemaining; }
    public void setProjectPromotionsRemaining(int projectPromotionsRemaining) { this.projectPromotionsRemaining = projectPromotionsRemaining; }

    public String getShowreelVideosJson() { return showreelVideosJson; }
    public void setShowreelVideosJson(String showreelVideosJson) { this.showreelVideosJson = showreelVideosJson; }

    public String getPortfolioPhotosJson() { return portfolioPhotosJson; }
    public void setPortfolioPhotosJson(String portfolioPhotosJson) { this.portfolioPhotosJson = portfolioPhotosJson; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
