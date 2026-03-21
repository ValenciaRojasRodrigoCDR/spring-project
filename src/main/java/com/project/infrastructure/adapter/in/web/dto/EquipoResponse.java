package com.project.infrastructure.adapter.in.web.dto;

import java.time.LocalDateTime;

public record EquipoResponse(
        Long id,
        String nombre,
        String temporada,
        String liga,
        String descripcion,
        LocalDateTime createdAt
) {}
