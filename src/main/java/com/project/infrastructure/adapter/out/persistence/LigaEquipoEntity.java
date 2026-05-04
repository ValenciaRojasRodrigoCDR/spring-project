package com.project.infrastructure.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "liga_equipo")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LigaEquipoEntity {

    @EmbeddedId
    private LigaEquipoId id;
}
