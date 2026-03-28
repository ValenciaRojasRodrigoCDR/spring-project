package com.project.infrastructure.adapter.out.persistence;

import com.project.domain.model.Equipo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EquipoPersistenceAdapterTest {

    @Mock EquipoJpaRepository jpaRepository;
    @InjectMocks EquipoPersistenceAdapter adapter;

    private EquipoEntity buildEntity() {
        return EquipoEntity.builder().id(1L).nombre("FC Test").temporada("2024")
                .liga("Liga A").descripcion("Desc").userId(10L)
                .createdAt(LocalDateTime.of(2024, 1, 1, 0, 0)).build();
    }

    private Equipo buildDomain() {
        return Equipo.builder().id(1L).nombre("FC Test").temporada("2024")
                .liga("Liga A").descripcion("Desc").userId(10L)
                .createdAt(LocalDateTime.of(2024, 1, 1, 0, 0)).build();
    }

    @Test
    void save_mapsToEntityAndReturnsDomain() {
        EquipoEntity entity = buildEntity();
        when(jpaRepository.save(any())).thenReturn(entity);

        Equipo result = adapter.save(buildDomain());

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getNombre()).isEqualTo("FC Test");
        assertThat(result.getUserId()).isEqualTo(10L);
        verify(jpaRepository).save(any());
    }

    @Test
    void findById_found_returnsMappedDomain() {
        when(jpaRepository.findById(1L)).thenReturn(Optional.of(buildEntity()));

        Optional<Equipo> result = adapter.findById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getNombre()).isEqualTo("FC Test");
    }

    @Test
    void findById_notFound_returnsEmpty() {
        when(jpaRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Equipo> result = adapter.findById(99L);

        assertThat(result).isEmpty();
    }

    @Test
    void findByUserId_returnsMappedList() {
        when(jpaRepository.findByUserId(10L)).thenReturn(List.of(buildEntity()));

        List<Equipo> result = adapter.findByUserId(10L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getLiga()).isEqualTo("Liga A");
    }
}
