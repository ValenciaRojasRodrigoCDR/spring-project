package com.project.infrastructure.adapter.in.web.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record PartidoResponse(
        Long id,
        Long ligaId,
        Long equipoId,
        String rival,
        LocalDate fecha,
        String lugar,
        String resultado,
        int golesFavor,
        int golesContra,
        LocalDateTime createdAt
) {}
