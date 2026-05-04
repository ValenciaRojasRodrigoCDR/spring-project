package com.project.infrastructure.adapter.in.web.dto;

import java.time.LocalDateTime;
import java.util.List;

public record LigaResponse(
        Long id,
        String nombre,
        String temporada,
        String descripcion,
        LocalDateTime createdAt,
        List<Long> equipoIds
) {}
