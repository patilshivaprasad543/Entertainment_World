package com.example.demo.controller;

import com.example.demo.dto.AuthResponse;
import com.example.demo.dto.LoginRequest;
import com.example.demo.model.UserRole;
import com.example.demo.service.RoleAuthService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@RestController
@RequestMapping("/api/auth/actors")
public class ActorAuthController {

    private final RoleAuthService roleAuthService;

    public ActorAuthController(RoleAuthService roleAuthService) {
        this.roleAuthService = roleAuthService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return roleAuthService.login(UserRole.ACTOR, request);
    }

    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AuthResponse> register(
            @RequestParam String fullName,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String professionalCategory,
            @RequestParam Integer actingStartAge,
            @RequestParam(required = false, defaultValue = "") String dateOfBirth,
            @RequestParam(required = false, defaultValue = "") String experienceCategory,
            @RequestParam(required = false, defaultValue = "") String gender,
            @RequestParam String phone,
            @RequestParam String city,
            @RequestParam(required = false, defaultValue = "") String portfolioOrBio,
            @RequestParam MultipartFile photoFile
    ) {
        return roleAuthService.registerProfessional(
                fullName, email, password, professionalCategory, actingStartAge,
                dateOfBirth, experienceCategory, gender, phone, city, portfolioOrBio, photoFile);
    }

    @GetMapping("/photo/{fileName}")
    public ResponseEntity<Resource> getPhoto(@PathVariable String fileName) throws IOException {
        Path filePath = RoleAuthService.PHOTO_DIR.resolve(fileName).normalize();
        if (!filePath.startsWith(RoleAuthService.PHOTO_DIR) || !Files.exists(filePath)) {
            return ResponseEntity.notFound().build();
        }
        String contentType = Files.probeContentType(filePath);
        MediaType mediaType = contentType != null ? MediaType.parseMediaType(contentType) : MediaType.IMAGE_JPEG;
        return ResponseEntity.ok()
                .contentType(mediaType)
                .body(new UrlResource(filePath.toUri()));
    }
}
