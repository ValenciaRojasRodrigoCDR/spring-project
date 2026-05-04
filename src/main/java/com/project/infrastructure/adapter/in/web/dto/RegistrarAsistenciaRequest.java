package com.project.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record RegistrarAsistenciaRequest(
        @NotNull List<AsistenciaItem> asistencias
) {
    public record AsistenciaItem(
            Long jugadorId,
            boolean asistio,
            int goles,
            int minutos,
            boolean titular
    ) {}
}
