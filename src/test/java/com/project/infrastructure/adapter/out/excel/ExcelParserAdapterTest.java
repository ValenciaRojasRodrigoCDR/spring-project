package com.project.infrastructure.adapter.out.excel;

import com.project.domain.model.ExcelData;
import com.project.domain.model.JornadaStatus;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExcelParserAdapterTest {

    private ExcelParserAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new ExcelParserAdapter();
    }

    private InputStream buildWorkbook(WorkbookConfigurer configurer) throws IOException {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet();
            configurer.configure(sheet);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            wb.write(baos);
            return new ByteArrayInputStream(baos.toByteArray());
        }
    }

    @FunctionalInterface
    interface WorkbookConfigurer {
        void configure(Sheet sheet);
    }

    private void setString(Row row, int col, String value) {
        row.createCell(col).setCellValue(value);
    }

    private void setNumeric(Row row, int col, double value) {
        row.createCell(col).setCellValue(value);
    }

    @Test
    void parse_emptySheet_returnsEmptyLists() throws IOException {
        InputStream is = buildWorkbook(sheet -> {});

        ExcelData data = adapter.parse(is);

        assertThat(data.getPlayers()).isEmpty();
        assertThat(data.getMatches()).isEmpty();
    }

    @Test
    void parse_onePlayerWithNumericGoals_parsesCorrectly() throws IOException {
        InputStream is = buildWorkbook(sheet -> {
            Row row = sheet.createRow(2);
            setString(row, 0, "Messi");
            setNumeric(row, 1, 3);   // J1 goals
            setNumeric(row, 24, 3);  // totalGoals (col Y)
            setNumeric(row, 26, 1);  // partidosJugados (col AA)
            setNumeric(row, 27, 3.0);// golPorPartido (col AB)
        });

        ExcelData data = adapter.parse(is);

        assertThat(data.getPlayers()).hasSize(1);
        assertThat(data.getPlayers().get(0).getName()).isEqualTo("Messi");
        assertThat(data.getPlayers().get(0).getTotalGoals()).isEqualTo(3);
        assertThat(data.getPlayers().get(0).getPartidosJugados()).isEqualTo(1);
        assertThat(data.getPlayers().get(0).getJornadas().get("J1").status()).isEqualTo(JornadaStatus.GOALS);
        assertThat(data.getPlayers().get(0).getJornadas().get("J1").goals()).isEqualTo(3);
    }

    @Test
    void parse_playerWithAbsent_parsesX() throws IOException {
        InputStream is = buildWorkbook(sheet -> {
            Row row = sheet.createRow(2);
            setString(row, 0, "Ronaldo");
            setString(row, 1, "x");  // J1 absent
        });

        ExcelData data = adapter.parse(is);

        assertThat(data.getPlayers().get(0).getJornadas().get("J1").status()).isEqualTo(JornadaStatus.ABSENT);
    }

    @Test
    void parse_playerWithNoPresentado_parsesCorrectly() throws IOException {
        InputStream is = buildWorkbook(sheet -> {
            Row row = sheet.createRow(2);
            setString(row, 0, "Xavi");
            setString(row, 1, "NO PRESENTADO");
        });

        ExcelData data = adapter.parse(is);

        assertThat(data.getPlayers().get(0).getJornadas().get("J1").status()).isEqualTo(JornadaStatus.NO_PRESENTADO);
    }

    @Test
    void parse_playerWithBlankNameRow_isSkipped() throws IOException {
        InputStream is = buildWorkbook(sheet -> {
            Row row = sheet.createRow(2);
            setString(row, 0, "  ");  // blank name
            Row row2 = sheet.createRow(3);
            setString(row2, 0, "Iniesta");
        });

        ExcelData data = adapter.parse(is);

        assertThat(data.getPlayers()).hasSize(1);
        assertThat(data.getPlayers().get(0).getName()).isEqualTo("Iniesta");
    }

    @Test
    void parse_playerWithStringGoals_parsesAsNumber() throws IOException {
        InputStream is = buildWorkbook(sheet -> {
            Row row = sheet.createRow(2);
            setString(row, 0, "Suarez");
            setString(row, 1, "2");  // J1 as string number
        });

        ExcelData data = adapter.parse(is);

        assertThat(data.getPlayers().get(0).getJornadas().get("J1").status()).isEqualTo(JornadaStatus.GOALS);
        assertThat(data.getPlayers().get(0).getJornadas().get("J1").goals()).isEqualTo(2);
    }

    @Test
    void parse_playerWithInvalidStringInGoalCell_setsPending() throws IOException {
        InputStream is = buildWorkbook(sheet -> {
            Row row = sheet.createRow(2);
            setString(row, 0, "Pique");
            setString(row, 1, "??");  // invalid
        });

        ExcelData data = adapter.parse(is);

        assertThat(data.getPlayers().get(0).getJornadas().get("J1").status()).isEqualTo(JornadaStatus.PENDING);
    }

    @Test
    void parse_matchDataInRivalsRow_parsesMatch() throws IOException {
        InputStream is = buildWorkbook(sheet -> {
            Row rivals = sheet.createRow(20);
            setString(rivals, 1, "Real Madrid");  // J1 rival
        });

        ExcelData data = adapter.parse(is);

        assertThat(data.getMatches()).hasSize(1);
        assertThat(data.getMatches().get(0).getJornada()).isEqualTo("J1");
        assertThat(data.getMatches().get(0).getRival()).isEqualTo("Real Madrid");
    }

    @Test
    void parse_j12columnInSecondBlock_parsedCorrectly() throws IOException {
        InputStream is = buildWorkbook(sheet -> {
            Row row = sheet.createRow(2);
            setString(row, 0, "Alba");
            setNumeric(row, 13, 1);  // J12 (col N, index 13)
        });

        ExcelData data = adapter.parse(is);

        assertThat(data.getPlayers().get(0).getJornadas().get("J12").status()).isEqualTo(JornadaStatus.GOALS);
    }

    @Test
    void parse_multiplePlayersInRange_allParsed() throws IOException {
        InputStream is = buildWorkbook(sheet -> {
            for (int r = 2; r <= 17; r++) {
                Row row = sheet.createRow(r);
                setString(row, 0, "Player" + r);
            }
        });

        ExcelData data = adapter.parse(is);

        assertThat(data.getPlayers()).hasSize(16);
    }

    @Test
    void parse_invalidInputStream_throwsRuntimeException() {
        InputStream badStream = new ByteArrayInputStream(new byte[]{1, 2, 3, 4});

        assertThatThrownBy(() -> adapter.parse(badStream))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Error al parsear el Excel");
    }

    @Test
    void parse_nullGoalCell_returnsPending() throws IOException {
        InputStream is = buildWorkbook(sheet -> {
            Row row = sheet.createRow(2);
            setString(row, 0, "Busquets");
            // col 1 (J1) left as null
        });

        ExcelData data = adapter.parse(is);

        assertThat(data.getPlayers().get(0).getJornadas().get("J1").status()).isEqualTo(JornadaStatus.PENDING);
    }
}
