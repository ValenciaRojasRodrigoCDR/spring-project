package com.project.application.port.in;

import com.project.domain.model.ExcelData;

import java.io.InputStream;

public interface ExcelUploadUseCase {

    ExcelData upload(InputStream inputStream);
}
