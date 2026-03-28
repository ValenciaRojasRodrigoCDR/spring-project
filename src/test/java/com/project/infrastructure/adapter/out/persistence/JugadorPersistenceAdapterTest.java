package com.project.infrastructure.adapter.out.persistence;

import com.project.domain.model.Jugador;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JugadorPersistenceAdapterTest {

    @Mock JugadorJpaRepository jpaRepository;
    @InjectMocks JugadorPersistenceAdapter adapter;

    private JugadorEntity buildEntity() {
        return JugadorEntity.builder().id(1L).nombre("Leo").posicion("DEL")
                .dorsal(10).edad(25).totalGoals(5).partidosJugados(10)
                .golPorPartido(0.5).fotoUrl("jugadores/img.jpg").equipoId(3L).build();
    }

    private Jugador buildDomain() {
        return Jugador.builder().id(1L).nombre("Leo").posicion("DEL")
                .dorsal(10).edad(25).totalGoals(5).partidosJugados(10)
                .golPorPartido(0.5).fotoUrl("jugadores/img.jpg").equipoId(3L).build();
    }

    @Test
    void save_mapsAndReturnsDomain() {
        when(jpaRepository.save(any())).thenReturn(buildEntity());

        Jugador result = adapter.save(buildDomain());

        assertThat(result.getNombre()).isEqualTo("Leo");
        assertThat(result.getTotalGoals()).isEqualTo(5);
        assertThat(result.getFotoUrl()).isEqualTo("jugadores/img.jpg");
        verify(jpaRepository).save(any());
    }

    @Test
    void findByEquipoId_returnsMappedList() {
        when(jpaRepository.findByEquipoId(3L)).thenReturn(List.of(buildEntity()));

        List<Jugador> result = adapter.findByEquipoId(3L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPosicion()).isEqualTo("DEL");
    }

    @Test
    void findById_found_returnsMappedDomain() {
        when(jpaRepository.findById(1L)).thenReturn(Optional.of(buildEntity()));

        Optional<Jugador> result = adapter.findById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getDorsal()).isEqualTo(10);
    }

    @Test
    void findById_notFound_returnsEmpty() {
        when(jpaRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Jugador> result = adapter.findById(99L);

        assertThat(result).isEmpty();
    }
}
