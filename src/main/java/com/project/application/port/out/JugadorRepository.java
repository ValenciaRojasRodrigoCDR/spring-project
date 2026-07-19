package com.project.application.port.out;

import com.project.domain.model.Jugador;

import java.util.List;
import java.util.Optional;

public interface JugadorRepository {
    Jugador save(Jugador jugador);
    List<Jugador> saveAll(List<Jugador> jugadores);
    List<Jugador> findByEquipoId(Long equipoId);
    List<Jugador> findByEquipoId(Long equipoId, int page, int size);
    long countByEquipoId(Long equipoId);
    Optional<Jugador> findById(Long id);
    void actualizarEstadisticas(List<Long> jugadorIds);
}
