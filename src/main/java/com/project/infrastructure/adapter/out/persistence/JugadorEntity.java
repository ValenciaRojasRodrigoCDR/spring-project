package com.project.infrastructure.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "jugadores")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JugadorEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(name = "total_goals")
    private int totalGoals;

    @Column(name = "partidos_jugados")
    private int partidosJugados;

    @Column(name = "gol_por_partido")
    private double golPorPartido;

    @Column(name = "equipo_id", nullable = false)
    private Long equipoId;
}
