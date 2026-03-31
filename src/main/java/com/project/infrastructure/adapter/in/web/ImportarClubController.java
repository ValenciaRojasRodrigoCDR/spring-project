package com.project.infrastructure.adapter.in.web;

import com.project.application.port.in.GetUserQuery;
import com.project.application.port.in.ImportarClubUseCase;
import com.project.infrastructure.adapter.in.web.dto.ImportarClubRequest;
import com.project.infrastructure.adapter.in.web.dto.ImportarClubResponse;
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
public class ImportarClubController {

    private final ImportarClubUseCase importarClubUseCase;
    private final GetUserQuery getUserQuery;

    @PostMapping("/importar")
    public ResponseEntity<ImportarClubResponse> importar(@Valid @RequestBody ImportarClubRequest request,
                                                          Authentication authentication) {
        Long userId = getUserQuery.getByUsername(authentication.getName()).getId();

        List<ImportarClubUseCase.JugadorData> jugadores = request.jugadores().stream()
                .map(j -> new ImportarClubUseCase.JugadorData(
                        j.nombre(), j.totalGoals(), j.partidosJugados(), j.golPorPartido()))
                .toList();

        var result = importarClubUseCase.importar(new ImportarClubUseCase.ImportarClubCommand(
                request.nombre(), request.temporada(), request.liga(),
                request.descripcion(), userId, jugadores));

        List<ImportarClubResponse.JugadorDto> jugadorDtos = result.jugadores().stream()
                .map(j -> new ImportarClubResponse.JugadorDto(
                        j.getId(), j.getNombre(), j.getTotalGoals(),
                        j.getPartidosJugados(), j.getGolPorPartido()))
                .toList();

        var equipo = result.equipo();
        return ResponseEntity.status(HttpStatus.CREATED).body(new ImportarClubResponse(
                equipo.getId(), equipo.getNombre(), equipo.getTemporada(),
                equipo.getLiga(), equipo.getDescripcion(), equipo.getCreatedAt(),
                jugadorDtos));
    }
}
