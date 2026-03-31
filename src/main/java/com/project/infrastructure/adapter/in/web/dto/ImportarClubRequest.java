package com.project.infrastructure.adapter.in.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record ImportarClubRequest(
        @NotBlank String nombre,
        @NotBlank String temporada,
        String liga,
        String descripcion,
        @NotEmpty @Valid List<JugadorDto> jugadores
) {
    public record JugadorDto(
            @NotBlank String nombre,
            @Min(0) int totalGoals,
            @Min(0) int partidosJugados,
            double golPorPartido
    ) {}
}
