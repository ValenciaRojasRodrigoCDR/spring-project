package com.project.infrastructure.adapter.in.web;

import com.project.application.port.in.ExcelUploadUseCase;
import com.project.domain.model.ExcelData;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Set;

@RestController
@RequestMapping("/api/excel")
@RequiredArgsConstructor
public class ExcelController {

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/vnd.ms-excel"
    );

    private final ExcelUploadUseCase excelUploadUseCase;

    @PostMapping("/upload")
    public ResponseEntity<ExcelData> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El fichero está vacío");
        }
        if (!isExcel(file)) {
            throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                    "Solo se admiten ficheros Excel (.xlsx, .xls)");
        }
        try {
            return ResponseEntity.ok(excelUploadUseCase.upload(file.getInputStream()));
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se pudo leer el fichero", e);
        }
    }

    private boolean isExcel(MultipartFile file) {
        String ct   = file.getContentType();
        String name = file.getOriginalFilename();
        boolean validType = ct != null && ALLOWED_TYPES.contains(ct);
        boolean validExt  = name != null &&
                (name.toLowerCase().endsWith(".xlsx") || name.toLowerCase().endsWith(".xls"));
        return validType && validExt;
    }
}
