package com.project.infrastructure.adapter.in.web.dto;

import java.util.List;

public record EstadisticasAvanzadasResponse(
        double promedioEdad,
        double promedioGoles,
        double promedioPartidos,
        int jugadoresSinGoles,
        List<PosicionStat> porPosicion,
        JugadorStat masEficiente,
        JugadorStat masPartidosSinMarcar,
        JugadorStat masJoven,
        JugadorStat masVeterano,
        JugadorStat dorsalMasBajo,
        JugadorStat dorsalMasAlto
) {
    public record PosicionStat(String posicion, int jugadores, int goles, double porcentajeGoles) {}
    public record JugadorStat(String nombre, String valor) {}
}
