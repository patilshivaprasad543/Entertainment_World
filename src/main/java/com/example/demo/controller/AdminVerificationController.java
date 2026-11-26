package com.example.demo.controller;

import com.example.demo.dto.VerificationItemDto;
import com.example.demo.model.UserAccount;
import com.example.demo.model.UserRole;
import com.example.demo.model.VerificationStatus;
import com.example.demo.repository.UserAccountRepository;
import com.example.demo.service.RoleAuthService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/verifications")
public class AdminVerificationController {

    private static final List<UserRole> VERIFIABLE_ROLES =
            List.of(UserRole.VENDOR, UserRole.EVENT_MANAGEMENT, UserRole.CASTING, UserRole.BRAND);

    private final UserAccountRepository userAccountRepository;

    public AdminVerificationController(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    @GetMapping
    public List<VerificationItemDto> listVerifications() {
        return userAccountRepository.findByRoleInOrderByIdDesc(VERIFIABLE_ROLES)
                .stream()
                .map(user -> new VerificationItemDto(
                        user.getId(),
                        user.getFullName(),
                        user.getEmail(),
                        user.getRole().name(),
                        user.getRole().getDisplayName(),
                        user.getOrganizationName(),
                        user.getDocumentType(),
                        user.getDocumentNumber(),
                        user.getDocumentFileName(),
                        user.getVerificationStatus() == null
                                ? VerificationStatus.APPROVED.name()
                                : user.getVerificationStatus().name()
                ))
                .toList();
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<Map<String, String>> approve(@PathVariable Long id) {
        return updateStatus(id, VerificationStatus.APPROVED, "Account approved. User can now log in.");
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<Map<String, String>> reject(@PathVariable Long id) {
        return updateStatus(id, VerificationStatus.REJECTED, "Account verification rejected.");
    }

    @GetMapping("/{id}/document")
    public ResponseEntity<Resource> viewDocument(@PathVariable Long id) throws IOException {
        UserAccount user = userAccountRepository.findById(id).orElse(null);
        if (user == null || user.getDocumentFileName() == null) {
            return ResponseEntity.notFound().build();
        }

        Path filePath = RoleAuthService.UPLOAD_DIR.resolve(user.getDocumentFileName()).normalize();
        if (!filePath.startsWith(RoleAuthService.UPLOAD_DIR) || !Files.exists(filePath)) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new UrlResource(filePath.toUri());
        String contentType = Files.probeContentType(filePath);
        MediaType mediaType = contentType != null ? MediaType.parseMediaType(contentType) : MediaType.APPLICATION_OCTET_STREAM;

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + user.getDocumentFileName() + "\"")
                .body(resource);
    }

    private ResponseEntity<Map<String, String>> updateStatus(Long id, VerificationStatus status, String message) {
        return userAccountRepository.findById(id)
                .map(user -> {
                    user.setVerificationStatus(status);
                    userAccountRepository.save(user);
                    return ResponseEntity.ok(Map.of("message", message, "status", status.name()));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
