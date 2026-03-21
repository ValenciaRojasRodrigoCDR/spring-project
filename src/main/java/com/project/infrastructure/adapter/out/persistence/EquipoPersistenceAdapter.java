package com.project.infrastructure.adapter.out.persistence;

import com.project.application.port.out.EquipoRepository;
import com.project.domain.model.Equipo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class EquipoPersistenceAdapter implements EquipoRepository {

    private final EquipoJpaRepository jpaRepository;

    @Override
    public Equipo save(Equipo equipo) {
        return toDomain(jpaRepository.save(toEntity(equipo)));
    }

    @Override
    public Optional<Equipo> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Equipo> findByUserId(Long userId) {
        return jpaRepository.findByUserId(userId).stream().map(this::toDomain).toList();
    }

    private Equipo toDomain(EquipoEntity e) {
        return Equipo.builder()
                .id(e.getId())
                .nombre(e.getNombre())
                .temporada(e.getTemporada())
                .liga(e.getLiga())
                .descripcion(e.getDescripcion())
                .createdAt(e.getCreatedAt())
                .userId(e.getUserId())
                .build();
    }

    private EquipoEntity toEntity(Equipo e) {
        return EquipoEntity.builder()
                .id(e.getId())
                .nombre(e.getNombre())
                .temporada(e.getTemporada())
                .liga(e.getLiga())
                .descripcion(e.getDescripcion())
                .createdAt(e.getCreatedAt())
                .userId(e.getUserId())
                .build();
    }
}
