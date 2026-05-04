package com.project.application.service;

import com.project.application.port.in.CreatePartidoUseCase;
import com.project.application.port.in.DeletePartidoUseCase;
import com.project.application.port.in.GetAsistenciasQuery;
import com.project.application.port.in.GetPartidosQuery;
import com.project.application.port.in.RegistrarAsistenciaUseCase;
import com.project.application.port.out.AsistenciaRepository;
import com.project.application.port.out.JugadorRepository;
import com.project.application.port.out.PartidoRepository;
import com.project.domain.exception.PartidoNotFoundException;
import com.project.domain.model.Asistencia;
import com.project.domain.model.Jugador;
import com.project.domain.model.Partido;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PartidoService implements CreatePartidoUseCase, GetPartidosQuery, DeletePartidoUseCase,
        RegistrarAsistenciaUseCase, GetAsistenciasQuery {

    private final PartidoRepository  partidoRepository;
    private final AsistenciaRepository asistenciaRepository;
    private final JugadorRepository  jugadorRepository;

    @Override
    public Partido create(CreatePartidoCommand command) {
        return partidoRepository.save(Partido.builder()
                .ligaId(command.ligaId())
                .equipoId(command.equipoId())
                .rival(command.rival())
                .fecha(command.fecha())
                .lugar(command.lugar())
                .resultado(command.resultado())
                .golesFavor(command.golesFavor())
                .golesContra(command.golesContra())
                .build());
    }

    @Override
    public List<Partido> getByLigaId(Long ligaId) {
        return partidoRepository.findByLigaId(ligaId);
    }

    @Override
    public List<Partido> getByEquipoId(Long equipoId) {
        return partidoRepository.findByEquipoId(equipoId);
    }

    @Override
    public Partido getById(Long id) {
        return partidoRepository.findById(id)
                .orElseThrow(() -> new PartidoNotFoundException(id));
    }

    @Override
    public void delete(Long partidoId) {
        if (partidoRepository.findById(partidoId).isEmpty()) {
            throw new PartidoNotFoundException(partidoId);
        }
        asistenciaRepository.deleteByPartidoId(partidoId);
        partidoRepository.deleteById(partidoId);
    }

    @Override
    public List<Asistencia> registrar(Long partidoId, List<AsistenciaItem> items) {
        if (partidoRepository.findById(partidoId).isEmpty()) {
            throw new PartidoNotFoundException(partidoId);
        }
        asistenciaRepository.deleteByPartidoId(partidoId);

        List<Asistencia> saved = items.stream()
                .map(item -> asistenciaRepository.save(Asistencia.builder()
                        .partidoId(partidoId)
                        .jugadorId(item.jugadorId())
                        .asistio(item.asistio())
                        .goles(item.goles())
                        .minutos(item.minutos())
                        .titular(item.titular())
                        .build()))
                .toList();

        recalcularEstadisticasJugadores(items.stream().map(AsistenciaItem::jugadorId).distinct().toList());
        return saved;
    }

    @Override
    public List<Asistencia> getByPartidoId(Long partidoId) {
        return asistenciaRepository.findByPartidoId(partidoId);
    }

    private void recalcularEstadisticasJugadores(List<Long> jugadorIds) {
        for (Long jugadorId : jugadorIds) {
            jugadorRepository.findById(jugadorId).ifPresent(jugador -> {
                List<Asistencia> todas = asistenciaRepository.findByJugadorId(jugadorId);
                int totalGoals      = todas.stream().mapToInt(Asistencia::getGoles).sum();
                int partidosJugados = (int) todas.stream().filter(Asistencia::isAsistio).count();
                double golPorPartido = partidosJugados > 0 ? (double) totalGoals / partidosJugados : 0.0;

                jugadorRepository.save(Jugador.builder()
                        .id(jugador.getId())
                        .nombre(jugador.getNombre())
                        .posicion(jugador.getPosicion())
                        .dorsal(jugador.getDorsal())
                        .edad(jugador.getEdad())
                        .fotoUrl(jugador.getFotoUrl())
                        .fotoConvertida(jugador.isFotoConvertida())
                        .equipoId(jugador.getEquipoId())
                        .totalGoals(totalGoals)
                        .partidosJugados(partidosJugados)
                        .golPorPartido(golPorPartido)
                        .build());
            });
        }
    }
}
