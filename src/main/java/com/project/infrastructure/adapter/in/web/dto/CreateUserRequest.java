package com.project.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateUserRequest(
        @NotBlank String username,
        @NotBlank String password,
        @NotBlank String role,
        String nombre,
        String apellidos,
        String email,
        Long jugadorId
) {}
