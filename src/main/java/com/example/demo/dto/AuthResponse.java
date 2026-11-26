package com.example.demo.dto;

public record AuthResponse(
        boolean success,
        String message,
        String fullName,
        String email,
        String role,
        String professionalCategory,
        Integer actingStartAge,
        String dateOfBirth,
        String experienceCategory,
        String gender,
        String profilePhotoUrl,
        String redirectUrl
) {
}
