package com.project.application.service;

import com.project.application.port.in.CreateEquipoUseCase;
import com.project.application.port.in.GetEquiposQuery;
import com.project.application.port.out.EquipoRepository;
import com.project.domain.model.Equipo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EquipoService implements CreateEquipoUseCase, GetEquiposQuery {

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
}
