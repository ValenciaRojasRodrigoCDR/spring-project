package com.project.application.service;

import com.project.application.port.in.CreateEquipoUseCase;
import com.project.application.port.in.GetEquiposQuery;
import com.project.application.port.in.UpdateEquipoUseCase;
import com.project.application.port.out.EquipoRepository;
import com.project.domain.exception.EquipoNotFoundException;
import com.project.domain.exception.UnauthorizedEquipoAccessException;
import com.project.domain.model.Equipo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EquipoService implements CreateEquipoUseCase, GetEquiposQuery, UpdateEquipoUseCase {

    private final EquipoRepository equipoRepository;

    @Override
    public Equipo create(CreateEquipoCommand command) {
        return equipoRepository.save(Equipo.builder()
                .nombre(command.nombre())
                .temporada(command.temporada())
                .liga(command.liga())
                .descripcion(command.descripcion())
                .userId(command.userId())
                .build());
    }

    @Override
    public List<Equipo> getByUserId(Long userId) {
        return equipoRepository.findByUserId(userId);
    }

    @Override
    public Equipo update(UpdateEquipoCommand command) {
        Equipo existing = equipoRepository.findById(command.id())
                .orElseThrow(() -> new EquipoNotFoundException(command.id()));

        if (!existing.getUserId().equals(command.requestingUserId())) {
            throw new UnauthorizedEquipoAccessException();
        }

        return equipoRepository.save(Equipo.builder()
                .id(existing.getId())
                .nombre(command.nombre())
                .temporada(command.temporada())
                .liga(command.liga())
                .descripcion(command.descripcion())
                .createdAt(existing.getCreatedAt())
                .userId(existing.getUserId())
                .build());
    }
}
