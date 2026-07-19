package com.project.infrastructure.adapter.in.web;

import com.project.application.port.in.CreateEquipoUseCase;
import com.project.application.port.in.GetEquiposQuery;
import com.project.application.port.in.GetEstadisticasAvanzadasQuery;
import com.project.application.port.in.GetJugadoresQuery;
import com.project.application.port.in.UpdateEquipoUseCase;
import com.project.infrastructure.adapter.in.web.dto.CreateEquipoRequest;
import com.project.infrastructure.adapter.in.web.dto.EquipoResponse;
import com.project.infrastructure.adapter.in.web.dto.EstadisticasAvanzadasResponse;
import com.project.infrastructure.adapter.in.web.dto.JugadorResponse;
import com.project.infrastructure.adapter.in.web.dto.PageResponse;
import com.project.infrastructure.adapter.in.web.dto.UpdateEquipoRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/equipos")
@RequiredArgsConstructor
public class EquipoController {

    private final CreateEquipoUseCase            createEquipoUseCase;
    private final UpdateEquipoUseCase            updateEquipoUseCase;
    private final GetEquiposQuery                getEquiposQuery;
    private final GetJugadoresQuery              getJugadoresQuery;
    private final GetEstadisticasAvanzadasQuery  getEstadisticasAvanzadasQuery;

    @GetMapping
    public ResponseEntity<List<EquipoResponse>> list(Authentication authentication) {
        Long userId = CurrentUser.userId(authentication);
        List<EquipoResponse> equipos = getEquiposQuery.getByUserId(userId)
                .stream().map(this::toResponse).toList();
        return ResponseEntity.ok(equipos);
    }

    @PostMapping
    public ResponseEntity<EquipoResponse> create(@Valid @RequestBody CreateEquipoRequest request,
                                                 Authentication authentication) {
        Long userId = CurrentUser.userId(authentication);
        var equipo = createEquipoUseCase.create(new CreateEquipoUseCase.CreateEquipoCommand(
                request.nombre(), request.temporada(), request.liga(), request.descripcion(), userId));
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(equipo));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EquipoResponse> update(@PathVariable Long id,
                                                 @Valid @RequestBody UpdateEquipoRequest request,
                                                 Authentication authentication) {
        Long userId = CurrentUser.userId(authentication);
        var equipo = updateEquipoUseCase.update(new UpdateEquipoUseCase.UpdateEquipoCommand(
                id, request.nombre(), request.temporada(), request.liga(), request.descripcion(), userId));
        return ResponseEntity.ok(toResponse(equipo));
    }

    @GetMapping("/{id}/jugadores")
    public ResponseEntity<?> jugadores(@PathVariable Long id,
                                       @RequestParam(required = false) Integer page,
                                       @RequestParam(defaultValue = "25") int size) {
        if (page == null) {
            return ResponseEntity.ok(getJugadoresQuery.getByEquipoId(id)
                    .stream().map(this::toJugadorResponse).toList());
        }
        var result = getJugadoresQuery.getByEquipoId(id, page, size);
        return ResponseEntity.ok(PageResponse.of(
                result.content().stream().map(this::toJugadorResponse).toList(),
                page, size, result.totalElements()));
    }

    private JugadorResponse toJugadorResponse(com.project.domain.model.Jugador j) {
        return new JugadorResponse(j.getId(), j.getNombre(), j.getPosicion(), j.getDorsal(),
                j.getEdad(), j.getTotalGoals(), j.getPartidosJugados(), j.getGolPorPartido(),
                j.getFotoUrl());
    }

    @GetMapping("/{id}/estadisticas-avanzadas")
    public ResponseEntity<EstadisticasAvanzadasResponse> estadisticasAvanzadas(@PathVariable Long id) {
        GetEstadisticasAvanzadasQuery.Result r = getEstadisticasAvanzadasQuery.get(id);
        List<EstadisticasAvanzadasResponse.PosicionStat> posiciones = r.porPosicion().stream()
                .map(p -> new EstadisticasAvanzadasResponse.PosicionStat(
                        p.posicion(), p.jugadores(), p.goles(), p.porcentajeGoles()))
                .toList();
        return ResponseEntity.ok(new EstadisticasAvanzadasResponse(
                r.promedioEdad(), r.promedioGoles(), r.promedioPartidos(), r.jugadoresSinGoles(),
                posiciones,
                toJugadorStat(r.masEficiente()),
                toJugadorStat(r.masPartidosSinMarcar()),
                toJugadorStat(r.masJoven()),
                toJugadorStat(r.masVeterano()),
                toJugadorStat(r.dorsalMasBajo()),
                toJugadorStat(r.dorsalMasAlto())));
    }

    private EstadisticasAvanzadasResponse.JugadorStat toJugadorStat(GetEstadisticasAvanzadasQuery.JugadorStat s) {
        return new EstadisticasAvanzadasResponse.JugadorStat(s.nombre(), s.valor());
    }

    private EquipoResponse toResponse(com.project.domain.model.Equipo e) {
        return new EquipoResponse(e.getId(), e.getNombre(), e.getTemporada(),
                e.getLiga(), e.getDescripcion(), e.getCreatedAt());
    }
}
