package com.project.infrastructure.adapter.in.web;

import com.project.application.port.in.ExcelUploadUseCase;
import com.project.domain.model.ExcelData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ExcelControllerTest {

    @Mock ExcelUploadUseCase excelUploadUseCase;
    @InjectMocks ExcelController excelController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(excelController).build();
    }

    @Test
    void upload_validXlsx_returns200() throws Exception {
        ExcelData data = ExcelData.builder().players(List.of()).matches(List.of()).build();
        when(excelUploadUseCase.upload(any())).thenReturn(data);

        MockMultipartFile file = new MockMultipartFile(
                "file", "data.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                new byte[]{1, 2, 3});

        mockMvc.perform(multipart("/api/excel/upload").file(file))
                .andExpect(status().isOk());
    }

    @Test
    void upload_validXls_returns200() throws Exception {
        ExcelData data = ExcelData.builder().players(List.of()).matches(List.of()).build();
        when(excelUploadUseCase.upload(any())).thenReturn(data);

        MockMultipartFile file = new MockMultipartFile(
                "file", "data.xls",
                "application/vnd.ms-excel",
                new byte[]{1, 2, 3});

        mockMvc.perform(multipart("/api/excel/upload").file(file))
                .andExpect(status().isOk());
    }

    @Test
    void upload_emptyFile_returns400() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "data.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                new byte[0]);

        mockMvc.perform(multipart("/api/excel/upload").file(file))
                .andExpect(status().isBadRequest());
    }

    @Test
    void upload_invalidContentType_returns415() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "data.pdf",
                "application/pdf",
                new byte[]{1, 2, 3});

        mockMvc.perform(multipart("/api/excel/upload").file(file))
                .andExpect(status().isUnsupportedMediaType());
    }
}
