package com.project.infrastructure.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "partidos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartidoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "liga_id")
    private Long ligaId;

    @Column(name = "equipo_id", nullable = false)
    private Long equipoId;

    @Column(nullable = false)
    private String rival;

    private LocalDate fecha;
    private String lugar;
    private String resultado;

    @Column(name = "goles_favor")
    private int golesFavor;

    @Column(name = "goles_contra")
    private int golesContra;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
