package com.project.domain.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Jugador {
    Long id;
    String nombre;
    String posicion;
    Integer dorsal;
    Integer edad;
    int totalGoals;
    int partidosJugados;
    double golPorPartido;
    String fotoUrl;
    boolean fotoConvertida;
    Long equipoId;
}
