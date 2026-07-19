package com.project.infrastructure.adapter.in.web;

import com.project.application.port.in.AddEquipoToLigaUseCase;
import com.project.application.port.in.CreateLigaUseCase;
import com.project.application.port.in.DeleteLigaUseCase;
import com.project.application.port.in.GetLigasQuery;
import com.project.application.port.in.UpdateLigaUseCase;
import com.project.domain.model.Liga;
import com.project.infrastructure.adapter.in.web.dto.CreateLigaRequest;
import com.project.infrastructure.adapter.in.web.dto.LigaResponse;
import com.project.infrastructure.adapter.in.web.dto.UpdateLigaRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ligas")
@RequiredArgsConstructor
public class LigaController {

    private final CreateLigaUseCase     createLigaUseCase;
    private final UpdateLigaUseCase     updateLigaUseCase;
    private final DeleteLigaUseCase     deleteLigaUseCase;
    private final GetLigasQuery         getLigasQuery;
    private final AddEquipoToLigaUseCase addEquipoToLigaUseCase;

    @GetMapping
    public ResponseEntity<List<LigaResponse>> list(Authentication authentication) {
        Long userId = resolveUserId(authentication);
        List<Liga> ligas = getLigasQuery.getByUserId(userId);
        Map<Long, List<Long>> equipoIdsPorLiga = addEquipoToLigaUseCase.getEquipoIdsByLigaIds(
                ligas.stream().map(Liga::getId).toList());
        return ResponseEntity.ok(ligas.stream()
                .map(l -> toResponse(l, equipoIdsPorLiga.getOrDefault(l.getId(), List.of())))
                .toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<LigaResponse> get(@PathVariable Long id) {
        Liga liga = getLigasQuery.getById(id);
        return ResponseEntity.ok(toResponse(liga, addEquipoToLigaUseCase.getEquipoIds(id)));
    }

    @PostMapping
    public ResponseEntity<LigaResponse> create(@Valid @RequestBody CreateLigaRequest request,
                                               Authentication authentication) {
        Long userId = resolveUserId(authentication);
        Liga liga = createLigaUseCase.create(new CreateLigaUseCase.CreateLigaCommand(
                request.nombre(), request.temporada(), request.descripcion(), userId));
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(liga, List.of()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LigaResponse> update(@PathVariable Long id,
                                               @Valid @RequestBody UpdateLigaRequest request,
                                               Authentication authentication) {
        Long userId = resolveUserId(authentication);
        Liga liga = updateLigaUseCase.update(new UpdateLigaUseCase.UpdateLigaCommand(
                id, request.nombre(), request.temporada(), request.descripcion(), userId));
        return ResponseEntity.ok(toResponse(liga, addEquipoToLigaUseCase.getEquipoIds(id)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication authentication) {
        Long userId = resolveUserId(authentication);
        deleteLigaUseCase.delete(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/equipos/{equipoId}")
    public ResponseEntity<Void> addEquipo(@PathVariable Long id,
                                          @PathVariable Long equipoId,
                                          Authentication authentication) {
        Long userId = resolveUserId(authentication);
        addEquipoToLigaUseCase.addEquipo(id, equipoId, userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/equipos/{equipoId}")
    public ResponseEntity<Void> removeEquipo(@PathVariable Long id,
                                             @PathVariable Long equipoId,
                                             Authentication authentication) {
        Long userId = resolveUserId(authentication);
        addEquipoToLigaUseCase.removeEquipo(id, equipoId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/equipos")
    public ResponseEntity<List<Long>> equipos(@PathVariable Long id) {
        return ResponseEntity.ok(addEquipoToLigaUseCase.getEquipoIds(id));
    }

    private Long resolveUserId(Authentication authentication) {
        return CurrentUser.userId(authentication);
    }

    private LigaResponse toResponse(Liga l, List<Long> equipoIds) {
        return new LigaResponse(l.getId(), l.getNombre(), l.getTemporada(),
                l.getDescripcion(), l.getCreatedAt(), equipoIds);
    }
}
