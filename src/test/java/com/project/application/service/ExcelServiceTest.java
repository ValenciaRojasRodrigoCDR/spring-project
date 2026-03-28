package com.project.application.service;

import com.project.application.port.out.ExcelParserPort;
import com.project.domain.model.ExcelData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.InputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExcelServiceTest {

    @Mock ExcelParserPort excelParserPort;
    @InjectMocks ExcelService excelService;

    @Test
    void upload_delegatesToParser_andReturnsResult() {
        InputStream inputStream = mock(InputStream.class);
        ExcelData expected = ExcelData.builder().players(List.of()).matches(List.of()).build();
        when(excelParserPort.parse(inputStream)).thenReturn(expected);

        ExcelData result = excelService.upload(inputStream);

        assertThat(result).isSameAs(expected);
        verify(excelParserPort).parse(inputStream);
    }
}
