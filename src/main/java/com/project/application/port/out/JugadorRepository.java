package com.project.application.port.out;

import com.project.domain.model.Jugador;

import java.util.List;

public interface JugadorRepository {
    Jugador save(Jugador jugador);
    List<Jugador> findByEquipoId(Long equipoId);
}
