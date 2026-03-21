package com.project.infrastructure.adapter.out.persistence;

import com.project.application.port.out.JugadorRepository;
import com.project.domain.model.Jugador;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class JugadorPersistenceAdapter implements JugadorRepository {

    private final JugadorJpaRepository jpaRepository;

    @Override
    public Jugador save(Jugador jugador) {
        return toDomain(jpaRepository.save(toEntity(jugador)));
    }

    @Override
    public List<Jugador> findByEquipoId(Long equipoId) {
        return jpaRepository.findByEquipoId(equipoId).stream().map(this::toDomain).toList();
    }

    private Jugador toDomain(JugadorEntity e) {
        return Jugador.builder()
                .id(e.getId())
                .nombre(e.getNombre())
                .totalGoals(e.getTotalGoals())
                .partidosJugados(e.getPartidosJugados())
                .golPorPartido(e.getGolPorPartido())
                .equipoId(e.getEquipoId())
                .build();
    }

    private JugadorEntity toEntity(Jugador j) {
        return JugadorEntity.builder()
                .id(j.getId())
                .nombre(j.getNombre())
                .totalGoals(j.getTotalGoals())
                .partidosJugados(j.getPartidosJugados())
                .golPorPartido(j.getGolPorPartido())
                .equipoId(j.getEquipoId())
                .build();
    }
}
