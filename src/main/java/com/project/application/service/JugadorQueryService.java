package com.project.application.service;

import com.project.application.port.in.GetJugadoresQuery;
import com.project.application.port.out.JugadorRepository;
import com.project.domain.model.Jugador;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class JugadorQueryService implements GetJugadoresQuery {

    private final JugadorRepository jugadorRepository;

    @Override
    public List<Jugador> getByEquipoId(Long equipoId) {
        return jugadorRepository.findByEquipoId(equipoId);
    }
}
