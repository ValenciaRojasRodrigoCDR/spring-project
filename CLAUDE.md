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

`DataInitializer` seeds a default `admin` user on first startup — password from `ADMIN_PASSWORD` env var (default `changeme`, see `app.admin.password` in `application.yml`).

## Security

Stateless JWT auth via `JwtAuthFilter`. Public routes: `/api/auth/**`, `/api/jugadores/*/foto`, `/h2-console/**`, and all static HTML/CSS/JS assets. Everything else requires a valid Bearer token.

## Excel feature

`ExcelParserAdapter` parses `.xlsx` files using Apache POI. The expected format is a fixed-layout sheet: players in rows 3–18 (0-based 2–17), jornada columns in two blocks (B–L for J1–J11, N–X for J12–J22, column M is a separator).
## Frontend

Static assets served by Spring Boot from `src/main/resources/static/`:
- Pages: `login.html`, `index.html` (dashboard), `club.html`, `jugadores.html`, `estadisticas.html`, `import-club.html`, `profile.html`
- **`css/main.css`** — glassmorphism design system (CSS variables: `--pu`, `--bl`, `--pk`, `--gr`, `--am`, `--re`)
- **`js/main.js`** — utilities (`apiFetch`, `renderTable`, `createBarChart`, `createLineChart`, `updateKpi`)

All API calls go through `apiFetch(path)` which prefixes `/api`.

## Optimización — consigna principal del proyecto

> La optimización es la prioridad transversal. Lema: buen diseño = mejores tiempos de respuesta.

### Herramientas de Java a aprovechar
- **record** para DTOs y value objects (inmutables, sin boilerplate).
- **Lambdas + Streams** para transformar/filtrar colecciones de forma declarativa.
- **Pattern matching / switch expressions** para ramas por tipo o enum.
- **Optional** para ausencia de valor sin nulls dispersos.

### Reglas prácticas
- Filtrar en la BBDD, no en Java: consultas derivadas o `@Query` con WHERE. Nunca traer toda la tabla para filtrar en memoria.
- Una query agregada (`GROUP BY`, `IN (...)`) antes que N queries en bucle.
- Escrituras en lote: `saveAll` / `batchUpdate`, nunca `save` en bucle. No usar `saveAndFlush` (anula el batching de Hibernate configurado en `application.yml`).
- `existsById` para comprobar existencia; no cargar la entidad entera para tirarla.
- Deletes en bloque con `@Modifying @Query`, no derivados (que hacen SELECT + delete fila a fila).
- Operaciones multi-paso de escritura siempre con `@Transactional`.
- Paginar/limitar consultas grandes antes de que sean un problema.
- Medir antes de microoptimizar; preferir claridad cuando la ganancia es nula.

### Antipatrones a evitar
- N+1 queries en JPA (una query por elemento de una lista).
- Trabajo repetido por petición (p. ej. verificar el JWT más de una vez, o resolver el userId con una query cuando puede viajar en el token).
- Recalcular en el frontend lo que la API ya devuelve calculado.

## Programación — DRY y limpieza

- Antes de escribir código nuevo en una clase existente: comprueba si ya hay un método (en esa clase o en otra del proyecto) que haga lo mismo. Si existe, reutilízalo.
- EXCEPCIÓN: al crear una clase nueva todo su código es nuevo por definición; la búsqueda de duplicados aplica al AÑADIR código a lo que ya existe.
- Lógica repetida entre controllers/servicios → extráela a un sitio común.
- Métodos cortos, una responsabilidad. Si un método crece, extrae sub-métodos privados.
- Clases acotadas, nada de "cajón de sastre".
- Sin código muerto ni imports sin usar.

### Checklist antes de dar por hecho un cambio
1. ¿He reutilizado lo existente en lugar de duplicar?
2. ¿Cada clase está en la carpeta correcta según la arquitectura hexagonal?
3. ¿Métodos cortos y con una sola responsabilidad?
4. ¿He generado/actualizado los tests?

## Code style

Do not add Javadoc or inline comments to model classes, DTOs, enums, or records. Keep them clean — the field names are self-explanatory.

## Testing

JUnit 5 + Mockito via `spring-boot-starter-test`. H2 in-memory DB is the default datasource — create `src/test/resources/application-test.yml` to override if needed. Test directory structure mirrors `src/main/java/com/project/`.

### Reglas
- En CADA prompt en el que se añade/modifica código, se generan o actualizan sus tests.
- El test de una clase va en el mismo paquete que la clase, bajo `src/test/java` (espejo exacto de `src/main`).

### Qué testear por capa
| Capa | Tipo de test | Herramientas |
|---|---|---|
| application/service | Unitario del service | JUnit 5 + Mockito (mock del Port) |
| infrastructure/adapter/in/web | Controller (slice) | `@WebMvcTest` + MockMvc |
| infrastructure/adapter/out/persistence | Adaptador / repo | `@DataJpaTest` + `@Import(adaptador)` |
| arranque | Carga de contexto | `@SpringBootTest` (1 test global, BBDD en memoria) |

### Convenciones
- Nombre del método: `debe…Cuando…`.
- Patrón Arrange / Act / Assert.
- Un test = una conducta. Sin lógica compleja dentro del test.
- Para servicios: mockear el `Repository` (puerto) y verificar interacción + resultado.

## Memoria

`.claude/memoria.md` tiene dos propósitos únicos, y toda escritura requiere aceptación explícita del usuario:
1. **Aprendizajes de errores** — cuando se comete un error (build roto, mala decisión), anotar la lección para no repetirla.
2. **Resúmenes de sesión** — al final de una sesión, cuando el usuario lo pida.

## Workflow after every code change

After **any** code change, always run this sequence before reporting the task as done:
1. `mvn test` — verify all tests pass
2. `mvn clean install -DskipTests` — build the JAR
3. Kill the running server process
4. `mvn spring-boot:run -Dspring.profiles.active=local` — restart the server

The user only needs to press F5 to see changes. Never skip the test step.
