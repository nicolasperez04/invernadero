# `generate.py` — Universal Backend Generator

> Script autónomo que lee el meta-modelo [`JSON.md`](./JSON.md) del proyecto SIGMA
> y genera un backend completo y funcional en múltiples frameworks.

---

## Índice

1. [¿Qué es?](#qué-es)
2. [Requisitos](#requisitos)
3. [Uso básico](#uso-básico)
4. [Targets disponibles](#targets-disponibles)
5. [Capas generadas](#capas-generadas)
6. [Estructura de salida](#estructura-de-salida)
7. [Mapeo de tipos](#mapeo-de-tipos)
8. [Flag `--only`](#flag---only)
9. [Reglas de negocio](#reglas-de-negocio)
10. [Extender a nuevos targets](#extender-a-nuevos-targets)
11. [Arquitectura interna](#arquitectura-interna)

---

## ¿Qué es?

`generate.py` es un **generador de backend universal** de **archivo único y cero dependencias externas**.

Lee el meta-modelo JSON embebido en [`JSON.md`](./JSON.md) — que describe entidades, DTOs,
endpoints, seguridad, reglas de negocio, etc. — y genera un backend completo listo para
ejecutarse en el framework seleccionado.

**Casos de uso:**
- Migrar SIGMA de Spring Boot (Java) a FastAPI (Python) o Express (TypeScript)
- Generar scaffolding inicial para prototipado rápido
- Mantener múltiples implementaciones sincronizadas desde un solo meta-modelo
- Automatizar la creación de APIs en nuevos lenguajes/frameworks

---

## Requisitos

| Requisito | Versión |
|---|---|
| Python | 3.10 o superior |
| Dependencias externas | **Ninguna** (solo usa `json`, `os`, `re`, `argparse` de la stdlib) |

---

## Uso básico

```bash
# Parado en el directorio doc/
cd Desktop\UNIVERSIDAD\NOVENO SEMESTRE\ELECTIVA INGENIERÍA I\back + front\proyecto> cd src/main/java/com/invernadero/proyecto/doc

# Generar backend FastAPI
python generate.py --target fastapi --output ./mi-api-fastapi

# Generar backend Express
python generate.py --target express --output ./mi-api-express

# Especificar ruta alternativa del JSON.md
python generate.py --target fastapi --output ./out --json ../otro/JSON.md

# Ver ayuda completa
python generate.py --help
```

---

## Targets disponibles

| Target | Framework | Lenguaje | ORM | Auth |
|---|---|---|---|---|
| `fastapi` | FastAPI | Python 3.10+ | SQLAlchemy 2.0 (asíncrono) | JWT via `python-jose` |
| `express` | Express.js | TypeScript 5+ | Prisma 5 | JWT via `jsonwebtoken` |

---

## Capas generadas

Cada `--target` genera 7 capas. Cada capa es una función independiente dentro del script
que puede ejecutarse de forma aislada.

| # | Capa | Descripción | FastAPI | Express |
|---|---|---|---|---|
| 1 | `entities` | Modelos de datos con campos, tipos y relaciones | `app/models/*.py` (SQLAlchemy `Base`) | `prisma/schema.prisma` + `src/models/*.ts` |
| 2 | `dtos` | Schemas de entrada/salida con validaciones | `app/schemas/*.py` (Pydantic `BaseModel`) | `src/dto/index.ts` (TypeScript interfaces) |
| 3 | `repositories` | Capa de acceso a datos con queries CRUD + queries personalizadas | `app/repositories/*.py` | `src/repositories/*.ts` |
| 4 | `services` | Lógica de negocio CRUD con `TODO` para reglas específicas | `app/services/*.py` | `src/services/*.ts` |
| 5 | `controllers` | Endpoints REST con método HTTP, path, roles, parámetros | `app/api/*.py` (FastAPI routers) | `src/routes/*.ts` + `src/controllers/*.ts` |
| 6 | `middleware` | Autenticación JWT + manejador global de errores | `app/middleware/auth.py` + `error_handler.py` | `src/middleware/auth.ts` |
| 7 | `config` | Punto de entrada, conexión DB, variables de entorno, dependencias | `app/main.py`, `app/config.py`, `app/database.py`, `requirements.txt`, `.env.example` | `src/app.ts`, `src/config.ts`, `package.json`, `tsconfig.json`, `.env.example` |

---

## Estructura de salida

### FastAPI

```
output/
├── app/
│   ├── __init__.py
│   ├── main.py                    # FastAPI app + CORS + routers
│   ├── config.py                  # Settings from env (pydantic-settings)
│   ├── database.py                # SQLAlchemy async engine + session
│   │
│   ├── models/
│   │   ├── __init__.py
│   │   ├── base.py                # DeclarativeBase
│   │   ├── user.py
│   │   ├── crop.py
│   │   ├── lot.py
│   │   ├── event.py
│   │   └── event_type.py
│   │
│   ├── schemas/                   # Pydantic models
│   │   ├── __init__.py
│   │   ├── auth.py                # AuthRequest, AuthResponse
│   │   ├── crop_request.py        # + crop_response.py
│   │   ├── event_request.py       # + event_response.py
│   │   ├── lot_request.py         # + lot_response.py
│   │   ├── user_request.py        # + user_response.py
│   │   ├── dashboard_response.py
│   │   ├── event_chart_dto.py
│   │   ├── lot_status_dto.py
│   │   ├── lot_progress_dto.py
│   │   └── lot_summary.py
│   │
│   ├── repositories/
│   │   ├── __init__.py
│   │   ├── base.py                # BaseRepository (recibe AsyncSession)
│   │   ├── crop_repository.py
│   │   ├── event_repository.py
│   │   ├── event_type_repository.py
│   │   ├── lot_repository.py
│   │   └── user_repository.py
│   │
│   ├── services/
│   │   ├── __init__.py
│   │   ├── auth_service.py        # login(email, password) -> token
│   │   ├── crop_service.py
│   │   ├── dashboard_service.py
│   │   ├── event_service.py
│   │   ├── event_type_service.py
│   │   ├── lot_service.py
│   │   └── user_service.py
│   │
│   ├── api/                       # FastAPI routers
│   │   ├── __init__.py
│   │   ├── router.py              # Agrega todos los routers al APIRouter
│   │   ├── autenticacion.py
│   │   ├── cultivos.py
│   │   ├── eventos.py
│   │   ├── tipos_de_evento.py
│   │   ├── lote.py
│   │   ├── user_management.py
│   │   └── dashboard.py
│   │
│   └── middleware/
│       ├── __init__.py
│       ├── auth.py                # role_guard() dependency
│       └── error_handler.py       # AppException + global handler
│
├── alembic.ini                    # Migration config (skeleton)
├── .env.example
└── requirements.txt
```

### Express

```
output/
├── prisma/
│   └── schema.prisma              # Database schema + relations
│
├── src/
│   ├── app.ts                     # Express app + CORS + routes
│   ├── config.ts                  # Env-based config
│   │
│   ├── types/
│   │   └── index.ts               # PaginationParams
│   │
│   ├── models/
│   │   ├── user.ts                # TypeScript interfaces
│   │   ├── crop.ts
│   │   ├── lot.ts
│   │   ├── event.ts
│   │   └── eventType.ts
│   │
│   ├── dto/
│   │   └── index.ts               # All request/response interfaces
│   │
│   ├── repositories/
│   │   ├── index.ts               # PrismaClient singleton
│   │   ├── crop_repository.ts
│   │   ├── event_repository.ts
│   │   ├── event_type_repository.ts
│   │   ├── lot_repository.ts
│   │   └── user_repository.ts
│   │
│   ├── services/
│   │   ├── index.ts
│   │   ├── auth_service.ts
│   │   ├── crop_service.ts
│   │   ├── dashboard_service.ts
│   │   ├── event_service.ts
│   │   ├── event_type_service.ts
│   │   ├── lot_service.ts
│   │   └── user_service.ts
│   │
│   ├── controllers/               # Handler functions
│   │   ├── index.ts
│   │   ├── autenticacion.ts
│   │   ├── cultivos.ts
│   │   ├── eventos.ts
│   │   ├── tipos_de_evento.ts
│   │   ├── lote.ts
│   │   ├── user_management.ts
│   │   └── dashboard.ts
│   │
│   ├── routes/                    # Express routers
│   │   ├── index.ts               # Agrega todos los routers
│   │   ├── autenticacion.ts
│   │   ├── cultivos.ts
│   │   ├── eventos.ts
│   │   ├── tipos_de_evento.ts
│   │   ├── lote.ts
│   │   ├── user_management.ts
│   │   └── dashboard.ts
│   │
│   └── middleware/
│       └── auth.ts                # authorize() guard + errorHandler()
│
├── .env
├── .env.example
├── package.json
└── tsconfig.json
```

---

## Mapeo de tipos

| Java | FastAPI (Python) | SQLAlchemy | Express (TypeScript) | Prisma |
|---|---|---|---|---|
| `Long` | `int` | `Integer` | `number` | `Int` |
| `String` | `str` | `String` | `string` | `String` |
| `Integer` | `int` | `Integer` | `number` | `Int` |
| `boolean` | `bool` | `Boolean` | `boolean` | `Boolean` |
| `Instant` | `datetime` | `DateTime(timezone=True)` | `Date` | `DateTime` |
| `double` | `float` | `Float` | `number` | `Float` |
| `Enum(Role)` | `str` | `String` | `string` | `String` |
| `List<T>` | `list[T]` | `relationship` | `T[]` | `T[]` |

---

## Flag `--only`

Genera solo las capas especificadas, útil para regenerar partes específicas
sin afectar el resto.

```bash
# Solo modelos y DTOs
python generate.py --target fastapi --output ./out --only entities,dtos

# Solo controladores y middleware
python generate.py --target express --output ./out --only controllers,middleware

# Solo configuración inicial
python generate.py --target fastapi --output ./out --only config
```

Las capas disponibles son: `entities`, `dtos`, `repositories`, `services`,
`controllers`, `middleware`, `config`. Se especifican separadas por coma.

---

## Reglas de negocio

El meta-modelo JSON define reglas de negocio en la sección `businessRules`.
El generador las incluye como **comentarios `# TODO` / `// TODO`** en los servicios
correspondientes. El desarrollador debe implementar la lógica manualmente.

**Reglas incluidas como comentarios:**

| ID | Regla | Servicio destino |
|---|---|---|
| `EVT-SEQ-001` | No cosechar sin sembrar | `EventService` |
| `EVT-SEQ-002` | No sembrar dos veces en el mismo lote | `EventService` |
| `EVT-SEQ-003` | No agregar eventos a un lote cosechado | `EventService` |
| `HRV-CALC-001` | Calcular fecha de cosecha al sembrar | `EventService` |
| `BZ-001` | Nombre de cultivo único | `CropService` |
| `BZ-002` | Email de usuario único | `UserService` |

**Estados calculados** (no almacenados en DB, documentados para implementar):

- **Estado del lote**: `CREATED` → `IN_PRODUCTION` → `FINISHED`
- **Nivel de inactividad**: `GRAY` → `GREEN` → `YELLOW` → `RED`
- **Progreso del cultivo**: Porcentaje 0-100 basado en días transcurridos

---

## Extender a nuevos targets

Agregar un nuevo framework requiere **3 pasos** en `generate.py`:

### 1. Agregar type mapping (sección 1)

```python
JAVA_TO_DJANGO = {
    "Long": "models.BigIntegerField",
    "String": "models.CharField",
    ...
}
TYPE_MAPS["django"] = JAVA_TO_DJANGO
```

### 2. Agregar config del framework (sección 3)

```python
FRAMEWORKS["django"] = {
    "ext": ".py",
    "naming": "snake",
    ...
}
```

### 3. Agregar lógica dentro de cada generador

Dentro de cada función generadora, agregar un bloque `elif target == "django":`
con el template correspondiente (f-strings multiline, sin Jinja2).

**Ejemplo mínimo dentro de `generate_entities`:**

```python
elif target == "django":
    for ent in entities:
        fields = []
        for f in ent.get("fields", []):
            if f.get("pk"): continue
            dj_type = JAVA_TO_DJANGO.get(f["type"], "models.TextField")
            opts = []
            if not f.get("nullable", True):
                opts.append("blank=False")
            if f.get("unique"):
                opts.append("unique=True")
            opts_str = ", ".join(opts)
            fields.append(f"    {f['name']} = {dj_type}({opts_str})")
        code = f"""\
from django.db import models

class {ent['name']}(models.Model):
{chr(10).join(fields)}
"""
        _write(f"{root}/sigma/models.py", code, mode="a")
```

---

## Arquitectura interna

```
generate.py  (~1060 líneas, 0 dependencias externas)
├── 1) TYPE MAPS           (~50 lines)   Java → target type dictionaries
├── 2) NAMING HELPERS      (~25 lines)   snake_case, PascalCase, camelCase
├── 3) JSON READER         (~15 lines)   Extrae bloque JSON de JSON.md
├── 4) FILE WRITER HELPER  (~15 lines)   os.makedirs + open().write
├── 5) GENERATORS (7)      (~750 lines)
│   ├── generate_entities()
│   ├── generate_dtos()
│   ├── generate_repositories()
│   ├── generate_services()
│   ├── generate_controllers()
│   ├── generate_middleware()
│   └── generate_config()
├── 6) DISPATCHER          (~10 lines)   GENERATORS dict
└── 7) CLI + MAIN          (~40 lines)   argparse + orchestrator
```

**Principios de diseño:**

- **Archivo único** — sin módulos, paquetes, ni dependencias. Copiar y ejecutar.
- **Templates inline** — cada generador contiene sus templates como f-strings multiline.
  No se usa Jinja2 ni sistema de templates externo.
- **Sin clases** — funciones independientes. Cada generador recibe `(data, target, root)`
  y escribe archivos.
- **Determinístico** — misma entrada produce exactamente la misma salida siempre.
- **Idempotente** — ejecutar dos veces genera el mismo resultado (sin UUIDs, sin timestamps
  variables en el código generado).
