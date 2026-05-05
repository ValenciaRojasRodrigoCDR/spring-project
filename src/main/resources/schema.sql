CREATE SEQUENCE IF NOT EXISTS equipos_seq   START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE IF NOT EXISTS jugadores_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE IF NOT EXISTS users (
    id          BIGINT       AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(50)  NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    role        VARCHAR(30),
    nombre      VARCHAR(100),
    apellidos   VARCHAR(150),
    email       VARCHAR(150) UNIQUE,
    created_at  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    jugador_id  BIGINT       NULL
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

CREATE TABLE IF NOT EXISTS ligas (
    id          BIGINT       AUTO_INCREMENT PRIMARY KEY,
    nombre      VARCHAR(150) NOT NULL,
    temporada   VARCHAR(20),
    descripcion VARCHAR(500),
    created_at  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    user_id     BIGINT       NOT NULL,
    CONSTRAINT fk_liga_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS liga_equipo (
    liga_id   BIGINT NOT NULL,
    equipo_id BIGINT NOT NULL,
    PRIMARY KEY (liga_id, equipo_id),
    CONSTRAINT fk_le_liga   FOREIGN KEY (liga_id)   REFERENCES ligas(id)   ON DELETE CASCADE,
    CONSTRAINT fk_le_equipo FOREIGN KEY (equipo_id) REFERENCES equipos(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS partidos (
    id           BIGINT       AUTO_INCREMENT PRIMARY KEY,
    liga_id      BIGINT,
    equipo_id    BIGINT       NOT NULL,
    rival        VARCHAR(150) NOT NULL,
    fecha        DATE,
    lugar        VARCHAR(50),
    resultado    VARCHAR(10),
    goles_favor  INT          DEFAULT 0,
    goles_contra INT          DEFAULT 0,
    created_at   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_partido_liga   FOREIGN KEY (liga_id)   REFERENCES ligas(id)   ON DELETE SET NULL,
    CONSTRAINT fk_partido_equipo FOREIGN KEY (equipo_id) REFERENCES equipos(id)
);

CREATE TABLE IF NOT EXISTS asistencias (
    id         BIGINT  AUTO_INCREMENT PRIMARY KEY,
    partido_id BIGINT  NOT NULL,
    jugador_id BIGINT  NOT NULL,
    asistio    BOOLEAN DEFAULT TRUE,
    goles      INT     DEFAULT 0,
    minutos    INT     DEFAULT 0,
    titular    BOOLEAN DEFAULT FALSE,
    CONSTRAINT fk_asist_partido FOREIGN KEY (partido_id) REFERENCES partidos(id) ON DELETE CASCADE,
    CONSTRAINT fk_asist_jugador FOREIGN KEY (jugador_id) REFERENCES jugadores(id)
);
