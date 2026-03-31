package com.project.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateEquipoRequest(
        @NotBlank String nombre,
        String temporada,
        String liga,
        String descripcion
) {}
