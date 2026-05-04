package com.project.domain.model;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class Liga {
    Long id;
    String nombre;
    String temporada;
    String descripcion;
    LocalDateTime createdAt;
    Long userId;
}
