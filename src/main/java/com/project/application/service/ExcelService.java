package com.project.application.service;

import com.project.application.port.in.ExcelUploadUseCase;
import com.project.application.port.out.ExcelParserPort;
import com.project.domain.model.ExcelData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Service
@RequiredArgsConstructor
public class ExcelService implements ExcelUploadUseCase {

    private final ExcelParserPort excelParserPort;

    @Override
    public ExcelData upload(InputStream inputStream) {
        return excelParserPort.parse(inputStream);
    }
}
