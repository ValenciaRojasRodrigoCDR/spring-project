# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run Commands

```bash
# Build
mvn clean install

# Run (default profile)
mvn spring-boot:run

# Run with local profile (enables SQL logging, ddl-auto: update)
mvn -Dspring.profiles.active=local spring-boot:run

# Run tests
mvn test

# Run a single test class
mvn test -Dtest=ClassName

# Package JAR
mvn clean package
java -jar target/my-app-0.0.1-SNAPSHOT.jar
```

**Prerequisites:** Java 20+. The default profile uses an **H2 in-memory database** — no external DB needed to run. MySQL connector is included as a runtime dependency for production use.

## Architecture

This project uses **Hexagonal Architecture (Ports & Adapters)**:

```
com.project/
├── domain/            # Core business logic — no framework dependencies
│   ├── model/         # Entities and value objects
│   └── exception/     # Domain-specific exceptions
├── application/       # Use cases and orchestration
│   ├── port/
│   │   ├── in/        # Input port interfaces (*UseCase, *Query)
│   │   └── out/       # Output port interfaces (*Port, *Repository)
│   └── service/       # Service implementations
└── infrastructure/    # Spring wiring and adapters
    ├── adapter/
    │   ├── in/web/    # REST controllers (@RestController)
    │   └── out/persistence/ # JPA repository implementations
    └── config/        # @Configuration beans
```

**Naming conventions:**
- Input ports: `*UseCase`, `*Query`
- Output ports: `*Port`, `*Repository`
- DTOs in: `*Request`, `*Command` — DTOs out: `*Response`, `*Dto`
- Mappers: `*Mapper`

## Key Configuration

`src/main/resources/application.yml` — main config. App runs on **port 3000**.

- **default:** H2 in-memory DB (`jdbc:h2:mem:mydb`), schema initialized from `src/main/resources/schema.sql`, SQL logging off. H2 console available at `/h2-console`.
- **local:** Same as default but with `show-sql: true`.

JWT secret and expiration are configured under `app.jwt`. Uploaded player photos go to `uploads/jugadores/` (configurable via `app.upload-dir`).

`DataInitializer` seeds a default `admin`/`admin` user on first startup.

## Security

Stateless JWT auth via `JwtAuthFilter`. Public routes: `/api/auth/**`, `/api/jugadores/*/foto`, `/h2-console/**`, and all static HTML/CSS/JS assets. Everything else requires a valid Bearer token.

## Excel feature

`ExcelParserAdapter` parses `.xlsx` files using Apache POI. The expected format is a fixed-layout sheet: players in rows 3–18 (0-based 2–17), jornada columns in two blocks (B–L for J1–J11, N–X for J12–J22, column M is a separator). An example file is at `src/main/resources/examples/4M BIG DATA.xlsx`.

## Frontend

Static assets served by Spring Boot from `src/main/resources/static/`:
- Pages: `login.html`, `index.html` (dashboard), `club.html`, `jugadores.html`, `estadisticas.html`, `import-club.html`, `profile.html`
- **`css/main.css`** — glassmorphism design system (CSS variables: `--pu`, `--bl`, `--pk`, `--gr`, `--am`, `--re`)
- **`js/main.js`** — utilities (`apiFetch`, `renderTable`, `createBarChart`, `createLineChart`, `updateKpi`)

All API calls go through `apiFetch(path)` which prefixes `/api`.

## Code style

Do not add Javadoc or inline comments to model classes, DTOs, enums, or records. Keep them clean — the field names are self-explanatory.

## Testing

JUnit 5 + Mockito via `spring-boot-starter-test`. H2 in-memory DB is the default datasource — create `src/test/resources/application-test.yml` to override if needed. Test directory structure mirrors `src/main/java/com/project/`.
