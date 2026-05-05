package com.project.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateLigaRequest(
        @NotBlank String nombre,
        String temporada,
        String descripcion
) {}
