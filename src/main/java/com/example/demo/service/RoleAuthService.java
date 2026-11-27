package com.example.demo.service;

import com.example.demo.dto.AuthResponse;
import com.example.demo.dto.LoginRequest;
import com.example.demo.model.CategoryVendorProfile;
import com.example.demo.model.UserAccount;
import com.example.demo.model.UserRole;
import com.example.demo.model.VendorProfile;
import com.example.demo.model.VerificationStatus;
import com.example.demo.repository.CategoryVendorProfileRepository;
import com.example.demo.repository.UserAccountRepository;
import com.example.demo.repository.VendorProfileRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class RoleAuthService {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private static final Set<String> PROFESSIONAL_CATEGORIES = Set.of(
            "Actor",
            "Actress",
            "Influencer",
            "Child Artist",
            "Model",
            "Voice Artist / Dubbing",
            "Dancer / Choreographer",
            "Singer / Musician",
            "Comedian",
            "Anchor / Host",
            "Stunt Performer",
            "Director / Filmmaker",
            "Scriptwriter / Lyricist"
    );

    public static final Path UPLOAD_DIR = Paths.get("uploads", "verification-docs").toAbsolutePath().normalize();
    public static final Path PHOTO_DIR = Paths.get("uploads", "profile-photos").toAbsolutePath().normalize();

    private final UserAccountRepository userAccountRepository;
    private final VendorProfileRepository vendorProfileRepository;
    private final CategoryVendorProfileRepository categoryVendorProfileRepository;
    private final SessionAuthService sessionAuthService;

    public RoleAuthService(
            UserAccountRepository userAccountRepository,
            VendorProfileRepository vendorProfileRepository,
            CategoryVendorProfileRepository categoryVendorProfileRepository,
            SessionAuthService sessionAuthService) {
        this.userAccountRepository = userAccountRepository;
        this.vendorProfileRepository = vendorProfileRepository;
        this.categoryVendorProfileRepository = categoryVendorProfileRepository;
        this.sessionAuthService = sessionAuthService;
    }

    public ResponseEntity<AuthResponse> registerProfessional(
            String fullNameRaw,
            String emailRaw,
            String passwordRaw,
            String categoryRaw,
            Integer actingStartAge,
            String dateOfBirthRaw,
            String experienceCategoryRaw,
            String genderRaw,
            String phoneRaw,
            String cityRaw,
            String portfolioOrBioRaw,
            MultipartFile photoFile
    ) {
        String fullName = trimOrEmpty(fullNameRaw);
        String email = trimOrEmpty(emailRaw).toLowerCase();
        String password = passwordRaw == null ? "" : passwordRaw;
        String category = trimOrEmpty(categoryRaw);
        String dateOfBirth = trimOrEmpty(dateOfBirthRaw);
        String experienceCategory = trimOrEmpty(experienceCategoryRaw);
        String gender = trimOrEmpty(genderRaw);
        String phone = trimOrEmpty(phoneRaw);
        String city = trimOrEmpty(cityRaw);
        String portfolioOrBio = trimOrEmpty(portfolioOrBioRaw);

        ResponseEntity<AuthResponse> validationError = validateBaseCredentials(fullName, email, password, UserRole.ACTOR);
        if (validationError != null) {
            return validationError;
        }
        if (!PROFESSIONAL_CATEGORIES.contains(category)) {
            return error(HttpStatus.BAD_REQUEST, "Please select a valid professional category.");
        }
        if (actingStartAge == null || actingStartAge < 1 || actingStartAge > 100) {
            return error(HttpStatus.BAD_REQUEST, "Please enter a valid acting/career start age (1–100).");
        }
        if (phone.isEmpty() || city.isEmpty()) {
            return error(HttpStatus.BAD_REQUEST, "Phone number and city are required for your profile.");
        }
        if (photoFile == null || photoFile.isEmpty()) {
            return error(HttpStatus.BAD_REQUEST, "Please upload a profile photo (JPG, PNG, or WEBP).");
        }

        String storedPhoto;
        try {
            storedPhoto = storeFile(photoFile, PHOTO_DIR);
        } catch (IOException ex) {
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to save profile photo.");
        }

        UserAccount account = new UserAccount(fullName, email, password, UserRole.ACTOR);
        account.setProfessionalCategory(category);
        account.setActingStartAge(actingStartAge);
        account.setDateOfBirth(dateOfBirth.isEmpty() ? null : dateOfBirth);
        account.setExperienceCategory(experienceCategory.isEmpty() ? null : experienceCategory);
        account.setGender(gender.isEmpty() ? null : gender);
        account.setProfilePhotoFileName(storedPhoto);
        account.setPhone(phone);
        account.setCity(city);
        account.setPortfolioOrBio(portfolioOrBio);
        account.setVerificationStatus(VerificationStatus.APPROVED);

        UserAccount saved = userAccountRepository.save(account);
        return ResponseEntity.status(HttpStatus.CREATED).body(new AuthResponse(
                true,
                category + " profile created successfully!",
                saved.getFullName(),
                saved.getEmail(),
                UserRole.ACTOR.name(),
                saved.getProfessionalCategory(),
                saved.getActingStartAge(),
                saved.getDateOfBirth(),
                saved.getExperienceCategory(),
                saved.getGender(),
                buildPhotoUrl(saved.getProfilePhotoFileName()),
                UserRole.ACTOR.getDashboardUrl()
        ));
    }

    public ResponseEntity<AuthResponse> registerInfluencer(
            String fullNameRaw,
            String emailRaw,
            String passwordRaw,
            String phoneRaw,
            String cityRaw,
            String bioRaw
    ) {
        String fullName = trimOrEmpty(fullNameRaw);
        String email = trimOrEmpty(emailRaw).toLowerCase();
        String password = passwordRaw == null ? "" : passwordRaw;

        ResponseEntity<AuthResponse> validationError = validateBaseCredentials(fullName, email, password, UserRole.INFLUENCER);
        if (validationError != null) {
            return validationError;
        }

        UserAccount account = new UserAccount(fullName, email, password, UserRole.INFLUENCER);
        account.setProfessionalCategory("Influencer");
        account.setPhone(trimOrNull(phoneRaw));
        account.setCity(trimOrNull(cityRaw));
        account.setPortfolioOrBio(trimOrNull(bioRaw));
        account.setVerificationStatus(VerificationStatus.APPROVED);

        UserAccount saved = userAccountRepository.save(account);
        return ResponseEntity.status(HttpStatus.CREATED).body(new AuthResponse(
                true,
                "Influencer profile created successfully!",
                saved.getFullName(),
                saved.getEmail(),
                UserRole.INFLUENCER.name(),
                "Influencer",
                null,
                null,
                null,
                null,
                null,
                UserRole.INFLUENCER.getDashboardUrl()
        ));
    }

    public ResponseEntity<AuthResponse> registerWithDocuments(
            UserRole role,
            String fullNameRaw,
            String emailRaw,
            String passwordRaw,
            String organizationNameRaw,
            String documentTypeRaw,
            String documentNumberRaw,
            MultipartFile documentFile
    ) {
        String fullName = trimOrEmpty(fullNameRaw);
        String email = trimOrEmpty(emailRaw).toLowerCase();
        String password = passwordRaw == null ? "" : passwordRaw;
        String organizationName = trimOrEmpty(organizationNameRaw);
        String documentType = trimOrEmpty(documentTypeRaw);
        String documentNumber = trimOrEmpty(documentNumberRaw);

        ResponseEntity<AuthResponse> validationError = validateBaseCredentials(fullName, email, password, role);
        if (validationError != null) {
            return validationError;
        }
        if (organizationName.isEmpty() || documentType.isEmpty() || documentNumber.isEmpty()) {
            return error(HttpStatus.BAD_REQUEST, "Organization name, document type, and document number are required.");
        }
        if (documentFile == null || documentFile.isEmpty()) {
            return error(HttpStatus.BAD_REQUEST, "Please upload a verification document (PDF, JPG, or PNG).");
        }

        String storedFileName;
        try {
            storedFileName = storeFile(documentFile, UPLOAD_DIR);
        } catch (IOException ex) {
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to store verification document.");
        }

        UserAccount account = new UserAccount(fullName, email, password, role);
        account.setOrganizationName(organizationName);
        account.setDocumentType(documentType);
        account.setDocumentNumber(documentNumber);
        account.setDocumentFileName(storedFileName);
        boolean autoApproveVendor = (role == UserRole.VENDOR);
        account.setVerificationStatus(autoApproveVendor ? VerificationStatus.APPROVED : VerificationStatus.PENDING);

        UserAccount saved = userAccountRepository.save(account);
        String msg = autoApproveVendor
                ? "Vendor registration successful! You can now sign in to Vendors Management Dashboard."
                : "Registration submitted! Your documents are pending Admin verification before you can log in.";
        return ResponseEntity.status(HttpStatus.CREATED).body(new AuthResponse(
                true,
                msg,
                saved.getFullName(),
                saved.getEmail(),
                role.name(),
                null,
                null,
                null,
                null,
                null,
                null,
                role.getDashboardUrl()
        ));
    }

    public ResponseEntity<AuthResponse> registerVendorWithProfile(
            String fullNameRaw,
            String emailRaw,
            String passwordRaw,
            String organizationNameRaw,
            String designationRaw,
            String phoneRaw,
            String alternatePhoneRaw,
            Integer yearsInBusiness,
            Integer venuesManaged,
            String countryRaw,
            String stateRaw,
            String cityRaw,
            String pincodeRaw,
            String websiteUrlRaw,
            String bioRaw,
            String aboutUsRaw,
            String documentTypeRaw,
            String documentNumberRaw,
            MultipartFile documentFile,
            MultipartFile logoFile,
            MultipartFile coverFile,
            boolean acceptedTerms,
            boolean acceptedPrivacyPolicy
    ) {
        if (!acceptedTerms || !acceptedPrivacyPolicy) {
            return error(HttpStatus.BAD_REQUEST, "Please accept Terms & Conditions and Privacy Policy.");
        }

        ResponseEntity<AuthResponse> baseRes = registerWithDocuments(
                UserRole.VENDOR, fullNameRaw, emailRaw, passwordRaw,
                organizationNameRaw, documentTypeRaw, documentNumberRaw, documentFile);
        if (!baseRes.getStatusCode().is2xxSuccessful() || baseRes.getBody() == null) {
            return baseRes;
        }

        UserAccount savedVendor = userAccountRepository
                .findByEmailAndRole(baseRes.getBody().email(), UserRole.VENDOR)
                .orElseThrow();

        VendorProfile profile = new VendorProfile();
        profile.setUserAccount(savedVendor);
        profile.setCompanyName(trimOrEmpty(organizationNameRaw));
        profile.setContactPerson(trimOrEmpty(fullNameRaw));
        profile.setBusinessEmail(savedVendor.getEmail());
        profile.setDesignation(trimOrNull(designationRaw));
        profile.setPhone(trimOrNull(phoneRaw));
        profile.setAlternatePhone(trimOrNull(alternatePhoneRaw));
        profile.setYearsInBusiness(yearsInBusiness);
        profile.setVenuesManaged(venuesManaged);
        profile.setCountry(trimOrNull(countryRaw));
        profile.setState(trimOrNull(stateRaw));
        profile.setCity(trimOrNull(cityRaw));
        profile.setPincode(trimOrNull(pincodeRaw));
        profile.setWebsiteUrl(trimOrNull(websiteUrlRaw));
        profile.setBio(trimOrNull(bioRaw));
        profile.setAboutUs(trimOrNull(aboutUsRaw));
        profile.setAcceptedTerms(true);
        profile.setAcceptedPrivacyPolicy(true);
        profile.setUpdatedAt(java.time.LocalDateTime.now());

        try {
            if (logoFile != null && !logoFile.isEmpty()) {
                profile.setLogoFileName(storeFile(logoFile, VendorPortalService.LOGO_DIR));
            }
            if (coverFile != null && !coverFile.isEmpty()) {
                profile.setCoverImageFileName(storeFile(coverFile, VendorPortalService.COVER_DIR));
            }
        } catch (IOException ex) {
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to store logo or cover image.");
        }

        vendorProfileRepository.save(profile);
        return baseRes;
    }

    private static String trimOrNull(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }

    public ResponseEntity<AuthResponse> login(UserRole role, LoginRequest request) {
        String email = trimOrEmpty(request.email()).toLowerCase();
        String password = request.password() == null ? "" : request.password();

        if (email.isEmpty() || password.isEmpty()) {
            return error(HttpStatus.BAD_REQUEST, "Email and password are required.");
        }

        Optional<UserAccount> found = userAccountRepository.findByEmailAndRole(email, role)
                .filter(user -> user.getPassword().equals(password));

        if (found.isEmpty()) {
            return error(HttpStatus.UNAUTHORIZED, "Invalid credentials for " + role.getDisplayName() + ".");
        }

        UserAccount user = found.get();
        if (user.isSuspended()) {
            return error(HttpStatus.FORBIDDEN,
                    "Your account has been suspended by Admin. Please contact support.");
        }
        VerificationStatus status = user.getVerificationStatus();
        if (role != UserRole.VENDOR) {
            if (status == VerificationStatus.PENDING) {
                return error(HttpStatus.FORBIDDEN,
                        "Your account and documents are pending Admin verification. You can log in once approved.");
            }
            if (status == VerificationStatus.REJECTED) {
                return error(HttpStatus.FORBIDDEN,
                        "Your verification request was rejected by Admin.");
            }
        }

        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs) {
            sessionAuthService.establishSession(attrs.getRequest(), user);
        }

        String redirectUrl = role.getDashboardUrl();
        String categoryLabel = user.getProfessionalCategory();
        if (role == UserRole.VENDOR) {
            Optional<CategoryVendorProfile> catProf = categoryVendorProfileRepository.findByUserAccount(user);
            if (catProf.isPresent()) {
                redirectUrl = "/dashboards/service-vendor.html";
                categoryLabel = catProf.get().getVendorCategory();
            }
        }

        return ResponseEntity.ok(new AuthResponse(
                true,
                "Welcome to " + role.getDisplayName() + ", " + user.getFullName() + "!",
                user.getFullName(),
                user.getEmail(),
                role.name(),
                categoryLabel,
                user.getActingStartAge(),
                user.getDateOfBirth(),
                user.getExperienceCategory(),
                user.getGender(),
                buildPhotoUrl(user.getProfilePhotoFileName()),
                redirectUrl
        ));
    }

    private ResponseEntity<AuthResponse> validateBaseCredentials(
            String fullName, String email, String password, UserRole role) {
        if (fullName.isEmpty() || email.isEmpty() || password.isEmpty()) {
            return error(HttpStatus.BAD_REQUEST, "All required fields must be filled in.");
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            return error(HttpStatus.BAD_REQUEST, "Please enter a valid email address.");
        }
        if (password.length() < 6) {
            return error(HttpStatus.BAD_REQUEST, "Password must be at least 6 characters.");
        }
        if (userAccountRepository.existsByEmailAndRole(email, role)) {
            return error(HttpStatus.CONFLICT,
                    "An account with this email already exists in " + role.getDisplayName() + ".");
        }
        return null;
    }

    private static String storeFile(MultipartFile file, Path targetDir) throws IOException {
        Files.createDirectories(targetDir);
        String original = file.getOriginalFilename() == null ? "file" : Path.of(file.getOriginalFilename()).getFileName().toString();
        String safeName = UUID.randomUUID() + "_" + original.replaceAll("[^a-zA-Z0-9._-]", "_");
        Files.copy(file.getInputStream(), targetDir.resolve(safeName), StandardCopyOption.REPLACE_EXISTING);
        return safeName;
    }

    private static String buildPhotoUrl(String fileName) {
        return fileName == null || fileName.isBlank() ? null : "/api/auth/actors/photo/" + fileName;
    }

    private static String trimOrEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    private static ResponseEntity<AuthResponse> error(HttpStatus status, String message) {
        return ResponseEntity.status(status)
                .body(new AuthResponse(false, message, null, null, null, null, null, null, null, null, null, null));
    }
}
