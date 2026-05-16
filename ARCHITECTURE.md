# Arquitectura de SIGMA

## Visión General

SIGMA sigue una **arquitectura multinivel (layered architecture)** estándar de Spring Boot:

```
┌─────────────────────────────────────────────┐
│           PRESENTATION LAYER                │
│              Controllers                     │
│   (Auth, Crop, Lot, Event, User, etc.)       │
└──────────────────┬──────────────────────────┘
                   │ HTTP + DTOs Request
                   ▼
┌─────────────────────────────────────────────┐
│            BUSINESS LAYER                   │
│              Services                        │
│  (AuthService, CropService, LotService...)  │
└──────────────────┬──────────────────────────┘
                   │ Entity objects
                   ▼
┌─────────────────────────────────────────────┐
│            DATA ACCESS LAYER                │
│         Repositories (JPA)                   │
│  (UserRepository, CropRepository, etc.)     │
└──────────────────┬──────────────────────────┘
                   │ JPA/Hibernate
                   ▼
┌─────────────────────────────────────────────┐
│         PERSISTENCE LAYER                   │
│              PostgreSQL                      │
└─────────────────────────────────────────────┘
```

Capas adicionales:
- **Security Layer**: JWT filter → SecurityConfig → Authentication
- **DTO Mapping**: Controllers usan MapStruct para convertir Entity ↔ DTO

## Modelo de Datos (Entidad-Relación)

```
┌──────────┐        ┌──────────┐        ┌────────────┐
│   User   │1      M│  Event   │M      1│  EventType  │
│ (users)  │────────│ (events) │────────│(event_types)│
└──────────┘        └────┬─────┘        └────────────┘
                         │ M
                         │
                         ▼
                    ┌──────────┐  1     ┌──────────┐
                    │   Lot    │───────<│   Crop    │
                    │  (lots)  │        │ (crops)   │
                    └──────────┘        └──────────┘
```

### Detalle de relaciones

| Entidad    | Relación  | Entidad      | Tipo          |
|------------|-----------|--------------|---------------|
| User       | 1 → M     | Event        | Un usuario registra muchos eventos |
| Event      | M → 1     | Lot          | Cada evento pertenece a un lote |
| Event      | M → 1     | EventType    | Cada evento tiene un tipo |
| Event      | M → 1     | User         | Cada evento es registrado por un usuario |
| Lot        | M → 1     | Crop         | Cada lote pertenece a un cultivo |
| Crop       | 1 → M     | Lot          | Un cultivo tiene muchos lotes |

## Modelo de Dominio

### User
Campos: `id`, `name`, `lastName`, `email`, `password`, `role` (ADMIN/OPERATOR/VIEWER), `active`

### Crop (Cultivo)
Campos: `id`, `name`, `description`, `inactivityDaysThreshold`, `estimatedGrowthDays`

### Lot (Lote)
Campos: `id`, `name`, `crop` (FK), `startDate`, `endDate`, `estimatedHarvestDate`

### Event (Evento)
Campos: `id`, `lot` (FK), `type` (FK), `user` (FK), `timestamp`, `description`, `createdAt`

### EventType
Campos: `id`, `name`, `category`

## Seguridad

### Flujo de Autenticación JWT

```
1. Cliente → POST /api/auth/login { email, password }
2. Server: JwtService genera token (validez: 5 horas)
3. Server → { token: "eyJ..." }
4. Cliente → any endpoint (Header: Authorization: Bearer <token>)
5. Server: JwtAuthenticationFilter valida token
6. Server → Recurso o 401/403
```

### Configuración de Seguridad

- **Autenticación**: Stateless (sin sesiones en servidor)
- **Contraseña**: BCrypt encoding
- **CSRF**: Deshabilitado (API stateless)
- **CORS**: Permite `http://localhost:4200` y `${frontend.url}`

### Endpoints Públicos

```
/api/auth/**                  # Login y registro
/swagger-ui/**                # Documentación
/swagger-ui.html
/v3/api-docs/**               # OpenAPI spec
/api-docs/**
/api/health/**
```

## Perfiles de Spring Boot

### dev (desarrollo)
```properties
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.jdbc.time_zone=UTC
spring.jackson.time-zone=America/Bogota
```

### prod (producción)
```properties
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
```

### test (pruebas)
```properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=create-drop
# Base de datos H2 en memoria, no PostgreSQL
```

## Decisiones Técnicas Clave

| Decisión                       | Razón                                              |
|--------------------------------|----------------------------------------------------|
| JWT stateless                  | Escalabilidad horizontal sin estado de sesión      |
| Lombok                         | Reduce boilerplate en entidades y DTOs             |
| MapStruct                      | Mapping tipo-safe entre Entity y DTO                |
| DTOs separados (Request/Response) | Oculta estructura interna, versionado de API    |
| `update` en dev, `validate` en prod | Seguridad en producción, flexibilidad en dev |
| Timezone: UTC en BD, Bogota en API | Consistencia DB, legibilidad según zona horaria |
| H2 en tests                    | Aislamiento, velocidad, no requiere PostgreSQL     |

## Convenciones de Código

- **Paquetes**: lowercase (ej: `com.invernadero.proyecto`)
- **Clases**: PascalCase (ej: `CropController`, `UserService`)
- **DTOs Request**: suffix `Request` (ej: `CropRequest`)
- **DTOs Response**: suffix `Response` (ej: `LotResponse`)
- **Entidades JPA**: nombre de tabla en plural (ej: `@Table(name = "crops")`)
- **Errores**: clase `ApiError` con campos `status`, `error`, `message`, `timestamp`

## Internacionalización (i18n)

Archivos de mensajes:
- `messages.properties` → inglés (por defecto)
- `messages_es.properties` → español

## API Docs (Swagger/OpenAPI)

La documentación está completamente anotada con:
- `@Tag` en controllers
- `@Operation` y `@ApiResponse` en endpoints
- `@Schema` en DTOs y entidades (con `description` y `example`)
- Esquema de seguridad JWT

Acceso: `http://localhost:8080/swagger-ui.html`