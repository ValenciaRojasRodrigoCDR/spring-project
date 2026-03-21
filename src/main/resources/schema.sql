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
    total_goals       INT            DEFAULT 0,
    partidos_jugados  INT            DEFAULT 0,
    gol_por_partido   DOUBLE         DEFAULT 0,
    equipo_id         BIGINT         NOT NULL,
    CONSTRAINT fk_jugador_equipo FOREIGN KEY (equipo_id) REFERENCES equipos(id)
);
