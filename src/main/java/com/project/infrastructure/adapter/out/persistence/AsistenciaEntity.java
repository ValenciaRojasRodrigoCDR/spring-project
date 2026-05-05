package com.project.infrastructure.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "asistencias")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AsistenciaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "partido_id", nullable = false)
    private Long partidoId;

    @Column(name = "jugador_id", nullable = false)
    private Long jugadorId;

    private boolean asistio;
    private int goles;
    private int minutos;
    private boolean titular;
}
