package com.project.domain.model;

public record JornadaEntry(JornadaStatus status, int goals) {

    /** Devuelve true si el jugador participó en este partido. */
    public boolean played() {
        return status == JornadaStatus.GOALS;
    }
}
