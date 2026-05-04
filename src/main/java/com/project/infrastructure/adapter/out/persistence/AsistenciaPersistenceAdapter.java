package com.project.infrastructure.adapter.out.persistence;

import com.project.application.port.out.AsistenciaRepository;
import com.project.domain.model.Asistencia;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AsistenciaPersistenceAdapter implements AsistenciaRepository {

    private final AsistenciaJpaRepository jpaRepository;

    @Override
    public Asistencia save(Asistencia asistencia) {
        return toDomain(jpaRepository.saveAndFlush(toEntity(asistencia)));
    }

    @Override
    public List<Asistencia> findByPartidoId(Long partidoId) {
        return jpaRepository.findByPartidoId(partidoId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<Asistencia> findByJugadorId(Long jugadorId) {
        return jpaRepository.findByJugadorId(jugadorId).stream().map(this::toDomain).toList();
    }

    @Override
    @Transactional
    public void deleteByPartidoId(Long partidoId) {
        jpaRepository.deleteByPartidoId(partidoId);
    }

    private Asistencia toDomain(AsistenciaEntity e) {
        return Asistencia.builder()
                .id(e.getId())
                .partidoId(e.getPartidoId())
                .jugadorId(e.getJugadorId())
                .asistio(e.isAsistio())
                .goles(e.getGoles())
                .minutos(e.getMinutos())
                .titular(e.isTitular())
                .build();
    }

    private AsistenciaEntity toEntity(Asistencia a) {
        return AsistenciaEntity.builder()
                .id(a.getId())
                .partidoId(a.getPartidoId())
                .jugadorId(a.getJugadorId())
                .asistio(a.isAsistio())
                .goles(a.getGoles())
                .minutos(a.getMinutos())
                .titular(a.isTitular())
                .build();
    }
}
