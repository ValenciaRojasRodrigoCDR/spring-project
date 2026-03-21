package com.project.domain.model;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class PlayerStats {

    private String name;
    private Map<String, JornadaEntry> jornadas;
    private int totalGoals;
    private int partidosJugados;
    private double golPorPartido;
}
