package com.project.application.port.in;

import java.util.List;

public interface GetEstadisticasAvanzadasQuery {

    Result get(Long equipoId);

    record Result(
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
    ) {}

    record PosicionStat(String posicion, int jugadores, int goles, double porcentajeGoles) {}

    record JugadorStat(String nombre, String valor) {}
}
