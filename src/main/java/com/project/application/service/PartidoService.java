package com.project.application.service;

import com.project.application.port.in.CreatePartidoUseCase;
import com.project.application.port.in.DeletePartidoUseCase;
import com.project.application.port.in.GetAsistenciasQuery;
import com.project.application.port.in.GetPartidosQuery;
import com.project.application.port.in.PageResult;
import com.project.application.port.in.RegistrarAsistenciaUseCase;
import com.project.application.port.out.AsistenciaRepository;
import com.project.application.port.out.JugadorRepository;
import com.project.application.port.out.PartidoRepository;
import com.project.domain.exception.PartidoNotFoundException;
import com.project.domain.model.Asistencia;
import com.project.domain.model.Partido;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class PartidoService implements CreatePartidoUseCase, GetPartidosQuery, DeletePartidoUseCase,
        RegistrarAsistenciaUseCase, GetAsistenciasQuery {

    private final PartidoRepository  partidoRepository;
    private final AsistenciaRepository asistenciaRepository;
    private final JugadorRepository  jugadorRepository;

    @Override
    public Partido create(CreatePartidoCommand command) {
        return partidoRepository.save(toPartido(command));
    }

    @Override
    @Transactional
    public List<Partido> createAll(List<CreatePartidoCommand> commands) {
        return partidoRepository.saveAll(commands.stream().map(this::toPartido).toList());
    }

    private Partido toPartido(CreatePartidoCommand command) {
        return Partido.builder()
                .ligaId(command.ligaId())
                .equipoId(command.equipoId())
                .rival(command.rival())
                .fecha(command.fecha())
                .lugar(command.lugar())
                .resultado(command.resultado())
                .golesFavor(command.golesFavor())
                .golesContra(command.golesContra())
                .build();
    }

    @Override
    public List<Partido> getByLigaId(Long ligaId) {
        return partidoRepository.findByLigaId(ligaId);
    }

    @Override
    public PageResult<Partido> getByLigaId(Long ligaId, int page, int size) {
        return new PageResult<>(
                partidoRepository.findByLigaId(ligaId, page, size),
                partidoRepository.countByLigaId(ligaId));
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
    @Transactional
    public void delete(Long partidoId) {
        if (!partidoRepository.existsById(partidoId)) {
            throw new PartidoNotFoundException(partidoId);
        }
        List<Long> jugadoresAfectados = asistenciaRepository.findByPartidoId(partidoId)
                .stream().map(Asistencia::getJugadorId).distinct().toList();
        asistenciaRepository.deleteByPartidoId(partidoId);
        partidoRepository.deleteById(partidoId);
        jugadorRepository.actualizarEstadisticas(jugadoresAfectados);
    }

    @Override
    @Transactional
    public List<Asistencia> registrar(Long partidoId, List<AsistenciaItem> items) {
        if (!partidoRepository.existsById(partidoId)) {
            throw new PartidoNotFoundException(partidoId);
        }
        List<Long> jugadoresAfectados = Stream.concat(
                        asistenciaRepository.findByPartidoId(partidoId).stream().map(Asistencia::getJugadorId),
                        items.stream().map(AsistenciaItem::jugadorId))
                .distinct().toList();

        asistenciaRepository.deleteByPartidoId(partidoId);

        List<Asistencia> saved = asistenciaRepository.saveAll(items.stream()
                .map(item -> Asistencia.builder()
                        .partidoId(partidoId)
                        .jugadorId(item.jugadorId())
                        .asistio(item.asistio())
                        .goles(item.goles())
                        .minutos(item.minutos())
                        .titular(item.titular())
                        .build())
                .toList());

        jugadorRepository.actualizarEstadisticas(jugadoresAfectados);
        return saved;
    }

    @Override
    public List<Asistencia> getByPartidoId(Long partidoId) {
        return asistenciaRepository.findByPartidoId(partidoId);
    }
}
