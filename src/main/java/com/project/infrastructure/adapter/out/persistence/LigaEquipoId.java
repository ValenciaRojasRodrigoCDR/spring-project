package com.project.infrastructure.adapter.out.persistence;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LigaEquipoId implements Serializable {
    private Long ligaId;
    private Long equipoId;
}
