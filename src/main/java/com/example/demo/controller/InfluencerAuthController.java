package com.example.demo.controller;

import com.example.demo.dto.AuthResponse;
import com.example.demo.dto.LoginRequest;
import com.example.demo.model.UserRole;
import com.example.demo.service.RoleAuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth/influencer")
public class InfluencerAuthController {

    private final RoleAuthService roleAuthService;

    public InfluencerAuthController(RoleAuthService roleAuthService) {
        this.roleAuthService = roleAuthService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return roleAuthService.login(UserRole.INFLUENCER, request);
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody Map<String, String> body) {
        return roleAuthService.registerInfluencer(
                body.get("fullName"),
                body.get("email"),
                body.get("password"),
                body.get("phone"),
                body.get("city"),
                body.get("portfolioOrBio")
        );
    }
}
