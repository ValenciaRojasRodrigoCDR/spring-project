package com.project.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateEquipoRequest(
        @NotBlank String nombre,
        @NotBlank String temporada,
        String liga,
        String descripcion
) {}
