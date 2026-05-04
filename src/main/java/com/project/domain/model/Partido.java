package com.project.domain.model;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Value
@Builder
public class Partido {
    Long id;
    Long ligaId;
    Long equipoId;
    String rival;
    LocalDate fecha;
    String lugar;
    String resultado;
    int golesFavor;
    int golesContra;
    LocalDateTime createdAt;
}
