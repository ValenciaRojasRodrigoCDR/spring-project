package com.project.infrastructure.adapter.in.web;

import com.project.application.port.in.CreatePartidoUseCase;
import com.project.application.port.in.DeletePartidoUseCase;
import com.project.application.port.in.GetAsistenciasQuery;
import com.project.application.port.in.GetPartidosQuery;
import com.project.application.port.in.RegistrarAsistenciaUseCase;
import com.project.domain.model.Asistencia;
import com.project.domain.model.Partido;
import com.project.infrastructure.adapter.in.web.dto.AsistenciaResponse;
import com.project.infrastructure.adapter.in.web.dto.CreatePartidoRequest;
import com.project.infrastructure.adapter.in.web.dto.ImportarPartidosRequest;
import com.project.infrastructure.adapter.in.web.dto.PageResponse;
import com.project.infrastructure.adapter.in.web.dto.PartidoResponse;
import com.project.infrastructure.adapter.in.web.dto.RegistrarAsistenciaRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ligas/{ligaId}/partidos")
@RequiredArgsConstructor
public class PartidoController {

    private final CreatePartidoUseCase    createPartidoUseCase;
    private final GetPartidosQuery        getPartidosQuery;
    private final DeletePartidoUseCase    deletePartidoUseCase;
    private final RegistrarAsistenciaUseCase registrarAsistenciaUseCase;
    private final GetAsistenciasQuery     getAsistenciasQuery;

    @GetMapping
    public ResponseEntity<?> list(@PathVariable Long ligaId,
                                  @RequestParam(required = false) Integer page,
                                  @RequestParam(defaultValue = "25") int size) {
        if (page == null) {
            return ResponseEntity.ok(getPartidosQuery.getByLigaId(ligaId)
                    .stream().map(this::toResponse).toList());
        }
        var result = getPartidosQuery.getByLigaId(ligaId, page, size);
        return ResponseEntity.ok(PageResponse.of(
                result.content().stream().map(this::toResponse).toList(),
                page, size, result.totalElements()));
    }

    @GetMapping("/{partidoId}")
    public ResponseEntity<PartidoResponse> get(@PathVariable Long ligaId,
                                               @PathVariable Long partidoId) {
        return ResponseEntity.ok(toResponse(getPartidosQuery.getById(partidoId)));
    }

    @PostMapping
    public ResponseEntity<PartidoResponse> create(@PathVariable Long ligaId,
                                                  @Valid @RequestBody CreatePartidoRequest request) {
        Partido partido = createPartidoUseCase.create(toCommand(ligaId, request));
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(partido));
    }

    @PostMapping("/importar")
    public ResponseEntity<Map<String, Integer>> importar(@PathVariable Long ligaId,
                                                         @Valid @RequestBody ImportarPartidosRequest request) {
        List<Partido> creados = createPartidoUseCase.createAll(request.partidos().stream()
                .map(p -> toCommand(ligaId, p)).toList());
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("importados", creados.size()));
    }

    private CreatePartidoUseCase.CreatePartidoCommand toCommand(Long ligaId, CreatePartidoRequest request) {
        return new CreatePartidoUseCase.CreatePartidoCommand(
                ligaId,
                request.equipoId(),
                request.rival(),
                request.fecha(),
                request.lugar(),
                request.resultado(),
                request.golesFavor(),
                request.golesContra());
    }

    @DeleteMapping("/{partidoId}")
    public ResponseEntity<Void> delete(@PathVariable Long ligaId, @PathVariable Long partidoId) {
        deletePartidoUseCase.delete(partidoId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{partidoId}/asistencias")
    public ResponseEntity<List<AsistenciaResponse>> registrarAsistencias(
            @PathVariable Long ligaId,
            @PathVariable Long partidoId,
            @Valid @RequestBody RegistrarAsistenciaRequest request) {
        List<RegistrarAsistenciaUseCase.AsistenciaItem> items = request.asistencias().stream()
                .map(a -> new RegistrarAsistenciaUseCase.AsistenciaItem(
                        a.jugadorId(), a.asistio(), a.goles(), a.minutos(), a.titular()))
                .toList();
        List<AsistenciaResponse> responses = registrarAsistenciaUseCase.registrar(partidoId, items)
                .stream().map(this::toAsistenciaResponse).toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{partidoId}/asistencias")
    public ResponseEntity<List<AsistenciaResponse>> getAsistencias(@PathVariable Long ligaId,
                                                                    @PathVariable Long partidoId) {
        return ResponseEntity.ok(getAsistenciasQuery.getByPartidoId(partidoId)
                .stream().map(this::toAsistenciaResponse).toList());
    }

    private PartidoResponse toResponse(Partido p) {
        return new PartidoResponse(p.getId(), p.getLigaId(), p.getEquipoId(), p.getRival(),
                p.getFecha(), p.getLugar(), p.getResultado(), p.getGolesFavor(),
                p.getGolesContra(), p.getCreatedAt());
    }

    private AsistenciaResponse toAsistenciaResponse(Asistencia a) {
        return new AsistenciaResponse(a.getId(), a.getPartidoId(), a.getJugadorId(),
                a.isAsistio(), a.getGoles(), a.getMinutos(), a.isTitular());
    }
}
