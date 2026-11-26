package com.example.demo.controller;

import com.example.demo.model.UserRole;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Lists available authentication modules and their dashboard routes.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

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
}
