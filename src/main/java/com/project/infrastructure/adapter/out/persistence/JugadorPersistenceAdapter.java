package com.project.infrastructure.adapter.out.persistence;

import com.project.application.port.out.JugadorRepository;
import com.project.domain.model.Jugador;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

// TODO: CONVERSIÓN DE IMÁGENES A WEBP
//
// ESTRATEGIA: guardar imagen original inmediatamente → convertir en segundo plano → batch nocturno como red de seguridad
//
// CAMPO foto_convertida (BOOLEAN, DEFAULT FALSE en BD):
//   - false → imagen pendiente de convertir a WebP
//   - true  → imagen ya en formato WebP
//   Se usa este campo (en vez de comprobar extensión) para garantizar fiabilidad al 100%.
//
// PIEZA 1 — Conversión async (@Async)
//   - Librería: Thumbnailator + webp-imageio
//   - Al subir/editar foto: guardar original, responder al usuario, lanzar conversión en hilo aparte
//   - Al terminar: sobreescribir fotoUrl con la ruta .webp y marcar fotoConvertida = true
//   - Implementar en: ImageConversionService (@Async) + ImageConversionPort (out)
//
// PIEZA 2 — Batch nocturno (@Scheduled)
//   - Cron: "0 0 3 * * *" (cada día a las 3:00 AM)
//   - Query: JugadorRepository.findByFotoConvertidaFalseAndFotoUrlNotNull()
//   - Para cada jugador: convertir a WebP, actualizar fotoUrl, marcar fotoConvertida = true
//   - Implementar en: ConversionBatchJob (@Component + @Scheduled)
//
// DEPENDENCIAS Maven a añadir:
//   net.coobird:thumbnailator:0.4.20
//   org.sejda.imageio:webp-imageio:0.1.6
//
// NOTA: añadir a JugadorJpaRepository:
//   List<JugadorEntity> findByFotoConvertidaFalseAndFotoUrlNotNull();

@Component
@RequiredArgsConstructor
public class JugadorPersistenceAdapter implements JugadorRepository {

    private final JugadorJpaRepository jpaRepository;

    @Override
    public Jugador save(Jugador jugador) {
        return toDomain(jpaRepository.save(toEntity(jugador)));
    }

    @Override
    public List<Jugador> findByEquipoId(Long equipoId) {
        return jpaRepository.findByEquipoId(equipoId).stream().map(this::toDomain).toList();
    }

    @Override
    public Optional<Jugador> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    private Jugador toDomain(JugadorEntity e) {
        return Jugador.builder()
                .id(e.getId())
                .nombre(e.getNombre())
                .posicion(e.getPosicion())
                .dorsal(e.getDorsal())
                .edad(e.getEdad())
                .totalGoals(e.getTotalGoals())
                .partidosJugados(e.getPartidosJugados())
                .golPorPartido(e.getGolPorPartido())
                .fotoUrl(e.getFotoUrl())
                .fotoConvertida(e.isFotoConvertida())
                .equipoId(e.getEquipoId())
                .build();
    }

    private JugadorEntity toEntity(Jugador j) {
        return JugadorEntity.builder()
                .id(j.getId())
                .nombre(j.getNombre())
                .posicion(j.getPosicion())
                .dorsal(j.getDorsal())
                .edad(j.getEdad())
                .totalGoals(j.getTotalGoals())
                .partidosJugados(j.getPartidosJugados())
                .golPorPartido(j.getGolPorPartido())
                .fotoUrl(j.getFotoUrl())
                .fotoConvertida(j.isFotoConvertida())
                .equipoId(j.getEquipoId())
                .build();
    }
}
