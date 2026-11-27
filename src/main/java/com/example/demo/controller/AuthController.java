package com.example.demo.controller;

import com.example.demo.model.UserAccount;
import com.example.demo.model.UserRole;
import com.example.demo.service.SessionAuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Lists available authentication modules and manages server-side session checks/logout.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final SessionAuthService sessionAuthService;

    public AuthController(SessionAuthService sessionAuthService) {
        this.sessionAuthService = sessionAuthService;
    }

    @GetMapping("/modules")
    public List<Map<String, String>> listModules() {
        return Arrays.stream(UserRole.values())
                .map(role -> Map.of(
                        "role", role.name(),
                        "displayName", role.getDisplayName(),
                        "dashboardUrl", role.getDashboardUrl()
                ))
                .toList();
    }

    @GetMapping("/session")
    public ResponseEntity<Map<String, Object>> currentSession(HttpServletRequest request) {
        UserAccount user = sessionAuthService.requireUser(request);
        return ResponseEntity.ok(Map.of(
                "id", user.getId(),
                "fullName", user.getFullName(),
                "email", user.getEmail(),
                "role", user.getRole().name(),
                "organizationName", user.getOrganizationName() == null ? "" : user.getOrganizationName()
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(HttpServletRequest request) {
        sessionAuthService.clearSession(request);
        return ResponseEntity.ok(Map.of("success", true, "message", "Signed out cleanly"));
    }
}
