package com.example.demo.controller;

import com.example.demo.dto.AuthResponse;
import com.example.demo.dto.LoginRequest;
import com.example.demo.model.UserRole;
import com.example.demo.service.RoleAuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin authentication controller. Admin uses predefined credentials only.
 */
@RestController
@RequestMapping("/api/auth/admin")
public class AdminAuthController {

    private final RoleAuthService roleAuthService;

    public AdminAuthController(RoleAuthService roleAuthService) {
        this.roleAuthService = roleAuthService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return roleAuthService.login(UserRole.ADMIN, request);
    }
}
