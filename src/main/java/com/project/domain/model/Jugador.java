package com.project.domain.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Jugador {
    Long id;
    String nombre;
    int totalGoals;
    int partidosJugados;
    double golPorPartido;
    Long equipoId;
}
