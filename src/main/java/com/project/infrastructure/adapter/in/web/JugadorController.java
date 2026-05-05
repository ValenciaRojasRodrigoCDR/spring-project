package com.project.infrastructure.adapter.in.web;

import com.project.application.port.in.CreateJugadorUseCase;
import com.project.application.port.in.UpdateJugadorUseCase;
import com.project.application.port.out.JugadorRepository;
import com.project.domain.model.Jugador;
import com.project.infrastructure.adapter.in.web.dto.JugadorResponse;
import com.project.infrastructure.util.Constants;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Validated
@RestController
@RequestMapping("/api/jugadores")
@RequiredArgsConstructor
public class JugadorController {

    private final CreateJugadorUseCase createJugadorUseCase;
    private final UpdateJugadorUseCase updateJugadorUseCase;
    private final JugadorRepository    jugadorRepository;

    @Value("${app.upload-dir}")
    private String uploadDir;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<JugadorResponse> create(
            @NotBlank @RequestParam String nombre,
            @RequestParam(required = false) String posicion,
            @Min(0) @RequestParam(required = false) Integer dorsal,
            @Min(0) @RequestParam(required = false) Integer edad,
            @RequestParam Long equipoId,
            @RequestParam(required = false) MultipartFile foto) {

        Jugador jugador = createJugadorUseCase.create(new CreateJugadorUseCase.CreateJugadorCommand(
                nombre, posicion, dorsal, edad, equipoId, foto));
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(jugador));
    }

    @GetMapping("/{id}")
    public ResponseEntity<JugadorResponse> getById(@PathVariable Long id) {
        return jugadorRepository.findById(id)
                .map(j -> ResponseEntity.ok(toResponse(j)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<JugadorResponse> update(
            @PathVariable Long id,
            @NotBlank @RequestParam String nombre,
            @RequestParam(required = false) String posicion,
            @Min(0) @RequestParam(required = false) Integer dorsal,
            @Min(0) @RequestParam(required = false) Integer edad,
            @RequestParam(required = false) MultipartFile foto,
            Authentication authentication) {

        // JUGADOR role can only edit their own profile
        if (isJugadorRole(authentication)) {
            Long myJugadorId = extractJugadorId(authentication);
            if (myJugadorId == null || !myJugadorId.equals(id)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        Jugador jugador = updateJugadorUseCase.update(new UpdateJugadorUseCase.UpdateJugadorCommand(
                id, nombre, posicion, dorsal, edad, foto));
        return ResponseEntity.ok(toResponse(jugador));
    }

    @GetMapping("/{id}/foto")
    public ResponseEntity<Resource> getFoto(@PathVariable Long id) {
        var jugador = jugadorRepository.findById(id).orElse(null);
        if (jugador == null || jugador.getFotoUrl() == null) {
            return ResponseEntity.notFound().build();
        }
        try {
            Path path = Paths.get(uploadDir, jugador.getFotoUrl());
            Resource resource = new FileSystemResource(path);
            if (!resource.exists()) return ResponseEntity.notFound().build();
            return ResponseEntity.ok()
                    .contentType(resolveMediaType(jugador.getFotoUrl()))
                    .header(HttpHeaders.CACHE_CONTROL, "max-age=86400, public")
                    .contentLength(resource.contentLength())
                    .body(resource);
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }

    private boolean isJugadorRole(Authentication auth) {
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + Constants.ROLE_JUGADOR));
    }

    private Long extractJugadorId(Authentication auth) {
        if (auth instanceof UsernamePasswordAuthenticationToken token) {
            Object details = token.getDetails();
            return details instanceof Long ? (Long) details : null;
        }
        return null;
    }

    private MediaType resolveMediaType(String filename) {
        if (filename.endsWith(".png"))  return MediaType.IMAGE_PNG;
        if (filename.endsWith(".webp")) return MediaType.parseMediaType("image/webp");
        return MediaType.IMAGE_JPEG;
    }

    private JugadorResponse toResponse(Jugador j) {
        return new JugadorResponse(j.getId(), j.getNombre(), j.getPosicion(),
                j.getDorsal(), j.getEdad(), j.getTotalGoals(),
                j.getPartidosJugados(), j.getGolPorPartido(), j.getFotoUrl());
    }
}
