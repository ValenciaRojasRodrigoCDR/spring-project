package com.project.application.service;

import com.project.application.port.in.AddEquipoToLigaUseCase;
import com.project.application.port.in.CreateLigaUseCase;
import com.project.application.port.in.DeleteLigaUseCase;
import com.project.application.port.in.GetLigasQuery;
import com.project.application.port.in.UpdateLigaUseCase;
import com.project.application.port.out.LigaRepository;
import com.project.domain.exception.LigaNotFoundException;
import com.project.domain.exception.UnauthorizedLigaAccessException;
import com.project.domain.model.Liga;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LigaService implements CreateLigaUseCase, GetLigasQuery, UpdateLigaUseCase, DeleteLigaUseCase, AddEquipoToLigaUseCase {

    private final LigaRepository ligaRepository;

    @Override
    public Liga create(CreateLigaCommand command) {
        return ligaRepository.save(Liga.builder()
                .nombre(command.nombre())
                .temporada(command.temporada())
                .descripcion(command.descripcion())
                .userId(command.userId())
                .build());
    }

    @Override
    public List<Liga> getByUserId(Long userId) {
        return ligaRepository.findByUserId(userId);
    }

    @Override
    public Liga getById(Long id) {
        return ligaRepository.findById(id)
                .orElseThrow(() -> new LigaNotFoundException(id));
    }

    @Override
    public Liga update(UpdateLigaCommand command) {
        Liga existing = ligaRepository.findById(command.id())
                .orElseThrow(() -> new LigaNotFoundException(command.id()));
        if (!existing.getUserId().equals(command.requestingUserId())) {
            throw new UnauthorizedLigaAccessException();
        }
        return ligaRepository.save(Liga.builder()
                .id(existing.getId())
                .nombre(command.nombre())
                .temporada(command.temporada())
                .descripcion(command.descripcion())
                .createdAt(existing.getCreatedAt())
                .userId(existing.getUserId())
                .build());
    }

    @Override
    public void delete(Long ligaId, Long requestingUserId) {
        Liga existing = ligaRepository.findById(ligaId)
                .orElseThrow(() -> new LigaNotFoundException(ligaId));
        if (!existing.getUserId().equals(requestingUserId)) {
            throw new UnauthorizedLigaAccessException();
        }
        ligaRepository.deleteById(ligaId);
    }

    @Override
    public void addEquipo(Long ligaId, Long equipoId, Long requestingUserId) {
        Liga liga = ligaRepository.findById(ligaId)
                .orElseThrow(() -> new LigaNotFoundException(ligaId));
        if (!liga.getUserId().equals(requestingUserId)) {
            throw new UnauthorizedLigaAccessException();
        }
        ligaRepository.addEquipo(ligaId, equipoId);
    }

    @Override
    public void removeEquipo(Long ligaId, Long equipoId, Long requestingUserId) {
        Liga liga = ligaRepository.findById(ligaId)
                .orElseThrow(() -> new LigaNotFoundException(ligaId));
        if (!liga.getUserId().equals(requestingUserId)) {
            throw new UnauthorizedLigaAccessException();
        }
        ligaRepository.removeEquipo(ligaId, equipoId);
    }

    @Override
    public List<Long> getEquipoIds(Long ligaId) {
        return ligaRepository.findEquipoIdsByLigaId(ligaId);
    }

    @Override
    public Map<Long, List<Long>> getEquipoIdsByLigaIds(List<Long> ligaIds) {
        return ligaIds.isEmpty() ? Map.of() : ligaRepository.findEquipoIdsByLigaIds(ligaIds);
    }
}
