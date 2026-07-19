package com.project.infrastructure.adapter.in.web;

import com.project.application.port.in.ImportarClubUseCase;
import com.project.application.service.AsyncImportarClubService;
import com.project.infrastructure.adapter.in.web.dto.ImportarClubRequest;
import com.project.infrastructure.adapter.in.web.dto.ImportarClubResponse;
import com.project.infrastructure.adapter.in.web.dto.ImportJobResponse;
import com.project.infrastructure.store.ImportJobStore;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/equipos")
@RequiredArgsConstructor
public class ImportarClubController {

    private final AsyncImportarClubService asyncImportarClubService;
    private final ImportJobStore importJobStore;

    @PostMapping("/importar")
    public ResponseEntity<Map<String, String>> importar(@Valid @RequestBody ImportarClubRequest request,
                                                        Authentication authentication) {
        Long userId = CurrentUser.userId(authentication);

        List<ImportarClubUseCase.JugadorData> jugadores = request.jugadores().stream()
                .map(j -> new ImportarClubUseCase.JugadorData(
                        j.nombre(), j.totalGoals(), j.partidosJugados(), j.golPorPartido()))
                .toList();

        var command = new ImportarClubUseCase.ImportarClubCommand(
                request.nombre(), request.temporada(), request.liga(),
                request.descripcion(), userId, jugadores);

        String jobId = importJobStore.crear();
        asyncImportarClubService.importarAsync(jobId, command);

        return ResponseEntity.accepted().body(Map.of("jobId", jobId));
    }

    @GetMapping("/importar/{jobId}")
    public ResponseEntity<ImportJobResponse> estado(@PathVariable String jobId) {
        return importJobStore.obtener(jobId)
                .map(job -> switch (job.estado()) {
                    case PENDING -> ResponseEntity.ok(new ImportJobResponse("PENDING", null, null));
                    case DONE    -> ResponseEntity.ok(new ImportJobResponse("DONE", toResponse(job.resultado()), null));
                    case FAILED  -> ResponseEntity.ok(new ImportJobResponse("FAILED", null, job.error()));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    private ImportarClubResponse toResponse(ImportarClubUseCase.ImportarClubResult result) {
        List<ImportarClubResponse.JugadorDto> jugadorDtos = result.jugadores().stream()
                .map(j -> new ImportarClubResponse.JugadorDto(
                        j.getId(), j.getNombre(), j.getTotalGoals(),
                        j.getPartidosJugados(), j.getGolPorPartido()))
                .toList();
        var eq = result.equipo();
        return new ImportarClubResponse(
                eq.getId(), eq.getNombre(), eq.getTemporada(),
                eq.getLiga(), eq.getDescripcion(), eq.getCreatedAt(),
                jugadorDtos);
    }
}
