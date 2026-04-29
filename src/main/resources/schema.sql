CREATE SEQUENCE IF NOT EXISTS equipos_seq   START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE IF NOT EXISTS jugadores_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE IF NOT EXISTS users (
    id          BIGINT       AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(50)  NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    role        VARCHAR(20),
    nombre      VARCHAR(100),
    apellidos   VARCHAR(150),
    email       VARCHAR(150) UNIQUE,
    created_at  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS equipos (
    id          BIGINT        PRIMARY KEY,
    nombre      VARCHAR(150)  NOT NULL,
    temporada   VARCHAR(20),
    liga        VARCHAR(150),
    descripcion VARCHAR(500),
    created_at  TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    user_id     BIGINT        NOT NULL,
    CONSTRAINT fk_equipo_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS jugadores (
    id                BIGINT         DEFAULT NEXT VALUE FOR jugadores_seq PRIMARY KEY,
    nombre            VARCHAR(150)   NOT NULL,
    posicion          VARCHAR(50),
    dorsal            INT,
    edad              INT,
    total_goals       INT            DEFAULT 0,
    partidos_jugados  INT            DEFAULT 0,
    gol_por_partido   DOUBLE         DEFAULT 0,
    foto_url          VARCHAR(500),
    foto_convertida   BOOLEAN        DEFAULT FALSE,
    equipo_id         BIGINT         NOT NULL,
    CONSTRAINT fk_jugador_equipo FOREIGN KEY (equipo_id) REFERENCES equipos(id)
);

-- TODO: LIGAS
--
-- OBJETIVO: gestionar ligas independientes a las que pertenecen los equipos.
--
-- TABLA: ligas
--   id          BIGINT PK AUTO_INCREMENT
--   nombre      VARCHAR(150) NOT NULL     ej. 'Primera División'
--   pais        VARCHAR(100)
--   temporada   VARCHAR(20)               ej. '2024-25'
--   user_id     BIGINT FK → users(id)
--   created_at  TIMESTAMP
--
-- IMPACTO:
--   - Añadir columna liga_id BIGINT FK → ligas(id) en tabla equipos (nullable para retrocompatibilidad)
--   - Reemplazar el campo texto 'liga' actual por la FK
--
-- ARQUITECTURA:
--   Dominio:        Liga
--   Puertos in:     CreateLigaUseCase, GetLigasQuery, UpdateLigaUseCase
--   Puertos out:    LigaRepository
--   Controlador:    LigaController → /api/ligas
--
-- FRONTEND:
--   - Pantalla de gestión de ligas (crear, editar, listar)
--   - Selector de liga al crear/editar equipo
--   - Vista de clasificación por liga

-- TODO: PARTIDOS DE LIGA
--
-- OBJETIVO: registrar los partidos oficiales de una jornada de liga, vinculados a una liga concreta.
--
-- TABLA: jornadas
--   id          BIGINT PK AUTO_INCREMENT
--   liga_id     BIGINT FK → ligas(id)
--   numero      INT NOT NULL              número de jornada
--   fecha_ini   DATE
--   fecha_fin   DATE
--
-- TABLA: partidos_liga
--   id              BIGINT PK AUTO_INCREMENT
--   jornada_id      BIGINT FK → jornadas(id)
--   equipo_local    BIGINT FK → equipos(id)
--   equipo_visitante BIGINT FK → equipos(id)
--   goles_local     INT DEFAULT 0
--   goles_visitante INT DEFAULT 0
--   jugado          BOOLEAN DEFAULT FALSE
--   fecha           TIMESTAMP
--
-- ARQUITECTURA:
--   Dominio:        Jornada, PartidoLiga
--   Puertos in:     RegistrarPartidoLigaUseCase, GetJornadasQuery, GetClasificacionQuery
--   Puertos out:    PartidoLigaRepository, JornadaRepository
--   Controlador:    PartidoLigaController → /api/ligas/{id}/partidos
--
-- FRONTEND:
--   - Calendario de jornadas por liga
--   - Formulario de resultado (marcar partido como jugado + goles)
--   - Clasificación en tiempo real calculada desde partidos_liga

-- TODO: EXPORT DE LIGAS
--
-- OBJETIVO: exportar los datos de una liga (clasificación, goleadores, partidos) a Excel o PDF.
--
-- ARQUITECTURA:
--   - Reutilizar Apache POI (ya en dependencias) para Excel
--   - Añadir dependencia iText o OpenPDF para PDF
--   Puertos in:     ExportLigaUseCase
--   Puertos out:    LigaExportPort
--   Controlador:    LigaExportController → GET /api/ligas/{id}/export?format=xlsx|pdf
--
-- FRONTEND:
--   - Botón "Exportar" en la pantalla de liga con selector de formato
--   - Descarga directa via blob + URL.createObjectURL

-- TODO: TRACK DE PARTIDOS (seguimiento en directo)
--
-- OBJETIVO: permitir registrar eventos de un partido en tiempo real desde el móvil o tablet.
--
-- EVENTOS A TRACKEAR:
--   gol             jugador_id, minuto
--   tarjeta         jugador_id, tipo ('amarilla'|'roja'), minuto
--   sustitución     jugador_sale, jugador_entra, minuto
--   inicio/fin      tipo ('inicio_1t'|'fin_1t'|'inicio_2t'|'fin_2t')
--
-- TABLA: eventos_partido
--   id          BIGINT PK AUTO_INCREMENT
--   partido_id  BIGINT FK → partidos_liga(id)
--   jugador_id  BIGINT FK → jugadores(id) NULLABLE
--   tipo        VARCHAR(30)
--   minuto      INT
--   detalle     VARCHAR(100)
--   created_at  TIMESTAMP
--
-- ARQUITECTURA:
--   Dominio:        EventoPartido
--   Puertos in:     RegistrarEventoUseCase, GetEventosPartidoQuery
--   Puertos out:    EventoPartidoRepository
--   Controlador:    EventoPartidoController → /api/partidos/{id}/eventos
--   Opcional:       WebSocket para actualización en tiempo real en el frontend
--
-- FRONTEND:
--   - Vista "modo árbitro": pantalla simplificada con botones grandes (Gol, Tarjeta, etc.)
--   - Timeline del partido con los eventos ordenados por minuto
--   - Resumen automático al finalizar que actualiza estadísticas de jugadores

-- TODO: ASISTENCIA A PARTIDOS
--
-- OBJETIVO: registrar qué jugadores asistieron a cada partido y sus estadísticas individuales en ese partido.
--
-- TABLA: partidos
--   id            BIGINT PK AUTO_INCREMENT
--   equipo_id     BIGINT FK → equipos(id)
--   rival         VARCHAR(150)        nombre del equipo rival
--   fecha         DATE                fecha del partido
--   lugar         VARCHAR(50)         'local' | 'visitante'
--   resultado     VARCHAR(10)         ej. '2-1'
--   goles_favor   INT DEFAULT 0
--   goles_contra  INT DEFAULT 0
--   created_at    TIMESTAMP
--
-- TABLA: asistencias
--   id              BIGINT PK AUTO_INCREMENT
--   partido_id      BIGINT FK → partidos(id)
--   jugador_id      BIGINT FK → jugadores(id)
--   asistio         BOOLEAN DEFAULT TRUE    (convocado y jugó)
--   goles           INT DEFAULT 0           goles marcados en ese partido
--   minutos         INT DEFAULT 0           minutos jugados
--   titular         BOOLEAN DEFAULT FALSE
--
-- IMPACTO EN jugadores:
--   Al registrar una asistencia, recalcular y actualizar:
--     total_goals      += asistencias.goles
--     partidos_jugados += 1 (si asistio = true)
--     gol_por_partido   = total_goals / partidos_jugados
--
-- ARQUITECTURA:
--   Dominio:        Partido, Asistencia
--   Puertos in:     RegistrarPartidoUseCase, RegistrarAsistenciaUseCase, GetPartidosQuery
--   Puertos out:    PartidoRepository, AsistenciaRepository
--   Controladores:  PartidoController  → /api/equipos/{id}/partidos
--                   AsistenciaController → /api/partidos/{id}/asistencias
--
-- FRONTEND:
--   - pantalla de partidos por equipo (lista + botón nuevo partido)
--   - formulario de nuevo partido con selector de jugadores y asistencia
--   - al marcar asistencia: checkbox por jugador + campo de goles + minutos
--   - actualizar estadísticas en estadisticas.html automáticamente
