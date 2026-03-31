package com.project.infrastructure.adapter.in.web;

import com.project.application.port.in.CreateJugadorUseCase;
import com.project.application.port.in.UpdateJugadorUseCase;
import com.project.application.port.out.JugadorRepository;
import com.project.domain.model.Jugador;
import com.project.infrastructure.adapter.in.web.dto.JugadorResponse;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Validated
@RestController
@RequestMapping("/api/jugadores")
@RequiredArgsConstructor
public class JugadorController {

    private final CreateJugadorUseCase createJugadorUseCase;
    private final UpdateJugadorUseCase updateJugadorUseCase;
    private final JugadorRepository jugadorRepository;

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
            @RequestParam(required = false) MultipartFile foto) {

        Jugador jugador = updateJugadorUseCase.update(new UpdateJugadorUseCase.UpdateJugadorCommand(
                id, nombre, posicion, dorsal, edad, foto));
        return ResponseEntity.ok(toResponse(jugador));
    }

    @GetMapping("/{id}/foto")
    public ResponseEntity<byte[]> getFoto(@PathVariable Long id) {
        var jugador = jugadorRepository.findById(id).orElse(null);
        if (jugador == null || jugador.getFotoUrl() == null) {
            return ResponseEntity.notFound().build();
        }
        try {
            Path path = Paths.get(uploadDir, jugador.getFotoUrl());
            byte[] bytes = Files.readAllBytes(path);
            return ResponseEntity.ok().contentType(resolveMediaType(jugador.getFotoUrl())).body(bytes);
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }

    private MediaType resolveMediaType(String filename) {
        if (filename.endsWith(".png")) return MediaType.IMAGE_PNG;
        if (filename.endsWith(".webp")) return MediaType.parseMediaType("image/webp");
        return MediaType.IMAGE_JPEG;
    }

    private JugadorResponse toResponse(Jugador j) {
        return new JugadorResponse(j.getId(), j.getNombre(), j.getPosicion(),
                j.getDorsal(), j.getEdad(), j.getTotalGoals(),
                j.getPartidosJugados(), j.getGolPorPartido(), j.getFotoUrl());
    }
}
