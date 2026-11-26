package com.example.demo.dto;

public record RegisterRequest(
        String fullName,
        String email,
        String password,
        String professionalCategory,
        String phone,
        String city,
        String portfolioOrBio
) {
}
