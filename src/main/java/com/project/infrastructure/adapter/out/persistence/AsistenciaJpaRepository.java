package com.project.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AsistenciaJpaRepository extends JpaRepository<AsistenciaEntity, Long> {
    List<AsistenciaEntity> findByPartidoId(Long partidoId);

    @Modifying
    @Query("DELETE FROM AsistenciaEntity a WHERE a.partidoId = :partidoId")
    void deleteByPartidoId(@Param("partidoId") Long partidoId);
}
