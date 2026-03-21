package com.project.infrastructure.adapter.out.excel;

import com.project.application.port.out.ExcelParserPort;
import com.project.domain.model.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.*;

/**
 * Parsea el fichero Los ficheros obtenidos.
 *
 * Columnas de jornada (0-based):
 *   J1-J11  → cols 1-11  (B-L)
 *   J12-J22 → cols 13-23 (N-X)
 *   col 12 (M) = separador "SEGUNDA VUELTA", se ignora
 *
 * Filas clave (0-based):
 *   0       → título
 *   1       → cabecera
 *   2-17    → jugadores (16 jugadores)
 *   19      → total goles equipo por jornada  (fila 20 en Excel)
 *   20      → nombres de rivales              (fila 21)
 *   28      → fechas                          (fila 29)
 *   30      → resultados                      (fila 31)
 */
@Component
public class ExcelParserAdapter implements ExcelParserPort {

    // Índices de columna para cada jornada (0-based)
    private static final int[] JORNADA_COLS = {
            1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11,
            13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23
    };
    private static final String[] JORNADA_NAMES = {
            "J1", "J2", "J3", "J4", "J5", "J6", "J7", "J8", "J9", "J10", "J11",
            "J12", "J13", "J14", "J15", "J16", "J17", "J18", "J19", "J20", "J21", "J22"
    };

    private static final int PLAYER_START_ROW    = 2;
    private static final int PLAYER_END_ROW      = 17;
    private static final int TEAM_TOTAL_ROW      = 19;
    private static final int RIVALS_ROW          = 20;
    private static final int DATES_ROW           = 28;
    private static final int RESULTS_ROW         = 30;

    private static final int TOTAL_COL            = 24; // Y
    private static final int PARTIDOS_COL         = 26; // AA
    private static final int GOL_POR_PARTIDO_COL  = 27; // AB

    @Override
    public ExcelData parse(InputStream inputStream) {
        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            return ExcelData.builder()
                    .players(parsePlayers(sheet))
                    .matches(parseMatches(sheet))
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Error al parsear el Excel: " + e.getMessage(), e);
        }
    }

    // ── Jugadores ────────────────────────────────────────────────────────────

    private List<PlayerStats> parsePlayers(Sheet sheet) {
        List<PlayerStats> players = new ArrayList<>();

        for (int r = PLAYER_START_ROW; r <= PLAYER_END_ROW; r++) {
            Row row = sheet.getRow(r);
            if (row == null) continue;

            String name = getString(row.getCell(0));
            if (name == null || name.isBlank()) continue;

            Map<String, JornadaEntry> jornadas = new LinkedHashMap<>();
            for (int i = 0; i < JORNADA_COLS.length; i++) {
                jornadas.put(JORNADA_NAMES[i], parseGoalCell(row.getCell(JORNADA_COLS[i])));
            }

            players.add(PlayerStats.builder()
                    .name(name)
                    .jornadas(jornadas)
                    .totalGoals(getInt(row.getCell(TOTAL_COL)))
                    .partidosJugados(getInt(row.getCell(PARTIDOS_COL)))
                    .golPorPartido(getDouble(row.getCell(GOL_POR_PARTIDO_COL)))
                    .build());
        }

        return players;
    }

    // ── Partidos ─────────────────────────────────────────────────────────────

    private List<MatchInfo> parseMatches(Sheet sheet) {
        Row rivalsRow  = sheet.getRow(RIVALS_ROW);
        Row datesRow   = sheet.getRow(DATES_ROW);
        Row resultsRow = sheet.getRow(RESULTS_ROW);
        Row totalsRow  = sheet.getRow(TEAM_TOTAL_ROW);

        List<MatchInfo> matches = new ArrayList<>();

        for (int i = 0; i < JORNADA_COLS.length; i++) {
            int col = JORNADA_COLS[i];

            String    rival   = rivalsRow  != null ? getString(rivalsRow.getCell(col))  : null;
            LocalDate date    = datesRow   != null ? getDate(datesRow.getCell(col))     : null;
            String    result  = resultsRow != null ? getString(resultsRow.getCell(col)) : null;
            Integer   goals   = totalsRow  != null ? getInt(totalsRow.getCell(col))     : null;

            if (rival != null || date != null || result != null) {
                matches.add(MatchInfo.builder()
                        .jornada(JORNADA_NAMES[i])
                        .rival(rival)
                        .date(date)
                        .result(result)
                        .teamTotalGoals(goals)
                        .build());
            }
        }

        return matches;
    }

    // ── Helpers de celda ────────────────────────────────────────────────────

    private JornadaEntry parseGoalCell(Cell cell) {
        if (cell == null) return new JornadaEntry(JornadaStatus.PENDING, 0);

        return switch (resolveType(cell)) {
            case NUMERIC -> new JornadaEntry(JornadaStatus.GOALS, (int) cell.getNumericCellValue());
            case STRING  -> {
                String v = cell.getStringCellValue().trim();
                if (v.equalsIgnoreCase("x"))              yield new JornadaEntry(JornadaStatus.ABSENT, 0);
                if (v.equalsIgnoreCase("NO PRESENTADO"))  yield new JornadaEntry(JornadaStatus.NO_PRESENTADO, 0);
                if (v.isBlank())                          yield new JornadaEntry(JornadaStatus.PENDING, 0);
                try {
                    yield new JornadaEntry(JornadaStatus.GOALS, Integer.parseInt(v));
                } catch (NumberFormatException e) {
                    yield new JornadaEntry(JornadaStatus.PENDING, 0);
                }
            }
            default -> new JornadaEntry(JornadaStatus.PENDING, 0);
        };
    }

    private String getString(Cell cell) {
        if (cell == null) return null;
        return switch (resolveType(cell)) {
            case STRING  -> {
                String v = cell.getStringCellValue().trim();
                yield v.isBlank() ? null : v;
            }
            case NUMERIC -> {
                double v = cell.getNumericCellValue();
                yield v == Math.floor(v) ? String.valueOf((int) v) : String.valueOf(v);
            }
            default -> null;
        };
    }

    private LocalDate getDate(Cell cell) {
        if (cell == null || resolveType(cell) != CellType.NUMERIC) return null;
        if (DateUtil.isCellDateFormatted(cell)) return cell.getLocalDateTimeCellValue().toLocalDate();
        return null;
    }

    private int getInt(Cell cell) {
        if (cell == null) return 0;
        if (resolveType(cell) == CellType.NUMERIC) return (int) cell.getNumericCellValue();
        return 0;
    }

    private double getDouble(Cell cell) {
        if (cell == null) return 0.0;
        if (resolveType(cell) == CellType.NUMERIC) return cell.getNumericCellValue();
        return 0.0;
    }

    /** Resuelve el tipo real de una celda, desenvuelve fórmulas. */
    private CellType resolveType(Cell cell) {
        if (cell.getCellType() == CellType.FORMULA) return cell.getCachedFormulaResultType();
        return cell.getCellType();
    }
}
