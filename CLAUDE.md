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

**Prerequisites:** Java 21+, MySQL running on `localhost:3306` with database `mydb`. Credentials default to `root`/`root` (override via `DB_USERNAME` / `DB_PASSWORD` env vars).

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

`src/main/resources/application.yml` — main config. Two profiles:
- **default:** `ddl-auto: validate`, SQL logging off
- **local:** `ddl-auto: update`, SQL logging on

## Frontend

Static assets served by Spring Boot from `src/main/resources/static/`:
- **`index.html`** — dashboard template
- **`css/main.css`** — glassmorphism design system (CSS variables: `--pu`, `--bl`, `--pk`, `--gr`, `--am`, `--re`)
- **`js/main.js`** — utilities (`apiFetch`, `renderTable`, `createBarChart`, `createLineChart`, `updateKpi`)

All API calls go through `apiFetch(path)` which prefixes `/api`.

## Code style

Do not add Javadoc or inline comments to model classes, DTOs, enums, or records. Keep them clean — the field names are self-explanatory.

## Testing

JUnit 5 + Mockito via `spring-boot-starter-test`. H2 in-memory DB is available for test scope — create `src/test/resources/application-test.yml` to override datasource when needed. Test directory structure mirrors `src/main/java/com/project/`.
