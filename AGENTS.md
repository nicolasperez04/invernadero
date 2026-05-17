# SIGMA - Backend (Spring Boot)

Spring Boot 3.5.8 REST API | Java 21 | PostgreSQL | `com.invernadero.proyecto`

## Commands

```powershell
.\mvnw.cmd spring-boot:run            # Dev server (port 8080)
.\mvnw.cmd test                        # All tests
.\mvnw.cmd test -Dtest=UserServiceTest # Single test
.\mvnw.cmd verify -q                   # Tests + JaCoCo coverage check
.\mvnw.cmd clean package -DskipTests   # JAR
```

## Key Config

- `--enable-preview` flag is required (pom.xml + Dockerfile)
- Spring profiles: `dev` (PostgreSQL, show-sql), `prod` (DB_URL env), `test` (H2 in-memory)
- `.env` at root loaded via `spring-dotenv` — copy from `.env.example`
- JWT secret in `JWT_SECRET` env var (HS256, min 64 chars)
- Timezone: UTC (DB) / America/Bogota (Jackson)
- i18n: `messages.properties` (en), `messages_es.properties` (es)

## API Surface

| Path | Description |
|------|-------------|
| `POST /api/auth/login` | Public |
| `POST /api/auth/register` | Public |
| `GET /api/health` | Public |
| `GET/POST/PUT/DELETE /api/crops` | CRUD |
| `GET/POST/PUT/DELETE /api/lots` | CRUD + `/lots/{id}/report` (PDF) |
| `GET/POST/PUT/DELETE /api/events` | CRUD + filter/search endpoints |
| `GET /api/event-types` | Read-only via REST (CRUD through crop-event-types) |
| `GET /api/dashboard` | Metrics |
| `GET/PUT /api/notifications` | Notifications + SSE push |
| `GET /api/users` | Admin-only CRUD |
| `GET /api/sse/subscribe?token=<jwt>` | Server-Sent Events (token as query param) |

- Swagger: `http://localhost:8080/swagger-ui.html`
- OpenAPI: `http://localhost:8080/api-docs`

## Architecture

```
src/main/java/com/invernadero/proyecto/
├── Config/        # CORS, OpenAPI, Locale, HealthController
├── controller/    # REST endpoints
├── Service/       # Business logic
├── Entity/        # JPA entities + enums
├── Dto/           # Request + Response DTOs
├── Repository/    # Spring Data JPA
├── Security/      # JWT filter, JwtService, SecurityConfig
├── mapper/        # MapStruct (Entity <-> DTO)
├── Exception/     # GlobalExceptionHandler
└── ProyectoApplication.java  # @EnableScheduling
```

## Database

- PostgreSQL with `spring.jpa.hibernate.ddl-auto=update`
- Roles: ADMIN (full), OPERATOR (manage crops/lots/events), VIEWER (read-only)

## Testing

- Tests use H2 in-memory (`spring.jpa.hibernate.ddl-auto=create-drop`)
- JaCoCo enforced: 80% line / 60% branch coverage (Config and Application excluded)
- Excludes: `com/invernadero/proyecto/Config/**` and `ProyectoApplication`

## CI (GitHub Actions)

Runs on every branch push and PR to `main`: compile → `verify` (tests + JaCoCo) → package → upload JAR + coverage artifacts. Coverage comment posted on PRs.

## Docker

Multi-stage build (eclipse-temurin:21-jdk build → 21-jre runtime). Port 8080. Healthcheck at `/api/health`. Non-root user. `--enable-preview` in entrypoint.

## Notable Dependencies

- **JWT**: jjwt 0.12.6 (api + impl + jackson)
- **PDF**: iTextPDF 5.5.13.3 (lot report download)
- **Excel**: Apache POI 5.4.0
- **OCR**: Tess4J 5.8.0 + PDFBox 2.0.30
- **Doc gen**: `doc/generate.py` — standalone Python script (zero deps) that generates FastAPI/Express backends from `doc/JSON.md` meta-model
