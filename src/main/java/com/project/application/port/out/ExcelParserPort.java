package com.project.application.port.out;

import com.project.domain.model.ExcelData;

import java.io.InputStream;

public interface ExcelParserPort {

    ExcelData parse(InputStream inputStream);
}
