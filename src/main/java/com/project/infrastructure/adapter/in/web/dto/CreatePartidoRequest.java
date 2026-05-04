package com.project.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreatePartidoRequest(
        @NotNull Long equipoId,
        @NotBlank String rival,
        LocalDate fecha,
        String lugar,
        String resultado,
        int golesFavor,
        int golesContra
) {}
