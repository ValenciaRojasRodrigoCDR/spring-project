package com.project.application.port.out;

import com.project.domain.model.Jugador;

import java.util.List;
import java.util.Optional;

public interface JugadorRepository {
    Jugador save(Jugador jugador);
    List<Jugador> findByEquipoId(Long equipoId);
    Optional<Jugador> findById(Long id);
}
