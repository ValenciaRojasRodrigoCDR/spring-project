package com.project.infrastructure.adapter.in.web.dto;

import java.util.List;

public record ImportarClubRequest(
        String nombre,
        String temporada,
        String liga,
        String descripcion,
        List<JugadorDto> jugadores
) {
    public record JugadorDto(String nombre, int totalGoals, int partidosJugados, double golPorPartido) {}
}
