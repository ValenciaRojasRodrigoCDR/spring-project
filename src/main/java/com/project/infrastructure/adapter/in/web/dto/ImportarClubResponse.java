package com.project.infrastructure.adapter.in.web.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ImportarClubResponse(
        Long equipoId,
        String nombre,
        String temporada,
        String liga,
        String descripcion,
        LocalDateTime createdAt,
        List<JugadorDto> jugadores
) {
    public record JugadorDto(Long id, String nombre, int totalGoals, int partidosJugados, double golPorPartido) {}
}
