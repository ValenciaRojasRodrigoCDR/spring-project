package com.project.application.service;

import com.project.application.port.in.GetJugadoresQuery;
import com.project.application.port.in.PageResult;
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

    @Override
    public PageResult<Jugador> getByEquipoId(Long equipoId, int page, int size) {
        return new PageResult<>(
                jugadorRepository.findByEquipoId(equipoId, page, size),
                jugadorRepository.countByEquipoId(equipoId));
    }
}
