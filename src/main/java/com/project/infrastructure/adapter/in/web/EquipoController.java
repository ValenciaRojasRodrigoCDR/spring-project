package com.project.infrastructure.adapter.in.web;

import com.project.application.port.in.CreateEquipoUseCase;
import com.project.application.port.in.GetEquiposQuery;
import com.project.application.port.in.GetJugadoresQuery;
import com.project.application.port.in.GetUserQuery;
import com.project.application.port.in.UpdateEquipoUseCase;
import com.project.infrastructure.adapter.in.web.dto.CreateEquipoRequest;
import com.project.infrastructure.adapter.in.web.dto.EquipoResponse;
import com.project.infrastructure.adapter.in.web.dto.JugadorResponse;
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

    private final CreateEquipoUseCase createEquipoUseCase;
    private final UpdateEquipoUseCase updateEquipoUseCase;
    private final GetEquiposQuery     getEquiposQuery;
    private final GetUserQuery        getUserQuery;
    private final GetJugadoresQuery   getJugadoresQuery;

    @GetMapping
    public ResponseEntity<List<EquipoResponse>> list(Authentication authentication) {
        Long userId = getUserQuery.getByUsername(authentication.getName()).getId();
        List<EquipoResponse> equipos = getEquiposQuery.getByUserId(userId)
                .stream().map(this::toResponse).toList();
        return ResponseEntity.ok(equipos);
    }

    @PostMapping
    public ResponseEntity<EquipoResponse> create(@Valid @RequestBody CreateEquipoRequest request,
                                                 Authentication authentication) {
        Long userId = getUserQuery.getByUsername(authentication.getName()).getId();
        var equipo = createEquipoUseCase.create(new CreateEquipoUseCase.CreateEquipoCommand(
                request.nombre(), request.temporada(), request.liga(), request.descripcion(), userId));
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(equipo));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EquipoResponse> update(@PathVariable Long id,
                                                 @Valid @RequestBody UpdateEquipoRequest request,
                                                 Authentication authentication) {
        Long userId = getUserQuery.getByUsername(authentication.getName()).getId();
        var equipo = updateEquipoUseCase.update(new UpdateEquipoUseCase.UpdateEquipoCommand(
                id, request.nombre(), request.temporada(), request.liga(), request.descripcion(), userId));
        return ResponseEntity.ok(toResponse(equipo));
    }

    @GetMapping("/{id}/jugadores")
    public ResponseEntity<List<JugadorResponse>> jugadores(@PathVariable Long id) {
        List<JugadorResponse> jugadores = getJugadoresQuery.getByEquipoId(id)
                .stream().map(j -> new JugadorResponse(
                        j.getId(), j.getNombre(), j.getPosicion(), j.getDorsal(), j.getEdad(),
                        j.getTotalGoals(), j.getPartidosJugados(), j.getGolPorPartido(), j.getFotoUrl()))
                .toList();
        return ResponseEntity.ok(jugadores);
    }

    private EquipoResponse toResponse(com.project.domain.model.Equipo e) {
        return new EquipoResponse(e.getId(), e.getNombre(), e.getTemporada(),
                e.getLiga(), e.getDescripcion(), e.getCreatedAt());
    }
}
