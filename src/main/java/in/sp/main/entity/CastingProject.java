package in.sp.main.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * Casting call / project posted by a verified & subscribed Casting Company,
 * covering all 8 Casting Post sections (Basic info, Talent requirements, Location &
 * schedule, Payment details, Audition details, Application requirements, Contact info,
 * and Media/attachments).
 */
@Entity
@Table(name = "casting_projects")
public class CastingProject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long companyId;

    // 1. Basic project information
    @Column(nullable = false, length = 220)
    private String projectTitle;

    @Column(nullable = false, length = 80)
    private String projectType;

    @Column(length = 180)
    private String productionHouseName;

    @Column(length = 180)
    private String castingCompanyName;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String projectDetails;

    @Column(length = 120)
    private String projectLanguage = "Hindi";

    @Column(length = 60)
    private String projectStage = "Casting Open"; // Pre-production, Casting Open, Shooting Started

    // 2. Talent requirements
    @Column(nullable = false, length = 100)
    private String targetCategory;

    @Column(nullable = false, length = 40)
    private String targetGender = "Any"; // Male, Female, Any

    @Column(nullable = false)
    private int ageMin = 18;

    @Column(nullable = false)
    private int ageMax = 35;

    @Column(length = 60)
    private String heightRequirement;

    @Column(length = 220)
    private String preferredAppearance;

    @Column(length = 50)
    private String experienceLevel = "Both"; // Fresher / Experienced / Both

    private Integer artistsRequiredCount = 1;

    @Column(columnDefinition = "TEXT")
    private String roleDescription;

    @Column(length = 140)
    private String characterName;

    // 3. Project location and schedule
    @Column(length = 160)
    private String location; // Shooting location

    @Column(length = 160)
    private String auditionLocation;

    private LocalDate auditionDate;

    @Column(length = 40)
    private String auditionType = "Online"; // Online / Offline

    private LocalDate shootingStartDate;

    private LocalDate shootingEndDate;

    @Column(length = 120)
    private String shootingDuration;

    @Column(length = 240)
    private String availabilityRequirements;

    @Column(nullable = false, length = 60)
    private String durationType = "Full-Time";

    // 4. Payment details
    @Column(length = 30)
    private String paymentType = "Paid"; // Paid / Unpaid

    @Column(length = 140)
    private String budget; // Salary / Compensation

    @Column(length = 50)
    private String paymentBasis = "Per day"; // Per day / Per project / Per episode

    @Column(length = 10)
    private String accommodationProvided = "No"; // Yes / No

    @Column(length = 10)
    private String foodProvided = "No"; // Yes / No

    @Column(length = 10)
    private String travelExpensesCovered = "No"; // Yes / No

    // 5. Audition details
    @Column(columnDefinition = "TEXT")
    private String auditionProcess;

    @Column(columnDefinition = "TEXT")
    private String auditionInstructions;

    @Column(length = 10)
    private String selfIntroVideoRequired = "Yes"; // Yes / No

    @Column(length = 10)
    private String portfolioRequired = "Yes"; // Yes / No

    @Column(columnDefinition = "TEXT")
    private String auditionScript;

    private LocalDate lastDateToApply;

    // 6. Application requirements
    @Column(length = 240)
    private String requiredPhotographs;

    @Column(length = 240)
    private String introVideoRequirement;

    @Column(length = 240)
    private String actingPortfolioRequirement;

    @Column(length = 240)
    private String previousWorkLinksReq;

    @Column(length = 240)
    private String applicantContactReq;

    @Column(length = 240)
    private String additionalDocumentsReq;

    // 7. Contact information
    @Column(length = 150)
    private String castingDirectorName;

    @Column(length = 160)
    private String contactEmail;

    @Column(length = 40)
    private String contactNumber;

    @Column(length = 40)
    private String whatsappNumber;

    @Column(length = 320)
    private String websiteOrSocialLinks;

    // 8. Media and attachments
    @Column(length = 500)
    private String projectPosterPath;

    @Column(columnDefinition = "TEXT")
    private String referenceImagePathsJson;

    @Column(columnDefinition = "TEXT")
    private String characterLookPathsJson;

    @Column(length = 500)
    private String auditionScriptPdfPath;

    @Column(length = 500)
    private String projectPromotionalImagePath;

    // Workflow & Filtering Metadata
    @Column(length = 300)
    private String tags;

    @Column(nullable = false)
    private boolean onlineAuditionEnabled = true;

    @Column(nullable = false)
    private boolean promoted = false;

    @Column(nullable = false, length = 30)
    private String status = "ACTIVE";

    @Column(nullable = false)
    private LocalDate postedDate = LocalDate.now();

    @Column(nullable = false)
    private LocalDate expiryDate = LocalDate.now().plusDays(30);

    @Column(nullable = false)
    private LocalDate lastViewedDate = LocalDate.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCompanyId() { return companyId; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }

    public String getProjectTitle() { return projectTitle; }
    public void setProjectTitle(String projectTitle) { this.projectTitle = projectTitle; }

    public String getProjectType() { return projectType; }
    public void setProjectType(String projectType) { this.projectType = projectType; }

    public String getProductionHouseName() { return productionHouseName; }
    public void setProductionHouseName(String productionHouseName) { this.productionHouseName = productionHouseName; }

    public String getCastingCompanyName() { return castingCompanyName; }
    public void setCastingCompanyName(String castingCompanyName) { this.castingCompanyName = castingCompanyName; }

    public String getProjectDetails() { return projectDetails; }
    public void setProjectDetails(String projectDetails) { this.projectDetails = projectDetails; }

    public String getProjectLanguage() { return projectLanguage; }
    public void setProjectLanguage(String projectLanguage) { this.projectLanguage = projectLanguage; }

    public String getProjectStage() { return projectStage; }
    public void setProjectStage(String projectStage) { this.projectStage = projectStage; }

    public String getTargetCategory() { return targetCategory; }
    public void setTargetCategory(String targetCategory) { this.targetCategory = targetCategory; }

    public String getTargetGender() { return targetGender; }
    public void setTargetGender(String targetGender) { this.targetGender = targetGender; }

    public int getAgeMin() { return ageMin; }
    public void setAgeMin(int ageMin) { this.ageMin = ageMin; }

    public int getAgeMax() { return ageMax; }
    public void setAgeMax(int ageMax) { this.ageMax = ageMax; }

    public String getHeightRequirement() { return heightRequirement; }
    public void setHeightRequirement(String heightRequirement) { this.heightRequirement = heightRequirement; }

    public String getPreferredAppearance() { return preferredAppearance; }
    public void setPreferredAppearance(String preferredAppearance) { this.preferredAppearance = preferredAppearance; }

    public String getExperienceLevel() { return experienceLevel; }
    public void setExperienceLevel(String experienceLevel) { this.experienceLevel = experienceLevel; }

    public Integer getArtistsRequiredCount() { return artistsRequiredCount; }
    public void setArtistsRequiredCount(Integer artistsRequiredCount) { this.artistsRequiredCount = artistsRequiredCount; }

    public String getRoleDescription() { return roleDescription; }
    public void setRoleDescription(String roleDescription) { this.roleDescription = roleDescription; }

    public String getCharacterName() { return characterName; }
    public void setCharacterName(String characterName) { this.characterName = characterName; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getAuditionLocation() { return auditionLocation; }
    public void setAuditionLocation(String auditionLocation) { this.auditionLocation = auditionLocation; }

    public LocalDate getAuditionDate() { return auditionDate; }
    public void setAuditionDate(LocalDate auditionDate) { this.auditionDate = auditionDate; }

    public String getAuditionType() { return auditionType; }
    public void setAuditionType(String auditionType) { this.auditionType = auditionType; }

    public LocalDate getShootingStartDate() { return shootingStartDate; }
    public void setShootingStartDate(LocalDate shootingStartDate) { this.shootingStartDate = shootingStartDate; }

    public LocalDate getShootingEndDate() { return shootingEndDate; }
    public void setShootingEndDate(LocalDate shootingEndDate) { this.shootingEndDate = shootingEndDate; }

    public String getShootingDuration() { return shootingDuration; }
    public void setShootingDuration(String shootingDuration) { this.shootingDuration = shootingDuration; }

    public String getAvailabilityRequirements() { return availabilityRequirements; }
    public void setAvailabilityRequirements(String availabilityRequirements) { this.availabilityRequirements = availabilityRequirements; }

    public String getDurationType() { return durationType; }
    public void setDurationType(String durationType) { this.durationType = durationType; }

    public String getPaymentType() { return paymentType; }
    public void setPaymentType(String paymentType) { this.paymentType = paymentType; }

    public String getBudget() { return budget; }
    public void setBudget(String budget) { this.budget = budget; }

    public String getPaymentBasis() { return paymentBasis; }
    public void setPaymentBasis(String paymentBasis) { this.paymentBasis = paymentBasis; }

    public String getAccommodationProvided() { return accommodationProvided; }
    public void setAccommodationProvided(String accommodationProvided) { this.accommodationProvided = accommodationProvided; }

    public String getFoodProvided() { return foodProvided; }
    public void setFoodProvided(String foodProvided) { this.foodProvided = foodProvided; }

    public String getTravelExpensesCovered() { return travelExpensesCovered; }
    public void setTravelExpensesCovered(String travelExpensesCovered) { this.travelExpensesCovered = travelExpensesCovered; }

    public String getAuditionProcess() { return auditionProcess; }
    public void setAuditionProcess(String auditionProcess) { this.auditionProcess = auditionProcess; }

    public String getAuditionInstructions() { return auditionInstructions; }
    public void setAuditionInstructions(String auditionInstructions) { this.auditionInstructions = auditionInstructions; }

    public String getSelfIntroVideoRequired() { return selfIntroVideoRequired; }
    public void setSelfIntroVideoRequired(String selfIntroVideoRequired) { this.selfIntroVideoRequired = selfIntroVideoRequired; }

    public String getPortfolioRequired() { return portfolioRequired; }
    public void setPortfolioRequired(String portfolioRequired) { this.portfolioRequired = portfolioRequired; }

    public String getAuditionScript() { return auditionScript; }
    public void setAuditionScript(String auditionScript) { this.auditionScript = auditionScript; }

    public LocalDate getLastDateToApply() { return lastDateToApply; }
    public void setLastDateToApply(LocalDate lastDateToApply) { this.lastDateToApply = lastDateToApply; }

    public String getRequiredPhotographs() { return requiredPhotographs; }
    public void setRequiredPhotographs(String requiredPhotographs) { this.requiredPhotographs = requiredPhotographs; }

    public String getIntroVideoRequirement() { return introVideoRequirement; }
    public void setIntroVideoRequirement(String introVideoRequirement) { this.introVideoRequirement = introVideoRequirement; }

    public String getActingPortfolioRequirement() { return actingPortfolioRequirement; }
    public void setActingPortfolioRequirement(String actingPortfolioRequirement) { this.actingPortfolioRequirement = actingPortfolioRequirement; }

    public String getPreviousWorkLinksReq() { return previousWorkLinksReq; }
    public void setPreviousWorkLinksReq(String previousWorkLinksReq) { this.previousWorkLinksReq = previousWorkLinksReq; }

    public String getApplicantContactReq() { return applicantContactReq; }
    public void setApplicantContactReq(String applicantContactReq) { this.applicantContactReq = applicantContactReq; }

    public String getAdditionalDocumentsReq() { return additionalDocumentsReq; }
    public void setAdditionalDocumentsReq(String additionalDocumentsReq) { this.additionalDocumentsReq = additionalDocumentsReq; }

    public String getCastingDirectorName() { return castingDirectorName; }
    public void setCastingDirectorName(String castingDirectorName) { this.castingDirectorName = castingDirectorName; }

    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }

    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }

    public String getWhatsappNumber() { return whatsappNumber; }
    public void setWhatsappNumber(String whatsappNumber) { this.whatsappNumber = whatsappNumber; }

    public String getWebsiteOrSocialLinks() { return websiteOrSocialLinks; }
    public void setWebsiteOrSocialLinks(String websiteOrSocialLinks) { this.websiteOrSocialLinks = websiteOrSocialLinks; }

    public String getProjectPosterPath() { return projectPosterPath; }
    public void setProjectPosterPath(String projectPosterPath) { this.projectPosterPath = projectPosterPath; }

    public String getReferenceImagePathsJson() { return referenceImagePathsJson; }
    public void setReferenceImagePathsJson(String referenceImagePathsJson) { this.referenceImagePathsJson = referenceImagePathsJson; }

    public String getCharacterLookPathsJson() { return characterLookPathsJson; }
    public void setCharacterLookPathsJson(String characterLookPathsJson) { this.characterLookPathsJson = characterLookPathsJson; }

    public String getAuditionScriptPdfPath() { return auditionScriptPdfPath; }
    public void setAuditionScriptPdfPath(String auditionScriptPdfPath) { this.auditionScriptPdfPath = auditionScriptPdfPath; }

    public String getProjectPromotionalImagePath() { return projectPromotionalImagePath; }
    public void setProjectPromotionalImagePath(String projectPromotionalImagePath) { this.projectPromotionalImagePath = projectPromotionalImagePath; }

    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }

    public boolean isOnlineAuditionEnabled() { return onlineAuditionEnabled; }
    public void setOnlineAuditionEnabled(boolean onlineAuditionEnabled) { this.onlineAuditionEnabled = onlineAuditionEnabled; }

    public boolean isPromoted() { return promoted; }
    public void setPromoted(boolean promoted) { this.promoted = promoted; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDate getPostedDate() { return postedDate; }
    public void setPostedDate(LocalDate postedDate) { this.postedDate = postedDate; }

    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }

    public LocalDate getLastViewedDate() { return lastViewedDate; }
    public void setLastViewedDate(LocalDate lastViewedDate) { this.lastViewedDate = lastViewedDate; }
}
