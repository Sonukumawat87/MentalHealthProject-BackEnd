package com.mentalsupport.backend.auth.dto;

public record AuthResponse(
        String token,
        String role,
        String name,
        String email
) {}