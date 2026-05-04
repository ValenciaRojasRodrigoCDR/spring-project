package com.project.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LigaEquipoJpaRepository extends JpaRepository<LigaEquipoEntity, LigaEquipoId> {

    @Query("SELECT e.id.equipoId FROM LigaEquipoEntity e WHERE e.id.ligaId = :ligaId")
    List<Long> findEquipoIdsByLigaId(@Param("ligaId") Long ligaId);
}
