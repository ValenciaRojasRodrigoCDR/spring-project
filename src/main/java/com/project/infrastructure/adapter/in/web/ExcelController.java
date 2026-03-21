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

@RestController
@RequestMapping("/api/excel")
@RequiredArgsConstructor
public class ExcelController {

    private final ExcelUploadUseCase excelUploadUseCase;

    @PostMapping("/upload")
    public ResponseEntity<ExcelData> upload(@RequestParam("file") MultipartFile file) {
        try {
            return ResponseEntity.ok(excelUploadUseCase.upload(file.getInputStream()));
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se pudo leer el fichero", e);
        }
    }
}
