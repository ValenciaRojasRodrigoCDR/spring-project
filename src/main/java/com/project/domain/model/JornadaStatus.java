package com.project.domain.model;

public enum JornadaStatus {
    GOALS,          // jugó y marcó N goles (puede ser 0)
    ABSENT,         // no jugó (X)
    PENDING,        // jornada pendiente / celda vacía
    NO_PRESENTADO   // rival no se presentó
}
