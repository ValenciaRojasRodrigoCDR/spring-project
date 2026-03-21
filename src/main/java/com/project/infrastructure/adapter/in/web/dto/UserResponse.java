package com.project.infrastructure.adapter.in.web.dto;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String username,
        String role,
        String nombre,
        String apellidos,
        String email,
        LocalDateTime createdAt
) {}
