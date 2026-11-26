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
@RequestMapping("/api/auth/events")
public class EventManagementAuthController {

    private final RoleAuthService roleAuthService;

    public EventManagementAuthController(RoleAuthService roleAuthService) {
        this.roleAuthService = roleAuthService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return roleAuthService.login(UserRole.EVENT_MANAGEMENT, request);
    }

    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AuthResponse> register(
            @RequestParam String fullName,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String organizationName,
            @RequestParam String documentType,
            @RequestParam String documentNumber,
            @RequestParam MultipartFile documentFile
    ) {
        return roleAuthService.registerWithDocuments(
                UserRole.EVENT_MANAGEMENT, fullName, email, password, organizationName, documentType, documentNumber, documentFile);
    }
}
