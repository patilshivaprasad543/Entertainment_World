package com.example.demo.dto;

public record VerificationItemDto(
        Long id,
        String fullName,
        String email,
        String role,
        String roleDisplayName,
        String organizationName,
        String documentType,
        String documentNumber,
        String documentFileName,
        String verificationStatus,
        String phone,
        String city,
        String portfolioOrBio
) {
}
