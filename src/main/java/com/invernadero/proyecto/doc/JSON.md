# SIGMA Meta-Model JSON

> Documento auto-descriptivo del proyecto **SIGMA** (Sistema de Gestión de Invernaderos).
>
> El JSON central de este documento constituye un **meta-modelo completo** del proyecto Spring Boot.
> Su propósito es servir como fuente única de verdad desde la cual se pueda **regenerar, migrar o reconstruir**
> el proyecto en cualquier lenguaje, framework o plataforma mediante automatización (generación de código,
> schemas de base de datos, documentación, clientes API, pruebas, etc.).

---

## Índice

1. [Meta-Modelo JSON](#meta-modelo-json)
2. [Guía de Uso](#guía-de-uso)
   - [Cómo usar este meta-modelo para migrar a otro stack](#cómo-usar-este-meta-modelo-para-migrar-a-otro-stack)
   - [Ejemplos de generación automatizada](#ejemplos-de-generación-automatizada)
3. [Diagrama de Entidades y Relaciones](#diagrama-de-entidades-y-relaciones)
4. [Glosario de Tipos](#glosario-de-tipos)
5. [Reglas de Negocio Detalladas](#reglas-de-negocio-detalladas)

---

## Meta-Modelo JSON

```json
{
  "_metadata": {
    "title": "SIGMA - Sistema de Gestión de Invernaderos",
    "description": "Meta-modelo auto-descriptivo del proyecto. Diseñado para regeneración automatizada en cualquier lenguaje/framework.",
    "version": "1.0.0",
    "generatedAt": "2026-05-15",
    "sourceProject": {
      "name": "SIGMA",
      "version": "0.0.1-SNAPSHOT",
      "description": "Sistema de Gestión de Invernaderos - API REST con autenticación JWT para administrar cultivos, lotes, eventos y usuarios.",
      "groupId": "com.SIGMA",
      "artifactId": "SIGMA",
      "package": "com.invernadero.proyecto"
    },
    "targetFrameworks": ["Spring Boot 3.5.8", "Java 21"],
    "buildTool": "Maven",
    "javaFlags": ["--enable-preview"],
    "repository": {
      "type": "git",
      "url": null
    }
  },

  "languages": {
    "programming": {
      "primary": {
        "name": "Java",
        "version": "21",
        "paradigm": "object-oriented"
      },
      "jvm": {
        "name": "OpenJDK",
        "features": ["records", "pattern-matching", "sealed-classes"]
      }
    },
    "markup": {
      "template": null,
      "documentation": "Markdown"
    },
    "query": {
      "data": "JPQL",
      "native": "SQL"
    },
    "i18n": {
      "basename": "messages",
      "encoding": "UTF-8",
      "fallbackToSystemLocale": false,
      "locales": [
        {
          "code": "en",
          "file": "messages.properties",
          "label": "English"
        },
        {
          "code": "es",
          "file": "messages_es.properties",
          "label": "Spanish"
        }
      ],
      "keys": [
        { "key": "error.validation.title", "en": "Validation Error", "es": "Error de validacion" },
        { "key": "error.validation.message", "en": "Invalid fields", "es": "Campos invalidos" },
        { "key": "error.constraint.title", "en": "Constraint Violation", "es": "Violacion de restricciones" },
        { "key": "error.constraint.message", "en": "Invalid parameters", "es": "Parametros invalidos" },
        { "key": "error.unauthorized.title", "en": "Unauthorized", "es": "No autorizado" },
        { "key": "error.unauthorized.message", "en": "Invalid credentials", "es": "Credenciales incorrectas" },
        { "key": "error.forbidden.title", "en": "Forbidden", "es": "Prohibido" },
        { "key": "error.forbidden.message", "en": "You do not have permission to access this resource", "es": "No tienes permisos para acceder a este recurso" },
        { "key": "error.business.title", "en": "Business Error", "es": "Error de negocio" },
        { "key": "error.internal.title", "en": "Internal Server Error", "es": "Error interno" },
        { "key": "error.internal.message", "en": "Unexpected error", "es": "Error inesperado" },
        { "key": "validation.crop.name.required", "en": "Crop name is required", "es": "El nombre del cultivo es obligatorio" },
        { "key": "validation.lot.name.required", "en": "Lot name is required", "es": "El nombre del lote es obligatorio" },
        { "key": "validation.lot.cropId.required", "en": "Crop id is required", "es": "El ID del cultivo es obligatorio" },
        { "key": "validation.lot.startDate.required", "en": "Start date is required", "es": "La fecha de inicio es obligatoria" },
        { "key": "validation.user.name.required", "en": "Name is required", "es": "El nombre es obligatorio" },
        { "key": "validation.user.lastName.required", "en": "Last name is required", "es": "El apellido es obligatorio" },
        { "key": "validation.user.email.required", "en": "Email is required", "es": "El correo es obligatorio" },
        { "key": "validation.user.email.invalid", "en": "Email is invalid", "es": "El correo no es valido" },
        { "key": "validation.user.password.required", "en": "Password is required", "es": "La contrasena es obligatoria" },
        { "key": "validation.user.role.required", "en": "Role is required", "es": "El rol es obligatorio" },
        { "key": "validation.event.lotId.required", "en": "Lot id is required", "es": "El ID del lote es obligatorio" },
        { "key": "validation.event.type.required", "en": "Event type is required", "es": "El tipo de evento es obligatorio" },
        { "key": "validation.event.userId.required", "en": "User id is required", "es": "El ID del usuario es obligatorio" },
        { "key": "validation.event.timestamp.required", "en": "Timestamp is required", "es": "La fecha del evento es obligatoria" },
        { "key": "validation.event.description.required", "en": "Description is required", "es": "La descripcion es obligatoria" }
      ]
    }
  },

  "database": {
    "vendor": "PostgreSQL",
    "dialect": "org.hibernate.dialect.PostgreSQLDialect",
    "connectionPool": {
      "provider": "HikariCP",
      "config": {
        "initializationFailTimeout": 60000,
        "connectionTimeout": 60000
      }
    },
    "ddlAuto": "update",
    "timezone": {
      "storage": "UTC",
      "application": "America/Bogota"
    },
    "profiles": {
      "dev": {
        "urlTemplate": "jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}",
        "usernameEnv": "DB_USER",
        "passwordEnv": "DB_PASSWORD"
      },
      "prod": {
        "urlTemplate": "${DB_URL}",
        "usernameEnv": "DB_USER",
        "passwordEnv": "DB_PASSWORD"
      }
    },
    "server": {
      "port": 8080,
      "portEnv": "PORT"
    },
    "tables": [
      {
        "name": "crops",
        "entity": "Crop"
      },
      {
        "name": "lots",
        "entity": "Lot"
      },
      {
        "name": "events",
        "entity": "Event"
      },
      {
        "name": "event_types",
        "entity": "EventType"
      },
      {
        "name": "users",
        "entity": "User"
      }
    ]
  },

  "enums": [
    {
      "name": "Role",
      "package": "com.invernadero.proyecto.Entity.enums",
      "values": ["ADMIN", "OPERATOR", "VIEWER"],
      "usage": "Defines user roles for authorization",
      "usedBy": ["User.role"]
    }
  ],

  "entities": [
    {
      "name": "User",
      "table": "users",
      "description": "System user implementing Spring Security UserDetails",
      "implements": ["org.springframework.security.core.userdetails.UserDetails"],
      "annotations": ["@Entity", "@Data", "@Builder", "@AllArgsConstructor", "@NoArgsConstructor"],
      "fields": [
        { "name": "id", "type": "Long", "javaType": "java.lang.Long", "pk": true, "generated": "IDENTITY", "nullable": false, "unique": false, "swaggerExample": "1" },
        { "name": "name", "type": "String", "javaType": "java.lang.String", "nullable": false, "unique": false, "validate": "@NotNull", "swaggerExample": "Carlos" },
        { "name": "lastName", "type": "String", "javaType": "java.lang.String", "nullable": false, "unique": false, "validate": "@NotNull", "swaggerExample": "García" },
        { "name": "email", "type": "String", "javaType": "java.lang.String", "nullable": false, "unique": true, "validate": "@NotNull", "swaggerExample": "carlos.garcia@ejemplo.com" },
        { "name": "password", "type": "String", "javaType": "java.lang.String", "nullable": false, "unique": false, "validate": "@NotNull", "notes": "Stored encrypted with BCrypt", "swaggerExample": null },
        { "name": "role", "type": "Enum(Role)", "javaType": "com.invernadero.proyecto.Entity.enums.Role", "nullable": false, "unique": false, "columnDefinition": "varchar", "storedAs": "STRING", "swaggerExample": "ADMIN" },
        { "name": "active", "type": "boolean", "javaType": "boolean", "nullable": false, "unique": false, "defaultValue": "true", "swaggerExample": "true" }
      ],
      "relationships": [],
      "audit": []
    },
    {
      "name": "Crop",
      "table": "crops",
      "description": "Crop type registered in the greenhouse system",
      "annotations": ["@Entity", "@Data", "@Builder", "@AllArgsConstructor", "@NoArgsConstructor"],
      "fields": [
        { "name": "id", "type": "Long", "javaType": "java.lang.Long", "pk": true, "generated": "IDENTITY", "nullable": false, "unique": false, "swaggerExample": "1" },
        { "name": "name", "type": "String", "javaType": "java.lang.String", "nullable": false, "unique": false, "swaggerExample": "Tomate cherry" },
        { "name": "description", "type": "String", "javaType": "java.lang.String", "nullable": true, "unique": false, "swaggerExample": "Variedad dulce de tomate para exportación" },
        { "name": "inactivityDaysThreshold", "type": "Integer", "javaType": "java.lang.Integer", "nullable": false, "unique": false, "swaggerExample": "7" },
        { "name": "estimatedGrowthDays", "type": "Integer", "javaType": "java.lang.Integer", "nullable": false, "unique": false, "swaggerExample": "90" }
      ],
      "relationships": [
        {
          "type": "OneToMany",
          "targetEntity": "Lot",
          "mappedBy": "crop",
          "cascade": ["ALL"],
          "orphanRemoval": true,
          "fetch": "LAZY",
          "jsonIgnore": true
        }
      ],
      "audit": []
    },
    {
      "name": "Lot",
      "table": "lots",
      "description": "Cultivation lot in the greenhouse, linked to a crop",
      "annotations": ["@Entity", "@Data", "@Builder", "@NoArgsConstructor", "@AllArgsConstructor"],
      "fields": [
        { "name": "id", "type": "Long", "javaType": "java.lang.Long", "pk": true, "generated": "IDENTITY", "nullable": false, "unique": false, "swaggerExample": "1" },
        { "name": "name", "type": "String", "javaType": "java.lang.String", "nullable": false, "unique": false, "swaggerExample": "Lote Norte A" },
        { "name": "startDate", "type": "Instant", "javaType": "java.time.Instant", "nullable": false, "unique": false, "swaggerExample": "2025-01-15T00:00:00Z" },
        { "name": "endDate", "type": "Instant", "javaType": "java.time.Instant", "nullable": true, "unique": false, "swaggerExample": "2025-04-15T00:00:00Z" },
        { "name": "estimatedHarvestDate", "type": "Instant", "javaType": "java.time.Instant", "nullable": true, "unique": false, "columnName": "estimated_harvest_date", "swaggerExample": "2025-04-15T00:00:00Z" }
      ],
      "relationships": [
        {
          "type": "ManyToOne",
          "targetEntity": "Crop",
          "joinColumn": "crop_id",
          "nullable": false,
          "fetch": "LAZY",
          "jsonIgnoreProperties": ["lots"]
        },
        {
          "type": "OneToMany",
          "targetEntity": "Event",
          "mappedBy": "lot",
          "cascade": ["ALL"],
          "orphanRemoval": true,
          "fetch": "LAZY",
          "jsonIgnore": true
        }
      ],
      "audit": []
    },
    {
      "name": "Event",
      "table": "events",
      "description": "Event registered in a cultivation lot (watering, fertilizing, harvest, etc.)",
      "annotations": ["@Entity", "@Data", "@Builder", "@AllArgsConstructor", "@NoArgsConstructor"],
      "fields": [
        { "name": "id", "type": "Long", "javaType": "java.lang.Long", "pk": true, "generated": "IDENTITY", "nullable": false, "unique": false, "swaggerExample": "1" },
        { "name": "timestamp", "type": "Instant", "javaType": "java.time.Instant", "nullable": false, "unique": false, "swaggerExample": "2025-01-15T10:30:00Z" },
        { "name": "description", "type": "String", "javaType": "java.lang.String", "nullable": true, "unique": false, "swaggerExample": "Riego automático completado" },
        { "name": "createdAt", "type": "Instant", "javaType": "java.time.Instant", "nullable": true, "unique": false, "notes": "Auto-set on @PrePersist", "swaggerExample": "2025-01-15T10:30:00Z" }
      ],
      "relationships": [
        {
          "type": "ManyToOne",
          "targetEntity": "Lot",
          "joinColumn": "lot_id",
          "nullable": false,
          "fetch": "LAZY",
          "jsonIgnoreProperties": ["events", "crop"]
        },
        {
          "type": "ManyToOne",
          "targetEntity": "EventType",
          "joinColumn": "type_id",
          "nullable": false,
          "fetch": "LAZY"
        },
        {
          "type": "ManyToOne",
          "targetEntity": "User",
          "joinColumn": "user_id",
          "nullable": false,
          "fetch": "LAZY"
        }
      ],
      "audit": [
        {
          "field": "createdAt",
          "strategy": "@PrePersist",
          "value": "Instant.now()",
          "description": "Auto-set before first persist"
        }
      ]
    },
    {
      "name": "EventType",
      "table": "event_types",
      "description": "Event category/type (SOWING, HARVEST, WATERING, etc.)",
      "annotations": ["@Entity", "@Data", "@Builder", "@AllArgsConstructor", "@NoArgsConstructor"],
      "fields": [
        { "name": "id", "type": "Long", "javaType": "java.lang.Long", "pk": true, "generated": "IDENTITY", "nullable": false, "unique": false, "swaggerExample": "1" },
        { "name": "name", "type": "String", "javaType": "java.lang.String", "nullable": false, "unique": true, "swaggerExample": "RIEGO" },
        { "name": "category", "type": "String", "javaType": "java.lang.String", "nullable": false, "unique": false, "swaggerExample": "MANTENIMIENTO" }
      ],
      "relationships": [],
      "audit": []
    }
  ],

  "relationships": {
    "summary": [
      { "from": "Crop", "to": "Lot", "type": "OneToMany", "via": "crop", "cascade": "ALL", "orphanRemoval": true },
      { "from": "Lot", "to": "Crop", "type": "ManyToOne", "via": "crop_id", "nullable": false },
      { "from": "Lot", "to": "Event", "type": "OneToMany", "via": "lot", "cascade": "ALL", "orphanRemoval": true },
      { "from": "Event", "to": "Lot", "type": "ManyToOne", "via": "lot_id", "nullable": false },
      { "from": "Event", "to": "EventType", "type": "ManyToOne", "via": "type_id", "nullable": false },
      { "from": "Event", "to": "User", "type": "ManyToOne", "via": "user_id", "nullable": false }
    ]
  },

  "dtos": {
    "requests": [
      {
        "name": "CropRequest",
        "package": "com.invernadero.proyecto.Dto.Request",
        "annotations": ["@Data", "@Builder", "@AllArgsConstructor", "@NoArgsConstructor"],
        "fields": [
          { "name": "name", "type": "String", "required": true, "validate": "@NotBlank(message = '{validation.crop.name.required}')", "swaggerExample": "Tomate" },
          { "name": "description", "type": "String", "required": false, "validate": null, "swaggerExample": "Variedad de tomate cherry para invernadero" },
          { "name": "inactivityDaysThreshold", "type": "Integer", "required": false, "validate": null, "minimum": 1, "swaggerExample": "7" },
          { "name": "estimatedGrowthDays", "type": "Integer", "required": false, "validate": null, "minimum": 1, "swaggerExample": "90" }
        ]
      },
      {
        "name": "EventRequest",
        "package": "com.invernadero.proyecto.Dto.Request",
        "annotations": ["@Data", "@Builder", "@AllArgsConstructor", "@NoArgsConstructor"],
        "fields": [
          { "name": "lotId", "type": "Long", "required": true, "validate": "@NotNull(message = '{validation.event.lotId.required}')", "swaggerExample": "1" },
          { "name": "type", "type": "String", "required": true, "validate": "@NotNull(message = '{validation.event.type.required}')", "swaggerExample": "SOWING" },
          { "name": "userId", "type": "Long", "required": true, "validate": "@NotNull(message = '{validation.event.userId.required}')", "swaggerExample": "1" },
          { "name": "timestamp", "type": "Instant", "required": true, "validate": "@NotNull(message = '{validation.event.timestamp.required}')", "swaggerExample": "2026-01-15T10:30:00Z" },
          { "name": "description", "type": "String", "required": true, "validate": "@NotNull(message = '{validation.event.description.required}')", "swaggerExample": "Siembra de semillas a 2cm de profundidad" }
        ]
      },
      {
        "name": "LotRequest",
        "package": "com.invernadero.proyecto.Dto.Request",
        "annotations": ["@Data", "@Builder", "@AllArgsConstructor", "@NoArgsConstructor"],
        "fields": [
          { "name": "name", "type": "String", "required": true, "validate": "@NotBlank(message = '{validation.lot.name.required}')", "swaggerExample": "Lote Norte A" },
          { "name": "cropId", "type": "Long", "required": true, "validate": "@NotNull(message = '{validation.lot.cropId.required}')", "swaggerExample": "1" },
          { "name": "startDate", "type": "Instant", "required": true, "validate": "@NotNull(message = '{validation.lot.startDate.required}')", "swaggerExample": "2026-01-15T10:30:00Z" },
          { "name": "endDate", "type": "Instant", "required": false, "validate": null, "swaggerExample": "2026-04-15T10:30:00Z" }
        ]
      },
      {
        "name": "UserRequest",
        "package": "com.invernadero.proyecto.Dto.Request",
        "annotations": ["@Data", "@Builder", "@AllArgsConstructor", "@NoArgsConstructor"],
        "fields": [
          { "name": "name", "type": "String", "required": true, "validate": "@NotBlank(message = '{validation.user.name.required}')", "swaggerExample": "Juan" },
          { "name": "lastName", "type": "String", "required": true, "validate": "@NotBlank(message = '{validation.user.lastName.required}')", "swaggerExample": "Pérez" },
          { "name": "email", "type": "String", "required": true, "validate": "@NotBlank(message = '{validation.user.email.required}') @Email(message = '{validation.user.email.invalid}')", "swaggerExample": "juan.perez@sigma.com" },
          { "name": "password", "type": "String", "required": true, "validate": "@NotBlank(message = '{validation.user.password.required}')", "swaggerExample": "Password123!" },
          { "name": "role", "type": "String", "required": true, "validate": "@NotBlank(message = '{validation.user.role.required}')", "notes": "One of: ADMIN, OPERATOR, VIEWER", "swaggerExample": "OPERATOR" }
        ]
      }
    ],
    "responses": [
      {
        "name": "CropResponse",
        "package": "com.invernadero.proyecto.Dto.response",
        "fields": [
          { "name": "id", "type": "Long", "swaggerExample": "1" },
          { "name": "name", "type": "String", "swaggerExample": "Tomate" },
          { "name": "description", "type": "String", "swaggerExample": "Variedad de tomate cherry" },
          { "name": "inactivityDaysThreshold", "type": "Long", "swaggerExample": "7", "notes": "Mapped from Integer via mapper" },
          { "name": "estimatedGrowthDays", "type": "Long", "swaggerExample": "90", "notes": "Mapped from Integer via mapper" }
        ]
      },
      {
        "name": "EventResponse",
        "package": "com.invernadero.proyecto.Dto.response",
        "fields": [
          { "name": "id", "type": "Long", "swaggerExample": "1", "$ref": "Event.id" },
          { "name": "lotId", "type": "Long", "swaggerExample": "3", "$ref": "Event.lot.id" },
          { "name": "lotName", "type": "String", "swaggerExample": "Lote Norte A", "$ref": "Event.lot.name" },
          { "name": "type", "type": "String", "swaggerExample": "RIEGO", "$ref": "Event.type.name" },
          { "name": "category", "type": "String", "swaggerExample": "MANTENIMIENTO", "$ref": "Event.type.category" },
          { "name": "userId", "type": "Long", "swaggerExample": "2", "$ref": "Event.user.id" },
          { "name": "userName", "type": "String", "swaggerExample": "Juan Pérez", "$ref": "Event.user.name" },
          { "name": "timestamp", "type": "Instant", "swaggerExample": "2025-01-15T10:30:00Z" },
          { "name": "description", "type": "String", "swaggerExample": "Riego automático completado" },
          { "name": "createdAt", "type": "Instant", "swaggerExample": "2025-01-15T10:30:00Z" }
        ]
      },
      {
        "name": "LotResponse",
        "package": "com.invernadero.proyecto.Dto.response",
        "fields": [
          { "name": "id", "type": "Long", "swaggerExample": "1" },
          { "name": "name", "type": "String", "swaggerExample": "Lote Norte A" },
          { "name": "cropId", "type": "Long", "swaggerExample": "1", "$ref": "Lot.crop.id" },
          { "name": "cropName", "type": "String", "swaggerExample": "Tomate", "$ref": "Lot.crop.name" },
          { "name": "startDate", "type": "Instant", "swaggerExample": "2026-01-15T10:30:00Z" },
          { "name": "endDate", "type": "Instant", "swaggerExample": "2026-04-15T10:30:00Z" }
        ]
      },
      {
        "name": "UserResponse",
        "package": "com.invernadero.proyecto.Dto.response",
        "fields": [
          { "name": "id", "type": "Long", "swaggerExample": "1" },
          { "name": "name", "type": "String", "swaggerExample": "Carlos" },
          { "name": "lastName", "type": "String", "swaggerExample": "García" },
          { "name": "email", "type": "String", "swaggerExample": "carlos.garcia@ejemplo.com" },
          { "name": "role", "type": "String", "swaggerExample": "ADMIN", "notes": "Enum name as string" },
          { "name": "active", "type": "boolean", "swaggerExample": "true" }
        ]
      },
      {
        "name": "DashboardResponse",
        "package": "com.invernadero.proyecto.Dto.response",
        "fields": [
          { "name": "eventChart", "type": "EventChartDTO", "$ref": "EventChartDTO" },
          { "name": "lotStatuses", "type": "List<LotStatusDTO>", "$ref": "LotStatusDTO" },
          { "name": "lotProgress", "type": "List<LotProgressDTO>", "$ref": "LotProgressDTO" }
        ]
      },
      {
        "name": "EventChartDTO",
        "package": "com.invernadero.proyecto.Dto.response",
        "fields": [
          { "name": "labels", "type": "List<String>", "notes": "Dates in yyyy-MM-dd format for last 30 days" },
          { "name": "values", "type": "List<Long>", "notes": "Event counts per date" }
        ]
      },
      {
        "name": "LotStatusDTO",
        "package": "com.invernadero.proyecto.Dto.response",
        "fields": [
          { "name": "lotId", "type": "Long" },
          { "name": "lotName", "type": "String" },
          { "name": "status", "type": "String", "notes": "CREATED | IN_PRODUCTION | FINISHED" },
          { "name": "inactivityLevel", "type": "String", "notes": "GRAY | GREEN | YELLOW | RED | UNKNOWN" }
        ]
      },
      {
        "name": "LotProgressDTO",
        "package": "com.invernadero.proyecto.Dto.response",
        "fields": [
          { "name": "lotId", "type": "Long" },
          { "name": "lotName", "type": "String" },
          { "name": "progress", "type": "double", "notes": "Percentage 0-100" },
          { "name": "estimatedHarvestDate", "type": "String", "notes": "ISO 8601 or 'null'" },
          { "name": "sowingDate", "type": "String", "notes": "ISO 8601 or 'null'" },
          { "name": "totalDays", "type": "int" },
          { "name": "daysElapsed", "type": "int" },
          { "name": "daysRemaining", "type": "int" }
        ]
      },
      {
        "name": "LotSummary",
        "package": "com.invernadero.proyecto.Dto.response",
        "fields": [
          { "name": "lotId", "type": "Long" },
          { "name": "lotName", "type": "String" },
          { "name": "status", "type": "String" },
          { "name": "inactivityStatus", "type": "String" },
          { "name": "totalEvents", "type": "Long" },
          { "name": "durationDays", "type": "Long" },
          { "name": "eventFrequency", "type": "Double", "notes": "Events per day" },
          { "name": "sowingDate", "type": "String" },
          { "name": "totalDays", "type": "int" },
          { "name": "daysElapsed", "type": "int" },
          { "name": "daysRemaining", "type": "int" },
          { "name": "estimatedHarvestDate", "type": "String" },
          { "name": "lastEventDate", "type": "Instant" },
          { "name": "lastEventType", "type": "String" }
        ]
      }
    ]
  },

  "api": {
    "basePath": "/api",
    "swagger": {
      "uiPath": "/swagger-ui.html",
      "apiDocsPath": "/api-docs",
      "operationsSorter": "method",
      "tagsSorter": "alpha",
      "displayRequestDuration": true,
      "tryItOutEnabled": true,
      "securityScheme": {
        "name": "bearer-jwt",
        "type": "http",
        "scheme": "bearer",
        "bearerFormat": "JWT"
      }
    },
    "tags": [
      { "name": "Autenticación", "description": "Gestión de autenticación y generación de tokens JWT" },
      { "name": "Cultivos", "description": "Gestión de cultivos del sistema" },
      { "name": "Dashboard", "description": "Visualización y métricas del sistema" },
      { "name": "Eventos", "description": "Gestión de eventos relacionados con los lotes" },
      { "name": "Tipos de Evento", "description": "Gestión de tipos de eventos relacionados con los lotes" },
      { "name": "Lote", "description": "Endpoints para gestionar los lotes de cultivo" },
      { "name": "User Management", "description": "Endpoints for managing users" }
    ],
    "endpoints": [
      {
        "tag": "Autenticación",
        "method": "POST",
        "path": "/api/auth/login",
        "summary": "Iniciar sesión",
        "auth": "none",
        "requestBody": {
          "contentType": "application/json",
          "schema": {
            "type": "object",
            "properties": {
              "email": { "type": "string", "required": true },
              "password": { "type": "string", "required": true }
            }
          }
        },
        "response": {
          "status": 200,
          "schema": { "type": "object", "properties": { "token": { "type": "string" } } }
        },
        "errorResponses": [
          { "status": 400, "description": "Solicitud inválida" },
          { "status": 401, "description": "Credenciales incorrectas" }
        ]
      },
      {
        "tag": "Cultivos",
        "method": "POST",
        "path": "/api/crops",
        "summary": "Crear cultivo",
        "auth": "bearer-jwt",
        "roles": ["ADMIN", "OPERATOR"],
        "requestBody": { "$ref": "CropRequest" },
        "response": { "status": 200, "$ref": "CropResponse" }
      },
      {
        "tag": "Cultivos",
        "method": "GET",
        "path": "/api/crops/{id}",
        "summary": "Obtener cultivo por ID",
        "auth": "bearer-jwt",
        "roles": ["ADMIN", "OPERATOR", "VIEWER"],
        "pathParams": [{ "name": "id", "type": "Long" }],
        "response": { "status": 200, "$ref": "CropResponse" }
      },
      {
        "tag": "Cultivos",
        "method": "GET",
        "path": "/api/crops",
        "summary": "Listar cultivos",
        "auth": "bearer-jwt",
        "roles": ["ADMIN", "OPERATOR", "VIEWER"],
        "response": { "status": 200, "type": "List<CropResponse>" }
      },
      {
        "tag": "Cultivos",
        "method": "PUT",
        "path": "/api/crops/{id}",
        "summary": "Actualizar cultivo",
        "auth": "bearer-jwt",
        "roles": ["ADMIN"],
        "pathParams": [{ "name": "id", "type": "Long" }],
        "requestBody": { "$ref": "CropRequest" },
        "response": { "status": 200, "$ref": "CropResponse" }
      },
      {
        "tag": "Cultivos",
        "method": "DELETE",
        "path": "/api/crops/{id}",
        "summary": "Eliminar cultivo",
        "auth": "bearer-jwt",
        "roles": ["ADMIN"],
        "pathParams": [{ "name": "id", "type": "Long" }],
        "response": { "status": 200, "type": "void" }
      },
      {
        "tag": "Dashboard",
        "method": "GET",
        "path": "/api/dashboard",
        "summary": "Dashboard completo",
        "auth": "bearer-jwt",
        "roles": ["ADMIN", "OPERATOR", "VIEWER"],
        "queryParams": [
          { "name": "cropId", "type": "Long", "required": false }
        ],
        "response": { "status": 200, "$ref": "DashboardResponse" }
      },
      {
        "tag": "Dashboard",
        "method": "GET",
        "path": "/api/dashboard/events",
        "summary": "Gráfico de eventos",
        "auth": "bearer-jwt",
        "roles": ["ADMIN", "OPERATOR", "VIEWER"],
        "queryParams": [
          { "name": "cropId", "type": "Long", "required": false }
        ],
        "response": { "status": 200, "$ref": "EventChartDTO" }
      },
      {
        "tag": "Dashboard",
        "method": "GET",
        "path": "/api/dashboard/status",
        "summary": "Estados de lotes",
        "auth": "bearer-jwt",
        "roles": ["ADMIN", "OPERATOR", "VIEWER"],
        "queryParams": [
          { "name": "cropId", "type": "Long", "required": false }
        ],
        "response": { "status": 200, "type": "List<LotStatusDTO>" }
      },
      {
        "tag": "Dashboard",
        "method": "GET",
        "path": "/api/dashboard/progress",
        "summary": "Progreso de lotes",
        "auth": "bearer-jwt",
        "roles": ["ADMIN", "OPERATOR", "VIEWER"],
        "queryParams": [
          { "name": "cropId", "type": "Long", "required": false }
        ],
        "response": { "status": 200, "type": "List<LotProgressDTO>" }
      },
      {
        "tag": "Eventos",
        "method": "POST",
        "path": "/api/events",
        "summary": "Registrar evento",
        "auth": "bearer-jwt",
        "roles": ["ADMIN", "OPERATOR"],
        "requestBody": { "$ref": "EventRequest" },
        "response": { "status": 200, "$ref": "EventResponse" }
      },
      {
        "tag": "Eventos",
        "method": "GET",
        "path": "/api/events/{id}",
        "summary": "Obtener evento por ID",
        "auth": "bearer-jwt",
        "roles": ["ADMIN", "OPERATOR", "VIEWER"],
        "pathParams": [{ "name": "id", "type": "Long" }],
        "response": { "status": 200, "$ref": "EventResponse" }
      },
      {
        "tag": "Eventos",
        "method": "GET",
        "path": "/api/events",
        "summary": "Listar eventos",
        "auth": "bearer-jwt",
        "roles": ["ADMIN", "OPERATOR", "VIEWER"],
        "response": { "status": 200, "type": "List<EventResponse>" }
      },
      {
        "tag": "Eventos",
        "method": "GET",
        "path": "/api/events/lot/{lotId}",
        "summary": "Eventos por lote",
        "auth": "bearer-jwt",
        "roles": ["ADMIN", "OPERATOR", "VIEWER"],
        "pathParams": [{ "name": "lotId", "type": "Long" }],
        "response": { "status": 200, "type": "List<EventResponse>" }
      },
      {
        "tag": "Eventos",
        "method": "GET",
        "path": "/api/events/lot/{lotId}/history",
        "summary": "Historial de eventos por lote",
        "auth": "bearer-jwt",
        "roles": ["ADMIN", "OPERATOR", "VIEWER"],
        "pathParams": [{ "name": "lotId", "type": "Long" }],
        "response": { "status": 200, "type": "List<EventResponse>" }
      },
      {
        "tag": "Eventos",
        "method": "GET",
        "path": "/api/events/filter",
        "summary": "Filtrar eventos",
        "auth": "bearer-jwt",
        "roles": ["ADMIN", "OPERATOR", "VIEWER"],
        "queryParams": [
          { "name": "lotId", "type": "Long", "required": true },
          { "name": "type", "type": "String", "required": false },
          { "name": "startDate", "type": "Instant", "required": false },
          { "name": "endDate", "type": "Instant", "required": false }
        ],
        "response": { "status": 200, "type": "List<EventResponse>" }
      },
      {
        "tag": "Tipos de Evento",
        "method": "GET",
        "path": "/api/event-types",
        "summary": "Listar tipos de eventos",
        "auth": "bearer-jwt",
        "roles": ["ADMIN", "OPERATOR", "VIEWER"],
        "response": { "status": 200, "type": "List<EventType>" }
      },
      {
        "tag": "Tipos de Evento",
        "method": "GET",
        "path": "/api/event-types/{id}",
        "summary": "Tipo de evento por ID",
        "auth": "bearer-jwt",
        "roles": ["ADMIN", "OPERATOR", "VIEWER"],
        "pathParams": [{ "name": "id", "type": "Long" }],
        "response": { "status": 200, "$ref": "EventType" }
      },
      {
        "tag": "Tipos de Evento",
        "method": "GET",
        "path": "/api/event-types/name/{name}",
        "summary": "Tipo de evento por nombre",
        "auth": "bearer-jwt",
        "roles": ["ADMIN", "OPERATOR", "VIEWER"],
        "pathParams": [{ "name": "name", "type": "String" }],
        "response": { "status": 200, "$ref": "EventType" }
      },
      {
        "tag": "Lote",
        "method": "POST",
        "path": "/api/lots",
        "summary": "Crear lote",
        "auth": "bearer-jwt",
        "roles": ["ADMIN", "OPERATOR"],
        "requestBody": { "$ref": "LotRequest" },
        "response": { "status": 200, "$ref": "LotResponse" },
        "errorResponses": [{ "status": 400, "description": "Solicitud inválida" }]
      },
      {
        "tag": "Lote",
        "method": "GET",
        "path": "/api/lots/{id}",
        "summary": "Obtener lote por ID",
        "auth": "bearer-jwt",
        "roles": ["ADMIN", "OPERATOR", "VIEWER"],
        "pathParams": [{ "name": "id", "type": "Long" }],
        "response": { "status": 200, "$ref": "LotResponse" },
        "errorResponses": [{ "status": 404, "description": "Lote no encontrado" }]
      },
      {
        "tag": "Lote",
        "method": "GET",
        "path": "/api/lots",
        "summary": "Listar lotes",
        "auth": "bearer-jwt",
        "roles": ["ADMIN", "OPERATOR", "VIEWER"],
        "response": { "status": 200, "type": "List<LotResponse>" }
      },
      {
        "tag": "Lote",
        "method": "GET",
        "path": "/api/lots/crop/{cropId}",
        "summary": "Lotes por cultivo",
        "auth": "bearer-jwt",
        "roles": ["ADMIN", "OPERATOR", "VIEWER"],
        "pathParams": [{ "name": "cropId", "type": "Long" }],
        "response": { "status": 200, "type": "List<LotResponse>" }
      },
      {
        "tag": "Lote",
        "method": "PUT",
        "path": "/api/lots/{id}",
        "summary": "Actualizar lote",
        "auth": "bearer-jwt",
        "roles": ["ADMIN", "OPERATOR"],
        "pathParams": [{ "name": "id", "type": "Long" }],
        "requestBody": { "$ref": "LotRequest" },
        "response": { "status": 200, "$ref": "LotResponse" },
        "errorResponses": [{ "status": 404, "description": "Lote no encontrado" }]
      },
      {
        "tag": "Lote",
        "method": "DELETE",
        "path": "/api/lots/{id}",
        "summary": "Eliminar lote",
        "auth": "bearer-jwt",
        "roles": ["ADMIN"],
        "pathParams": [{ "name": "id", "type": "Long" }],
        "response": { "status": 200, "type": "void" },
        "errorResponses": [{ "status": 404, "description": "Lote no encontrado" }]
      },
      {
        "tag": "Lote",
        "method": "GET",
        "path": "/api/lots/{id}/summary",
        "summary": "Resumen de lote",
        "auth": "bearer-jwt",
        "roles": ["ADMIN", "OPERATOR", "VIEWER"],
        "pathParams": [{ "name": "id", "type": "Long" }],
        "response": { "status": 200, "$ref": "LotSummary" },
        "errorResponses": [{ "status": 404, "description": "Lote no encontrado" }]
      },
      {
        "tag": "User Management",
        "method": "POST",
        "path": "/api/users",
        "summary": "Create user",
        "auth": "bearer-jwt",
        "roles": ["ADMIN"],
        "requestBody": { "$ref": "UserRequest" },
        "response": { "status": 200, "$ref": "UserResponse" },
        "errorResponses": [{ "status": 400, "description": "Invalid input data" }]
      },
      {
        "tag": "User Management",
        "method": "GET",
        "path": "/api/users/{id}",
        "summary": "Get user by ID",
        "auth": "bearer-jwt",
        "roles": ["ADMIN", "OPERATOR"],
        "pathParams": [{ "name": "id", "type": "Long" }],
        "response": { "status": 200, "$ref": "UserResponse" },
        "errorResponses": [{ "status": 404, "description": "User not found" }]
      },
      {
        "tag": "User Management",
        "method": "GET",
        "path": "/api/users",
        "summary": "Get all users",
        "auth": "bearer-jwt",
        "roles": ["ADMIN"],
        "response": { "status": 200, "type": "List<UserResponse>" }
      },
      {
        "tag": "User Management",
        "method": "PUT",
        "path": "/api/users/{id}",
        "summary": "Update user",
        "auth": "bearer-jwt",
        "roles": ["ADMIN"],
        "pathParams": [{ "name": "id", "type": "Long" }],
        "requestBody": { "$ref": "UserRequest" },
        "response": { "status": 200, "$ref": "UserResponse" },
        "errorResponses": [
          { "status": 400, "description": "Invalid input data" },
          { "status": 404, "description": "User not found" }
        ]
      },
      {
        "tag": "User Management",
        "method": "DELETE",
        "path": "/api/users/{id}",
        "summary": "Delete user",
        "auth": "bearer-jwt",
        "roles": ["ADMIN"],
        "pathParams": [{ "name": "id", "type": "Long" }],
        "response": { "status": 200, "type": "void" },
        "errorResponses": [{ "status": 404, "description": "User not found" }]
      }
    ]
  },

  "services": [
    {
      "name": "AuthService",
      "package": "com.invernadero.proyecto.Service",
      "description": "Authentication service - login and JWT generation",
      "dependencies": ["AuthenticationManager", "UserRepository", "JwtService"],
      "methods": [
        {
          "name": "login",
          "params": [
            { "name": "email", "type": "String", "description": "User email" },
            { "name": "password", "type": "String", "description": "User password" }
          ],
          "returns": { "type": "String", "description": "JWT token" },
          "exceptions": ["RuntimeException: Invalid credentials", "RuntimeException: User not found"],
          "logic": "authenticate via AuthenticationManager -> find user by email -> generate JWT token"
        }
      ]
    },
    {
      "name": "CropService",
      "package": "com.invernadero.proyecto.Service",
      "description": "Crop CRUD with name uniqueness validation",
      "dependencies": ["CropRepository"],
      "methods": [
        { "name": "createCrop", "params": [{ "name": "request", "type": "CropRequest" }], "returns": "CropResponse", "logic": "Validate unique name -> build entity from request -> save -> map to DTO" },
        { "name": "getCropById", "params": [{ "name": "id", "type": "Long" }], "returns": "CropResponse", "logic": "Find by id or throw -> map to DTO" },
        { "name": "getAllCrops", "params": [], "returns": "List<CropResponse>", "logic": "Find all -> stream -> map to DTO" },
        { "name": "updateCrop", "params": [{ "name": "id", "type": "Long" }, { "name": "request", "type": "CropRequest" }], "returns": "CropResponse", "logic": "Find by id -> update non-null fields -> save -> map to DTO" },
        { "name": "deleteCrop", "params": [{ "name": "id", "type": "Long" }], "returns": "void", "logic": "deleteById" }
      ]
    },
    {
      "name": "DashboardService",
      "package": "com.invernadero.proyecto.Service",
      "description": "Dashboard aggregator - combines lot statuses, progress, and event chart",
      "dependencies": ["EventRepository", "LotRepository", "LotService"],
      "methods": [
        { "name": "getDashboard", "params": [{ "name": "cropId", "type": "Long", "nullable": true }], "returns": "DashboardResponse", "logic": "Build eventChart + lotStatuses + lotProgress" }
      ],
      "privateMethods": [
        { "name": "buildEventChart", "logic": "Count events per day for last 30 days, fill missing days with 0" },
        { "name": "getLots", "logic": "If cropId null: findAll(), else: findByCropId()" },
        { "name": "buildLotStatuses", "logic": "For each lot: compute status and inactivityLevel via LotService" },
        { "name": "buildLotProgress", "logic": "For each lot: compute progress percentage and time metrics via LotService" }
      ]
    },
    {
      "name": "EventService",
      "package": "com.invernadero.proyecto.Service",
      "description": "Event registration with validation and business rules",
      "dependencies": ["EventRepository", "LotRepository", "EventTypeRepository", "UserRepository"],
      "methods": [
        { "name": "registerEvent", "params": [{ "name": "request", "type": "EventRequest" }], "returns": "EventResponse", "logic": "validate -> resolve entities -> validateEventSequence -> build -> save -> if SOWING: recalculate estimatedHarvestDate -> map to DTO" },
        { "name": "getEventById", "params": [{ "name": "id", "type": "Long" }], "returns": "EventResponse" },
        { "name": "getAllEvents", "params": [], "returns": "List<EventResponse>" },
        { "name": "getEventsByLot", "params": [{ "name": "lotId", "type": "Long" }], "returns": "List<EventResponse>" },
        { "name": "getEventHistoryByLot", "params": [{ "name": "lotId", "type": "Long" }], "returns": "List<Event>" },
        { "name": "filterEvents", "params": [{ "name": "lotId", "type": "Long" }, { "name": "type", "type": "String", "nullable": true }, { "name": "startDate", "type": "Instant", "nullable": true }, { "name": "endDate", "type": "Instant", "nullable": true }], "returns": "List<Event>" }
      ],
      "privateMethods": [
        { "name": "validateEvent", "logic": "Check lotId, typeName, timestamp are not null" },
        { "name": "validateEventSequence", "logic": "Cannot HARVEST before SOWING. Cannot SOW twice. No events after HARVEST." }
      ]
    },
    {
      "name": "EventTypeService",
      "package": "com.invernadero.proyecto.Service",
      "description": "Read-only service for event type catalog",
      "dependencies": ["EventTypeRepository"],
      "methods": [
        { "name": "getAllEventTypes", "params": [], "returns": "List<EventType>" },
        { "name": "getEventTypeById", "params": [{ "name": "id", "type": "Long" }], "returns": "EventType" },
        { "name": "getEventTypeByName", "params": [{ "name": "name", "type": "String" }], "returns": "EventType" }
      ]
    },
    {
      "name": "LotService",
      "package": "com.invernadero.proyecto.Service",
      "description": "Lot CRUD + business metrics (status, progress, inactivity, duration, frequency)",
      "dependencies": ["LotRepository", "CropRepository", "EventRepository"],
      "methods": [
        { "name": "createLot", "params": [{ "name": "request", "type": "LotRequest" }], "returns": "LotResponse", "logic": "Find crop by id -> build lot from request -> save -> map to DTO" },
        { "name": "getLotById", "params": [{ "name": "id", "type": "Long" }], "returns": "LotResponse" },
        { "name": "getAllLots", "params": [], "returns": "List<LotResponse>" },
        { "name": "getLotsByCrop", "params": [{ "name": "cropId", "type": "Long" }], "returns": "List<LotResponse>" },
        { "name": "updateLot", "params": [{ "name": "id", "type": "Long" }, { "name": "request", "type": "LotRequest" }], "returns": "LotResponse" },
        { "name": "deleteLot", "params": [{ "name": "id", "type": "Long" }], "returns": "void" },
        { "name": "getLotStatus", "params": [{ "name": "lotId", "type": "Long" }], "returns": "String (CREATED|IN_PRODUCTION|FINISHED)", "logic": "Check SOWING and HARVEST existence" },
        { "name": "countEvents", "params": [{ "name": "lotId", "type": "Long" }], "returns": "long" },
        { "name": "calculateDurationInDays", "params": [{ "name": "lotId", "type": "Long" }], "returns": "long", "logic": "Duration between SOWING and HARVEST (or now if no HARVEST)" },
        { "name": "calculateEventFrequency", "params": [{ "name": "lotId", "type": "Long" }], "returns": "double", "logic": "totalEvents / durationDays" },
        { "name": "getInactivityStatus", "params": [{ "name": "lotId", "type": "Long" }], "returns": "String (GRAY|GREEN|YELLOW|RED|UNKNOWN)", "logic": "Days since last event vs crop.inactivityDaysThreshold" },
        { "name": "getCropProgress", "params": [{ "name": "lotId", "type": "Long" }], "returns": "double (0-100)", "logic": "daysElapsed / totalDays * 100, capped at 100" },
        { "name": "getEstimatedHarvestDate", "params": [{ "name": "lotId", "type": "Long" }], "returns": "Instant" },
        { "name": "getEventsLast7Days", "params": [], "returns": "Map<String, Long>" },
        { "name": "getLotSummary", "params": [{ "name": "lotId", "type": "Long" }], "returns": "LotSummary", "logic": "Aggregate all metrics for a lot" },
        { "name": "getLotProgressDetails", "params": [{ "name": "lotId", "type": "Long" }], "returns": "Map{sowingDate, totalDays, daysElapsed, daysRemaining}" }
      ]
    },
    {
      "name": "UserService",
      "package": "com.invernadero.proyecto.Service",
      "description": "User CRUD with BCrypt password encoding and email uniqueness",
      "dependencies": ["UserRepository", "PasswordEncoder"],
      "methods": [
        { "name": "createUser", "params": [{ "name": "request", "type": "UserRequest" }], "returns": "UserResponse", "logic": "Check email uniqueness -> encode password -> set role from string -> save -> map to DTO" },
        { "name": "getById", "params": [{ "name": "id", "type": "Long" }], "returns": "UserResponse" },
        { "name": "getAll", "params": [], "returns": "List<UserResponse>" },
        { "name": "updateUser", "params": [{ "name": "id", "type": "Long" }, { "name": "request", "type": "UserRequest" }], "returns": "UserResponse", "logic": "Find -> update non-null fields (encode password if provided) -> save -> map to DTO" },
        { "name": "deleteUser", "params": [{ "name": "id", "type": "Long" }], "returns": "void" }
      ]
    }
  ],

  "repositories": [
    {
      "entity": "Crop",
      "interface": "CropRepository",
      "extends": "JpaRepository<Crop, Long>",
      "customQueries": [
        { "method": "findByName", "params": [{ "name": "name", "type": "String" }], "returns": "Optional<Crop>" }
      ]
    },
    {
      "entity": "Event",
      "interface": "EventRepository",
      "extends": "JpaRepository<Event, Long>",
      "customQueries": [
        { "method": "findByLotId", "params": [{ "name": "lotId", "type": "Long" }], "returns": "List<Event>" },
        { "method": "findByLotIdOrderByTimestampAsc", "params": [{ "name": "lotId", "type": "Long" }], "returns": "List<Event>" },
        { "method": "findByLotIdOrderByTimestampDesc", "params": [{ "name": "lotId", "type": "Long" }], "returns": "List<Event>" },
        { "method": "existsByLotIdAndTypeName", "params": [{ "name": "lotId", "type": "Long" }, { "name": "typeName", "type": "String" }], "returns": "boolean" },
        { "method": "findTopByLotIdOrderByTimestampDesc", "params": [{ "name": "lotId", "type": "Long" }], "returns": "Optional<Event>" },
        { "method": "filterEvents", "logic": "@Query custom JPQL", "params": [{ "name": "lotId", "type": "Long" }, { "name": "type", "type": "String", "nullable": true }, { "name": "startDate", "type": "Instant", "nullable": true }, { "name": "endDate", "type": "Instant", "nullable": true }], "returns": "List<Event>" },
        { "method": "countEventsByDay(Instant)", "logic": "@Query custom JPQL with GROUP BY DATE", "params": [{ "name": "startDate", "type": "Instant" }], "returns": "List<Object[]>" },
        { "method": "countEventsByDay(Instant, Long)", "logic": "@Query custom JPQL with GROUP BY DATE and crop filter", "params": [{ "name": "startDate", "type": "Instant" }, { "name": "cropId", "type": "Long", "nullable": true }], "returns": "List<Object[]>" }
      ]
    },
    {
      "entity": "EventType",
      "interface": "EventTypeRepository",
      "extends": "JpaRepository<EventType, Long>",
      "customQueries": [
        { "method": "findByName", "params": [{ "name": "name", "type": "String" }], "returns": "Optional<EventType>" }
      ]
    },
    {
      "entity": "Lot",
      "interface": "LotRepository",
      "extends": "JpaRepository<Lot, Long>",
      "customQueries": [
        { "method": "findByCropId", "params": [{ "name": "cropId", "type": "Long" }], "returns": "List<Lot>" }
      ]
    },
    {
      "entity": "User",
      "interface": "UserRepository",
      "extends": "JpaRepository<User, Long>",
      "customQueries": [
        { "method": "findByEmail", "params": [{ "name": "email", "type": "String" }], "returns": "Optional<User>" },
        { "method": "existsByEmail", "params": [{ "name": "email", "type": "String" }], "returns": "boolean" }
      ]
    }
  ],

  "mappers": [
    {
      "from": "Crop",
      "to": "CropResponse",
      "type": "static method",
      "class": "CropMapper",
      "method": "toDTO",
      "mappings": [
        { "source": "crop.id", "target": "id" },
        { "source": "crop.name", "target": "name" },
        { "source": "crop.description", "target": "description" },
        { "source": "crop.estimatedGrowthDays (Integer)", "target": "estimatedGrowthDays (Long)", "transform": "Long.valueOf()" },
        { "source": "crop.inactivityDaysThreshold (Integer)", "target": "inactivityDaysThreshold (Long)", "transform": "Long.valueOf()" }
      ]
    },
    {
      "from": "Event",
      "to": "EventResponse",
      "type": "static method",
      "class": "EventMapper",
      "method": "toDTO",
      "mappings": [
        { "source": "event.id", "target": "id" },
        { "source": "event.lot.id", "target": "lotId" },
        { "source": "event.lot.name", "target": "lotName" },
        { "source": "event.type.name", "target": "type" },
        { "source": "event.type.category", "target": "category" },
        { "source": "event.user.id", "target": "userId" },
        { "source": "event.user.name", "target": "userName" },
        { "source": "event.timestamp", "target": "timestamp" },
        { "source": "event.description", "target": "description" },
        { "source": "event.createdAt", "target": "createdAt" }
      ]
    },
    {
      "from": "Lot",
      "to": "LotResponse",
      "type": "static method",
      "class": "LotMapper",
      "method": "toDTO",
      "mappings": [
        { "source": "lot.id", "target": "id" },
        { "source": "lot.name", "target": "name" },
        { "source": "lot.crop.id", "target": "cropId" },
        { "source": "lot.crop.name", "target": "cropName" },
        { "source": "lot.startDate", "target": "startDate" },
        { "source": "lot.endDate", "target": "endDate" }
      ]
    },
    {
      "from": "User",
      "to": "UserResponse",
      "type": "static method",
      "class": "UserMapper",
      "method": "toDTO",
      "mappings": [
        { "source": "user.id", "target": "id" },
        { "source": "user.name", "target": "name" },
        { "source": "user.lastName", "target": "lastName" },
        { "source": "user.email", "target": "email" },
        { "source": "user.role.name()", "target": "role", "transform": "enum to string" },
        { "source": "user.active", "target": "active" }
      ]
    }
  ],

  "security": {
    "type": "JWT (JSON Web Token)",
    "algorithm": "HS256",
    "signingKey": {
      "source": "env JWT_SECRET",
      "encoding": "Base64"
    },
    "expiration": {
      "milliseconds": 18000000,
      "minutes": 300,
      "hours": 5
    },
    "tokenClaims": {
      "subject": "user email",
      "authorities": "List<String> (ROLE_ADMIN, ROLE_OPERATOR, ROLE_VIEWER)",
      "userId": "Long (user ID)"
    },
    "passwordEncoder": {
      "algorithm": "BCrypt",
      "class": "org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder"
    },
    "sessionPolicy": "STATELESS",
    "csrf": "DISABLED",
    "publicEndpoints": [
      { "pattern": "/api/auth/**", "methods": ["POST"] },
      { "pattern": "/swagger-ui/**", "methods": ["GET"] },
      { "pattern": "/swagger-ui.html", "methods": ["GET"] },
      { "pattern": "/v3/api-docs/**", "methods": ["GET"] },
      { "pattern": "/api-docs/**", "methods": ["GET"] },
      { "pattern": "/api/health/**", "methods": ["GET"] }
    ],
    "authenticatedEndpoints": "All other endpoints require valid JWT token",
    "roleHierarchy": {
      "ADMIN": ["ADMIN", "OPERATOR", "VIEWER"],
      "OPERATOR": ["OPERATOR", "VIEWER"],
      "VIEWER": ["VIEWER"]
    },
    "authorizationModel": "@PreAuthorize annotations on controller methods",
    "authenticationProvider": "DaoAuthenticationProvider with CustomUserDetailsService"
  },

  "securityEndpointMatrix": [
    { "method": "POST",   "path": "/api/auth/login",          "roles": ["ANONYMOUS"] },
    { "method": "POST",   "path": "/api/crops",               "roles": ["ADMIN", "OPERATOR"] },
    { "method": "GET",    "path": "/api/crops/**",            "roles": ["ADMIN", "OPERATOR", "VIEWER"] },
    { "method": "PUT",    "path": "/api/crops/*",             "roles": ["ADMIN"] },
    { "method": "DELETE", "path": "/api/crops/*",             "roles": ["ADMIN"] },
    { "method": "GET",    "path": "/api/dashboard/**",        "roles": ["ADMIN", "OPERATOR", "VIEWER"] },
    { "method": "POST",   "path": "/api/events",              "roles": ["ADMIN", "OPERATOR"] },
    { "method": "GET",    "path": "/api/events/**",           "roles": ["ADMIN", "OPERATOR", "VIEWER"] },
    { "method": "GET",    "path": "/api/event-types/**",      "roles": ["ADMIN", "OPERATOR", "VIEWER"] },
    { "method": "POST",   "path": "/api/lots",                "roles": ["ADMIN", "OPERATOR"] },
    { "method": "GET",    "path": "/api/lots/**",             "roles": ["ADMIN", "OPERATOR", "VIEWER"] },
    { "method": "PUT",    "path": "/api/lots/*",              "roles": ["ADMIN", "OPERATOR"] },
    { "method": "DELETE", "path": "/api/lots/*",              "roles": ["ADMIN"] },
    { "method": "POST",   "path": "/api/users",               "roles": ["ADMIN"] },
    { "method": "GET",    "path": "/api/users",               "roles": ["ADMIN"] },
    { "method": "GET",    "path": "/api/users/*",             "roles": ["ADMIN", "OPERATOR"] },
    { "method": "PUT",    "path": "/api/users/*",             "roles": ["ADMIN"] },
    { "method": "DELETE", "path": "/api/users/*",             "roles": ["ADMIN"] }
  ],

  "errorHandling": {
    "globalHandler": {
      "class": "GlobalExceptionHandler",
      "package": "com.invernadero.proyecto.Exception",
      "type": "@RestControllerAdvice",
      "dependencies": ["MessageSource"]
    },
    "errorResponseStructure": {
      "class": "ApiError",
      "package": "com.invernadero.proyecto.Exception",
      "annotations": ["@Data", "@Builder"],
      "fields": [
        { "name": "timestamp", "type": "Instant" },
        { "name": "status", "type": "int" },
        { "name": "error", "type": "String" },
        { "name": "message", "type": "String" },
        { "name": "path", "type": "String" },
        { "name": "details", "type": "List<String>", "required": false, "notes": "Only for validation errors" }
      ]
    },
    "handlers": [
      { "exception": "MethodArgumentNotValidException", "status": 400, "i18nTitleKey": "error.validation.title", "i18nMessageKey": "error.validation.message", "notes": "@Valid annotation on request body" },
      { "exception": "ConstraintViolationException",    "status": 400, "i18nTitleKey": "error.constraint.title", "i18nMessageKey": "error.constraint.message" },
      { "exception": "BadCredentialsException",         "status": 401, "i18nTitleKey": "error.unauthorized.title", "i18nMessageKey": "error.unauthorized.message" },
      { "exception": "AccessDeniedException",           "status": 403, "i18nTitleKey": "error.forbidden.title", "i18nMessageKey": "error.forbidden.message" },
      { "exception": "RuntimeException",                "status": 400, "i18nTitleKey": "error.business.title", "notes": "Message = exception.getMessage() (dynamic)" },
      { "exception": "Exception",                       "status": 500, "i18nTitleKey": "error.internal.title", "i18nMessageKey": "error.internal.message" }
    ]
  },

  "businessRules": [
    {
      "domain": "Event Sequence Validation",
      "description": "Agricultural event sequencing rules enforced at registration",
      "triggeredBy": "EventService.registerEvent()",
      "rules": [
        {
          "id": "EVT-SEQ-001",
          "condition": "Event type is HARVEST and lot has NO SOWING event",
          "action": "REJECT with message 'Cannot register harvest before sowing'",
          "severity": "ERROR"
        },
        {
          "id": "EVT-SEQ-002",
          "condition": "Event type is SOWING and lot already has a SOWING event",
          "action": "REJECT with message 'Sowing already exists for this lot'",
          "severity": "ERROR"
        },
        {
          "id": "EVT-SEQ-003",
          "condition": "Lot already has a HARVEST event (any new event attempt)",
          "action": "REJECT with message 'This lot is already finished'",
          "severity": "ERROR"
        }
      ]
    },
    {
      "domain": "Harvest Date Calculation",
      "description": "When a SOWING event is registered, automatically calculate estimated harvest date",
      "triggeredBy": "EventService.registerEvent()",
      "rules": [
        {
          "id": "HRV-CALC-001",
          "condition": "Event type is SOWING AND crop.estimatedGrowthDays is not null",
          "action": "SET lot.estimatedHarvestDate = event.timestamp + crop.estimatedGrowthDays days",
          "severity": "INFO"
        }
      ]
    },
    {
      "domain": "Lot Status Computation",
      "description": "Derived status based on events (not stored in DB)",
      "computed": true,
      "values": [
        { "status": "CREATED",       "condition": "Lot has no SOWING event" },
        { "status": "IN_PRODUCTION", "condition": "Lot has SOWING but no HARVEST event" },
        { "status": "FINISHED",      "condition": "Lot has HARVEST event" }
      ]
    },
    {
      "domain": "Inactivity Level Computation",
      "description": "Alert level based on days since last event vs crop threshold",
      "computed": true,
      "values": [
        { "level": "GRAY",   "condition": "Lot has NO events" },
        { "level": "GREEN",  "condition": "daysWithoutEvents < threshold / 2" },
        { "level": "YELLOW", "condition": "daysWithoutEvents >= threshold / 2 AND < threshold" },
        { "level": "RED",    "condition": "daysWithoutEvents >= threshold" },
        { "level": "UNKNOWN","condition": "Crop has no inactivityDaysThreshold" }
      ],
      "formula": "daysWithoutEvents = Duration.between(lastEvent.timestamp, now).toDays()"
    },
    {
      "domain": "Crop Progress Computation",
      "description": "Percentage of growth cycle completed",
      "computed": true,
      "formula": "MIN(currentDays / totalDays * 100, 100)",
      "variables": [
        { "name": "currentDays", "description": "Days from SOWING timestamp to now" },
        { "name": "totalDays",   "description": "Days from SOWING timestamp to estimatedHarvestDate" }
      ]
    },
    {
      "domain": "Business Validations (Service Layer)",
      "description": "Uniqueness and existence checks",
      "rules": [
        { "id": "BZ-001", "rule": "Crop name must be unique", "enforcedIn": "CropService.createCrop()" },
        { "id": "BZ-002", "rule": "User email must be unique", "enforcedIn": "UserService.createUser()" },
        { "id": "BZ-003", "rule": "Crop referenced by Lot must exist", "enforcedIn": "LotService.createLot()" },
        { "id": "BZ-004", "rule": "Event type referenced by Event must exist", "enforcedIn": "EventService.registerEvent()" },
        { "id": "BZ-005", "rule": "User referenced by Event must exist", "enforcedIn": "EventService.registerEvent()" },
        { "id": "BZ-006", "rule": "Lot referenced by Event must exist", "enforcedIn": "EventService.registerEvent()" }
      ]
    }
  ],

  "validations": {
    "framework": "Jakarta Bean Validation (jakarta.validation)",
    "globalConfig": {
      "includeMessageOnError": true,
      "includeBindingErrors": true
    },
    "fieldRules": [
      { "dto": "CropRequest",   "field": "name",                     "rules": ["@NotBlank"] },
      { "dto": "EventRequest",  "field": "lotId",                    "rules": ["@NotNull"] },
      { "dto": "EventRequest",  "field": "type",                     "rules": ["@NotNull"] },
      { "dto": "EventRequest",  "field": "userId",                   "rules": ["@NotNull"] },
      { "dto": "EventRequest",  "field": "timestamp",                "rules": ["@NotNull"] },
      { "dto": "EventRequest",  "field": "description",              "rules": ["@NotNull"] },
      { "dto": "LotRequest",    "field": "name",                     "rules": ["@NotBlank"] },
      { "dto": "LotRequest",    "field": "cropId",                   "rules": ["@NotNull"] },
      { "dto": "LotRequest",    "field": "startDate",                "rules": ["@NotNull"] },
      { "dto": "UserRequest",   "field": "name",                     "rules": ["@NotBlank"] },
      { "dto": "UserRequest",   "field": "lastName",                 "rules": ["@NotBlank"] },
      { "dto": "UserRequest",   "field": "email",                    "rules": ["@NotBlank", "@Email"] },
      { "dto": "UserRequest",   "field": "password",                 "rules": ["@NotBlank"] },
      { "dto": "UserRequest",   "field": "role",                     "rules": ["@NotBlank"] }
    ]
  },

  "dependencies": [
    { "group": "org.springframework.boot", "artifact": "spring-boot-starter-data-jpa",  "scope": "compile" },
    { "group": "org.springframework.boot", "artifact": "spring-boot-starter-mail",     "scope": "compile" },
    { "group": "org.springframework.boot", "artifact": "spring-boot-starter-security", "scope": "compile" },
    { "group": "org.springframework.boot", "artifact": "spring-boot-starter-validation","scope": "compile" },
    { "group": "org.springframework.boot", "artifact": "spring-boot-starter-web",      "scope": "compile" },
    { "group": "org.springframework.boot", "artifact": "spring-boot-devtools",         "scope": "runtime", "optional": true },
    { "group": "org.postgresql",           "artifact": "postgresql",                   "scope": "runtime" },
    { "group": "com.itextpdf",             "artifact": "itextpdf",        "version": "5.5.13.3", "scope": "compile" },
    { "group": "org.apache.poi",           "artifact": "poi-ooxml",       "version": "5.4.0",   "scope": "compile" },
    { "group": "org.projectlombok",        "artifact": "lombok",                        "scope": "compile", "optional": true },
    { "group": "io.jsonwebtoken",          "artifact": "jjwt-api",        "version": "0.12.6",  "scope": "compile" },
    { "group": "io.jsonwebtoken",          "artifact": "jjwt-impl",       "version": "0.12.6",  "scope": "runtime" },
    { "group": "io.jsonwebtoken",          "artifact": "jjwt-jackson",    "version": "0.12.6",  "scope": "runtime" },
    { "group": "me.paulschwarz",           "artifact": "spring-dotenv",   "version": "4.0.0",   "scope": "compile" },
    { "group": "net.sourceforge.tess4j",   "artifact": "tess4j",          "version": "5.8.0",   "scope": "compile" },
    { "group": "org.apache.pdfbox",        "artifact": "pdfbox",          "version": "2.0.30",  "scope": "compile" },
    { "group": "org.springdoc",            "artifact": "springdoc-openapi-starter-webmvc-ui", "version": "2.7.0", "scope": "compile" },
    { "group": "io.swagger.core.v3",       "artifact": "swagger-annotations", "version": "2.2.21", "scope": "compile" },
    { "group": "org.jetbrains",            "artifact": "annotations",    "version": "24.1.0",   "scope": "compile" },
    { "group": "com.h2database",           "artifact": "h2",                                 "scope": "test" },
    { "group": "org.springframework.boot", "artifact": "spring-boot-starter-test",         "scope": "test" },
    { "group": "org.springframework.security", "artifact": "spring-security-test",         "scope": "test" }
  ],

  "deployment": {
    "environmentVariables": [
      { "name": "PORT",         "description": "Server port (default: 8080)",      "required": false, "defaultValue": "8080" },
      { "name": "DB_URL",       "description": "Full JDBC URL (prod profile)",     "required": true },
      { "name": "DB_HOST",      "description": "Database host (dev profile)",      "required": false },
      { "name": "DB_PORT",      "description": "Database port (dev profile)",      "required": false },
      { "name": "DB_NAME",      "description": "Database name (dev profile)",      "required": false },
      { "name": "DB_USER",      "description": "Database username",                 "required": true },
      { "name": "DB_PASSWORD",  "description": "Database password",                 "required": true },
      { "name": "JWT_SECRET",   "description": "Base64-encoded secret for JWT",    "required": true },
      { "name": "FRONTEND_URL", "description": "Frontend URL for CORS",             "required": true }
    ],
    "activeProfile": "prod",
    "multipart": {
      "maxFileSize": "20MB",
      "maxRequestSize": "20MB"
    },
    "encoding": {
      "charset": "UTF-8",
      "force": true
    }
  },

  "projectStructure": {
    "sourceBase": "src/main/java/com/invernadero/proyecto",
    "testBase": "src/test/java/com/invernadero/proyecto",
    "resourcesBase": "src/main/resources",
    "directories": [
      { "path": "Entity/",         "purpose": "JPA entities",            "files": ["Crop.java", "Event.java", "EventType.java", "Lot.java", "User.java"] },
      { "path": "Entity/enums/",  "purpose": "Enumerations",            "files": ["Role.java"] },
      { "path": "Dto/Request/",   "purpose": "Request DTOs",            "files": ["CropRequest.java", "EventRequest.java", "LotRequest.java", "UserRequest.java"] },
      { "path": "Dto/response/",  "purpose": "Response DTOs",           "files": ["CropResponse.java", "DashboardResponse.java", "EventChartDTO.java", "EventResponse.java", "LotProgressDTO.java", "LotResponse.java", "LotStatusDTO.java", "LotSummary.java", "UserResponse.java"] },
      { "path": "Repository/",    "purpose": "Spring Data repositories", "files": ["CropRepository.java", "EventRepository.java", "EventTypeRepository.java", "LotRepository.java", "UserRepository.java"] },
      { "path": "Service/",       "purpose": "Business logic services",  "files": ["AuthService.java", "CropService.java", "DashboardService.java", "EventService.java", "EventTypeService.java", "LotService.java", "UserService.java"] },
      { "path": "controller/",    "purpose": "REST controllers",        "files": ["AuthController.java", "CropController.java", "DashboardController.java", "EventController.java", "EventTypeController.java", "LotController.java", "UserController.java"] },
      { "path": "Security/",      "purpose": "JWT + Spring Security",   "files": ["CustomUserDetailsService.java", "JwtAuthenticationFilter.java", "JwtService.java", "SecurityConfig.java"] },
      { "path": "mapper/",        "purpose": "Entity-to-DTO mappers",    "files": ["CropMapper.java", "EventMapper.java", "LotMapper.java", "UserMapper.java"] },
      { "path": "Exception/",     "purpose": "Exception handling",       "files": ["ApiError.java", "GlobalExceptionHandler.java"] },
      { "path": "doc/",           "purpose": "Documentation",            "files": ["JSON.md"] }
    ],
    "resources": [
      { "file": "application.properties",     "purpose": "Main config" },
      { "file": "application-dev.properties", "purpose": "Development profile" },
      { "file": "application-prod.properties","purpose": "Production profile" },
      { "file": "messages.properties",        "purpose": "i18n English" },
      { "file": "messages_es.properties",     "purpose": "i18n Spanish" }
    ]
  }
}
```

---

## Guía de Uso

### Cómo usar este meta-modelo para migrar a otro stack

El JSON anterior está diseñado para ser **la fuente única de verdad** del proyecto. Para migrar a otro lenguaje/framework:

#### 1. Generar esquema de base de datos
Toma `entities` → cada entidad con sus `fields` (tipo, PK, generated, unique, nullable, columnDefinition) y `relationships` → genera DDL/Schema para PostgreSQL, MySQL, MongoDB, etc.

#### 2. Generar modelos/entidades
`entities[].name` + `fields[].{name,type,javaType}` + `relationships` → genera clases/structs en cualquier lenguaje (Python Django, Node Prisma, Go GORM, C# EF Core).

#### 3. Generar API endpoints
`api.endpoints` → genera controladores/rutas con su método HTTP, path, roles, request/response types. Ideal para generar clientes OpenAPI/Swagger, Postman collections, o clientes TypeScript.

#### 4. Generar DTOs de entrada/salida
`dtos.requests` y `dtos.responses` + `mappers` → genera schemas de serialización y lógica de transformación.

#### 5. Generar sistema de autorización
`securityEndpointMatrix` + `security.roleHierarchy` + `security.publicEndpoints` → genera middlewares/guards de autenticación y autorización.

#### 6. Generar validaciones
`validations.fieldRules` + `languages.i18n.keys` → genera reglas de validación con mensajes internacionalizados.

#### 7. Generar reglas de negocio
`businessRules` → implementar lógica de secuencia de eventos, cálculo de estados, progreso, inactividad.

#### 8. Generar manejo de errores
`errorHandling` → estructura de respuesta de error uniforme + handlers por tipo de excepción.

---

### Ejemplos de generación automatizada

| Salida deseada | Sección del JSON a usar |
|---|---|
| Tabla SQL (`CREATE TABLE users ...`) | `entities[].fields` + `entities[].table` |
| Modelo Prisma / TypeORM / Sequelize | `entities[]` + `relationships` |
| Cliente API TypeScript (`fetch` / `axios`) | `api.endpoints` |
| Colección Postman / Insomnia | `api.endpoints` |
| Schemas Zod / Yup para validación frontend | `dtos.requests` + `validations.fieldRules` |
| Guards de Angular / React Router | `securityEndpointMatrix` |
| Documentación OpenAPI 3.0 | `api` completa |
| Pruebas automatizadas (contratos) | `api.endpoints` + `dtos` |
| Configuración Docker / env template | `deployment.environmentVariables` |
| Archivos i18n para frontend | `languages.i18n.keys` |

---

## Diagrama de Entidades y Relaciones

```
┌──────────────┐       ┌──────────────┐
│     User     │       │   EventType  │
│──────────────│       │──────────────│
│ id (PK)      │       │ id (PK)      │
│ name         │       │ name (UQ)    │
│ lastName     │       │ category     │
│ email (UQ)   │       └──────┬───────┘
│ password     │              │
│ role (Enum)  │              │
│ active       │              │
└──────┬───────┘              │
       │ 1                    │
       │                     │
       │ ┌───────────────────┘
       │ │
       │ │
┌──────▼─▼────────┐       ┌──────────────┐
│      Event      │       │     Crop     │
│─────────────────│       │──────────────│
│ id (PK)         │       │ id (PK)      │
│ lot_id (FK) ────│──┐    │ name         │
│ type_id (FK) ───│──┘    │ description  │
│ user_id (FK) ───│──┘    │ inactivityDays│
│ timestamp       │       │ threshold    │
│ description     │       │ estimated    │
│ createdAt       │       │ growthDays   │
└─────────────────┘       └──────┬───────┘
                                 │ 1
                                 │
                    ┌────────────┘
                    │
               ┌────▼──────────────┐
               │       Lot         │
               │───────────────────│
               │ id (PK)           │
               │ name              │
               │ crop_id (FK) ─────│──┘
               │ start_date        │
               │ end_date          │
               │ estimated_harvest │
               │ _date             │
               └───────────────────┘

Relationships:
  Crop   1 ──── * Lot
  Lot    1 ──── * Event
  Event  * ──── 1 EventType
  Event  * ──── 1 User
```

---

## Glosario de Tipos

| Tipo JSON | Java Type | SQL Type | Notes |
|---|---|---|---|
| `Long` | `java.lang.Long` | `BIGINT` | IDs auto-generados |
| `String` | `java.lang.String` | `VARCHAR(255)` | Texto |
| `Integer` | `java.lang.Integer` | `INTEGER` | Números enteros |
| `boolean` | `boolean` | `BOOLEAN` | Flags |
| `Instant` | `java.time.Instant` | `TIMESTAMP WITH TIME ZONE` | Fechas UTC |
| `Enum(X)` | `Enum` | `VARCHAR` / `INT` | Enumeraciones |
| `double` | `double` | `DOUBLE PRECISION` | Decimales |
| `int` | `int` | `INTEGER` | Primitivo entero |
| `List<T>` | `java.util.List<T>` | - | Colecciones |
| `Map<K,V>` | `java.util.Map<K,V>` | - | Mapas |

---

## Reglas de Negocio Detalladas

### 1. Secuencia de Eventos
- **Siembra (SOWING)**: Solo una vez por lote. No se puede registrar si el lote ya está cosechado.
- **Cosecha (HARVEST)**: Solo si existe una siembra previa.
- **Estado final**: Una vez cosechado, no se permiten más eventos en ese lote.

### 2. Estados de Lote (derivados, no almacenados)
```
Sin SOWING        → CREATED
Con SOWING        → IN_PRODUCTION
Con HARVEST       → FINISHED
```

### 3. Niveles de Inactividad
```
Sin eventos       → GRAY
días < umbral/2   → GREEN
días >= umbral/2  → YELLOW
días >= umbral    → RED
Sin umbral        → UNKNOWN
```

### 4. Progreso del Cultivo
```
progreso = MIN(díasTranscurridos / díasTotales * 100, 100)
```
Se calcula desde la fecha de siembra hasta la fecha estimada de cosecha.

### 5. Fecha Estimada de Cosecha
Se establece automáticamente al registrar un evento de tipo `SOWING`:
```
estimatedHarvestDate = timestamp + crop.estimatedGrowthDays (en días)
```
