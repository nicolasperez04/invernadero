# AGENTS.md - SIGMA Project

## Project Overview
- **Type**: Spring Boot 3.5.8 REST API (Java 21)
- **Purpose**: Greenhouse management system (SIGMA)
- **Package**: `com.invernadero.proyecto`

## Run Commands

```powershell
# Build and run (Windows)
.\mvnw.cmd spring-boot:run

# Run tests
.\mvnw.cmd test

# Run single test class
.\mvnw.cmd test -Dtest=UserServiceTest

# Package as JAR
.\mvnw.cmd clean package -DskipTests
```

## Database
- **Type**: PostgreSQL
- **URL**: `jdbc:postgresql://localhost:5432/invernadero`
- **Credentials**: admin/admin
- **Auto-migration**: `spring.jpa.hibernate.ddl-auto=update`

## API Access
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- API Docs: `http://localhost:8080/api-docs`

## Architecture
```
src/main/java/com/invernadero/proyecto/
├── controller/    # REST endpoints (Auth, Crop, Dashboard, Event, EventType, Lot, User)
├── Service/      # Business logic
├── Entity/        # JPA entities
├── Dto/           # Data transfer objects
├── Repository/    # Spring Data JPA repositories
├── Security/      # JWT config, SecurityConfig
├── mapper/        # MapStruct/DTO mappers
├── Exception/     # GlobalExceptionHandler
└── ProyectoApplication.java  # Entry point
```

## Key Config
- JWT secret in `application.properties`
- Timezone: UTC (DB), America/Bogota (API)
- I18n: `messages.properties`, `messages_es.properties`

## Notes
- Uses Lombok - ensure annotation processing enabled in IDE
- Java 21 with `--enable-preview` flag
- Tests use Spring Boot Test with MockMvc