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
    id          BIGINT        AUTO_INCREMENT PRIMARY KEY,
    nombre      VARCHAR(150)  NOT NULL,
    temporada   VARCHAR(20),
    liga        VARCHAR(150),
    descripcion VARCHAR(500),
    created_at  TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    user_id     BIGINT        NOT NULL,
    CONSTRAINT fk_equipo_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS jugadores (
    id                BIGINT         AUTO_INCREMENT PRIMARY KEY,
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
