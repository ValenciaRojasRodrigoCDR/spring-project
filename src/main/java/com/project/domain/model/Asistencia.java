package com.project.domain.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Asistencia {
    Long id;
    Long partidoId;
    Long jugadorId;
    boolean asistio;
    int goles;
    int minutos;
    boolean titular;
}
