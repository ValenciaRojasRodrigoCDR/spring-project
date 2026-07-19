package com.project.infrastructure.adapter.out.persistence;

import com.project.application.port.out.PartidoRepository;
import com.project.domain.model.Partido;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PartidoPersistenceAdapter implements PartidoRepository {

    private final PartidoJpaRepository jpaRepository;

    @Override
    public Partido save(Partido partido) {
        return toDomain(jpaRepository.save(toEntity(partido)));
    }

    @Override
    public List<Partido> saveAll(List<Partido> partidos) {
        return jpaRepository.saveAll(partidos.stream().map(this::toEntity).toList())
                .stream().map(this::toDomain).toList();
    }

    @Override
    public Optional<Partido> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public boolean existsById(Long id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public List<Partido> findByLigaId(Long ligaId) {
        return jpaRepository.findByLigaId(ligaId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<Partido> findByLigaId(Long ligaId, int page, int size) {
        return jpaRepository.findByLigaId(ligaId, PageRequest.of(page, size, Sort.by("id")))
                .stream().map(this::toDomain).toList();
    }

    @Override
    public long countByLigaId(Long ligaId) {
        return jpaRepository.countByLigaId(ligaId);
    }

    @Override
    public List<Partido> findByEquipoId(Long equipoId) {
        return jpaRepository.findByEquipoId(equipoId).stream().map(this::toDomain).toList();
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    private Partido toDomain(PartidoEntity e) {
        return Partido.builder()
                .id(e.getId())
                .ligaId(e.getLigaId())
                .equipoId(e.getEquipoId())
                .rival(e.getRival())
                .fecha(e.getFecha())
                .lugar(e.getLugar())
                .resultado(e.getResultado())
                .golesFavor(e.getGolesFavor())
                .golesContra(e.getGolesContra())
                .createdAt(e.getCreatedAt())
                .build();
    }

    private PartidoEntity toEntity(Partido p) {
        return PartidoEntity.builder()
                .id(p.getId())
                .ligaId(p.getLigaId())
                .equipoId(p.getEquipoId())
                .rival(p.getRival())
                .fecha(p.getFecha())
                .lugar(p.getLugar())
                .resultado(p.getResultado())
                .golesFavor(p.getGolesFavor())
                .golesContra(p.getGolesContra())
                .createdAt(p.getCreatedAt())
                .build();
    }
}
