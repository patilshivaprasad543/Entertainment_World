package com.example.demo.controller;

import com.example.demo.dto.AuthResponse;
import com.example.demo.dto.LoginRequest;
import com.example.demo.model.UserRole;
import com.example.demo.service.RoleAuthService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/auth/vendors")
public class VendorAuthController {

    private final RoleAuthService roleAuthService;

    public VendorAuthController(RoleAuthService roleAuthService) {
        this.roleAuthService = roleAuthService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return roleAuthService.login(UserRole.VENDOR, request);
    }

    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AuthResponse> register(
            @RequestParam String fullName,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String organizationName,
            @RequestParam(required = false) String designation,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String alternatePhone,
            @RequestParam(required = false) Integer yearsInBusiness,
            @RequestParam(required = false) Integer venuesManaged,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String pincode,
            @RequestParam(required = false) String websiteUrl,
            @RequestParam(required = false) String bio,
            @RequestParam(required = false) String aboutUs,
            @RequestParam String documentType,
            @RequestParam String documentNumber,
            @RequestParam MultipartFile documentFile,
            @RequestParam(required = false) MultipartFile logoFile,
            @RequestParam(required = false) MultipartFile coverFile,
            @RequestParam(defaultValue = "false") boolean acceptedTerms,
            @RequestParam(defaultValue = "false") boolean acceptedPrivacyPolicy
    ) {
        return roleAuthService.registerVendorWithProfile(
                fullName, email, password, organizationName,
                designation, phone, alternatePhone, yearsInBusiness, venuesManaged,
                country, state, city, pincode, websiteUrl, bio, aboutUs,
                documentType, documentNumber, documentFile, logoFile, coverFile,
                acceptedTerms, acceptedPrivacyPolicy
        );
    }
}
