package com.project.infrastructure.adapter.in.web.dto;

public record JugadorResponse(
        Long id,
        String nombre,
        int totalGoals,
        int partidosJugados,
        double golPorPartido
) {}
