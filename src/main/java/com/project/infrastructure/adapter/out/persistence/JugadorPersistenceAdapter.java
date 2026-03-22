package com.project.infrastructure.adapter.out.persistence;

import com.project.application.port.out.JugadorRepository;
import com.project.domain.model.Jugador;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

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

    @Override
    public Optional<Jugador> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    private Jugador toDomain(JugadorEntity e) {
        return Jugador.builder()
                .id(e.getId())
                .nombre(e.getNombre())
                .posicion(e.getPosicion())
                .dorsal(e.getDorsal())
                .edad(e.getEdad())
                .totalGoals(e.getTotalGoals())
                .partidosJugados(e.getPartidosJugados())
                .golPorPartido(e.getGolPorPartido())
                .fotoUrl(e.getFotoUrl())
                .equipoId(e.getEquipoId())
                .build();
    }

    private JugadorEntity toEntity(Jugador j) {
        return JugadorEntity.builder()
                .id(j.getId())
                .nombre(j.getNombre())
                .posicion(j.getPosicion())
                .dorsal(j.getDorsal())
                .edad(j.getEdad())
                .totalGoals(j.getTotalGoals())
                .partidosJugados(j.getPartidosJugados())
                .golPorPartido(j.getGolPorPartido())
                .fotoUrl(j.getFotoUrl())
                .equipoId(j.getEquipoId())
                .build();
    }
}
