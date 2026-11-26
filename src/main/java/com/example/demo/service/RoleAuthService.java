package com.example.demo.service;

import com.example.demo.dto.AuthResponse;
import com.example.demo.dto.LoginRequest;
import com.example.demo.model.UserAccount;
import com.example.demo.model.UserRole;
import com.example.demo.model.VerificationStatus;
import com.example.demo.repository.UserAccountRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
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

    public RoleAuthService(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
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
        account.setDateOfBirth(dateOfBirth);
        account.setExperienceCategory(experienceCategory);
        account.setGender(gender);
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
        account.setVerificationStatus(VerificationStatus.PENDING);

        UserAccount saved = userAccountRepository.save(account);
        return ResponseEntity.status(HttpStatus.CREATED).body(new AuthResponse(
                true,
                "Registration submitted! Your documents are pending Admin verification before you can log in.",
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
        VerificationStatus status = user.getVerificationStatus();
        if (status == VerificationStatus.PENDING) {
            return error(HttpStatus.FORBIDDEN,
                    "Your account and documents are pending Admin verification. You can log in once approved.");
        }
        if (status == VerificationStatus.REJECTED) {
            return error(HttpStatus.FORBIDDEN,
                    "Your verification request was rejected by Admin.");
        }

        return ResponseEntity.ok(new AuthResponse(
                true,
                "Welcome to " + role.getDisplayName() + ", " + user.getFullName() + "!",
                user.getFullName(),
                user.getEmail(),
                role.name(),
                user.getProfessionalCategory(),
                user.getActingStartAge(),
                user.getDateOfBirth(),
                user.getExperienceCategory(),
                user.getGender(),
                buildPhotoUrl(user.getProfilePhotoFileName()),
                role.getDashboardUrl()
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
