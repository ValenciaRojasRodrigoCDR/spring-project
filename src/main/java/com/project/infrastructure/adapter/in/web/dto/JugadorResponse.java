package com.project.infrastructure.adapter.in.web.dto;

public record JugadorResponse(
        Long id,
        String nombre,
        String posicion,
        Integer dorsal,
        Integer edad,
        int totalGoals,
        int partidosJugados,
        double golPorPartido,
        String fotoUrl
) {}
