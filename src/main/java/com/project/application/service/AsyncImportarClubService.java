package com.project.application.service;

import com.project.application.port.in.ImportarClubUseCase;
import com.project.infrastructure.store.ImportJobStore;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AsyncImportarClubService {

    private final ImportarClubUseCase importarClubUseCase;
    private final ImportJobStore importJobStore;

    @Async("importExecutor")
    public void importarAsync(String jobId, ImportarClubUseCase.ImportarClubCommand command) {
        try {
            var resultado = importarClubUseCase.importar(command);
            importJobStore.completar(jobId, resultado);
        } catch (Exception e) {
            importJobStore.fallar(jobId, e.getMessage());
        }
    }
}
