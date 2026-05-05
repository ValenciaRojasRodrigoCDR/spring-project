package com.project.infrastructure.adapter.out.persistence;

import com.project.application.port.out.LigaRepository;
import com.project.domain.model.Liga;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class LigaPersistenceAdapter implements LigaRepository {

    private final LigaJpaRepository      ligaJpaRepository;
    private final LigaEquipoJpaRepository ligaEquipoJpaRepository;

    @Override
    public Liga save(Liga liga) {
        return toDomain(ligaJpaRepository.saveAndFlush(toEntity(liga)));
    }

    @Override
    public Optional<Liga> findById(Long id) {
        return ligaJpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Liga> findByUserId(Long userId) {
        return ligaJpaRepository.findByUserId(userId).stream().map(this::toDomain).toList();
    }

    @Override
    public void deleteById(Long id) {
        ligaJpaRepository.deleteById(id);
    }

    @Override
    public void addEquipo(Long ligaId, Long equipoId) {
        LigaEquipoId id = new LigaEquipoId(ligaId, equipoId);
        if (!ligaEquipoJpaRepository.existsById(id)) {
            ligaEquipoJpaRepository.save(new LigaEquipoEntity(id));
        }
    }

    @Override
    public void removeEquipo(Long ligaId, Long equipoId) {
        ligaEquipoJpaRepository.deleteById(new LigaEquipoId(ligaId, equipoId));
    }

    @Override
    public List<Long> findEquipoIdsByLigaId(Long ligaId) {
        return ligaEquipoJpaRepository.findEquipoIdsByLigaId(ligaId);
    }

    private Liga toDomain(LigaEntity e) {
        return Liga.builder()
                .id(e.getId())
                .nombre(e.getNombre())
                .temporada(e.getTemporada())
                .descripcion(e.getDescripcion())
                .createdAt(e.getCreatedAt())
                .userId(e.getUserId())
                .build();
    }

    private LigaEntity toEntity(Liga l) {
        return LigaEntity.builder()
                .id(l.getId())
                .nombre(l.getNombre())
                .temporada(l.getTemporada())
                .descripcion(l.getDescripcion())
                .createdAt(l.getCreatedAt())
                .userId(l.getUserId())
                .build();
    }
}
