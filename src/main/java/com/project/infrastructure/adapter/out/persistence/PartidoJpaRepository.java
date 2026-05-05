package com.project.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PartidoJpaRepository extends JpaRepository<PartidoEntity, Long> {
    List<PartidoEntity> findByLigaId(Long ligaId);
    List<PartidoEntity> findByEquipoId(Long equipoId);
}
