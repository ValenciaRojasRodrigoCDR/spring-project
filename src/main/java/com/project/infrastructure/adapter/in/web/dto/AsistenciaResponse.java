package com.project.infrastructure.adapter.in.web.dto;

public record AsistenciaResponse(
        Long id,
        Long partidoId,
        Long jugadorId,
        boolean asistio,
        int goles,
        int minutos,
        boolean titular
) {}
