# SIGMA - Sistema de Gestión de Invernaderos

> API REST para la gestión integral de un sistema de invernaderos. Administra cultivos, lotes, eventos y usuarios con autenticación JWT.

## Stack Tecnológico

| Componente       | Tecnología                    |
|------------------|-------------------------------|
| Backend          | Spring Boot 3.5.8             |
| Lenguaje         | Java 21                       |
| Base de datos    | PostgreSQL                    |
| ORM              | Hibernate / Spring Data JPA   |
| Autenticación    | JWT (jjwt 0.12.6)             |
| API Docs         | SpringDoc OpenAPI 2.7.0       |
| Compilación      | Maven 3.9+                    |

## Requisitos

- **Java 21** (JDK)
- **Maven 3.9+**
- **PostgreSQL 14+**
- **Node.js 20+** (solo si se ejecuta el frontend)

## Instalación Local

### 1. Clonar y configurar

```bash
# Clonar el repositorio
git clone <repo-url>
cd proyecto

# Crear archivo .env en la raíz (o copiar desde .env.example si existe)
cp .env.example .env
```

### 2. Variables de entorno

```env
# Base de datos
DB_HOST=localhost
DB_PORT=5432
DB_NAME=invernadero
DB_USER=admin
DB_PASSWORD=admin

# JWT (mínimo 64 caracteres, Base64)
JWT_SECRET=E54791C31B99A58F25677B21FAECD57AB89CDEF1234567890ABCDEF12345678

# Frontend (Angular)
FRONTEND_URL=http://localhost:4200

# Servidor
PORT=8080
```

### 3. Base de datos

```sql
-- Crear base de datos PostgreSQL
CREATE DATABASE invernadero;

-- Crear usuario (opcional, usar admin/admin si se prefiere)
CREATE USER admin WITH PASSWORD 'admin';
GRANT ALL PRIVILEGES ON DATABASE:invernadero TO admin;
```

### 4. Ejecutar

```powershell
# Windows
.\mvnw.cmd spring-boot:run

# Linux/macOS
./mvnw spring-boot:run
```

La aplicación estará disponible en `http://localhost:8080`.

## Acceso a la Documentación

Una vez ejecutando:

| Recurso          | URL                                    |
|------------------|----------------------------------------|
| Swagger UI       | http://localhost:8080/swagger-ui.html  |
| OpenAPI JSON     | http://localhost:8080/api-docs         |
| Health Check     | http://localhost:8080/api/health       |

## Endpoints Públicos

| Método | Path                    | Descripción                     |
|--------|-------------------------|---------------------------------|
| POST   | /api/auth/login         | Iniciar sesión                  |
| POST   | /api/auth/register      | Registrar usuario               |
| GET    | /api/health             | Health check                    |

Todos los demás endpoints requieren autenticación JWT.

## Estructura del Proyecto

```
src/main/java/com/invernadero/proyecto/
├── Config/          # Configuración (CORS, OpenAPI, Locale)
├── Controller/      # Controladores REST
├── Dto/             # Data Transfer Objects (Request/Response)
├── Entity/          # Entidades JPA
├── Exception/       # Manejo global de errores
├── Mapper/          # MapStruct (Entity <-> DTO)
├── Repository/      # Repositorios JPA
├── Security/        # JWT, filtros, configuración de seguridad
└── Service/         # Lógica de negocio
```

## Comandos Maven

```powershell
# Ejecutar tests
.\mvnw.cmd test

# Compilar
.\mvnw.cmd clean compile

# Empaquetar JAR
.\mvnw.cmd clean package -DskipTests

# Ejecutar un test específico
.\mvnw.cmd test -Dtest=UserServiceTest
```

## Perfiles de Spring

| Perfil    | Uso                           | Config principal             |
|-----------|-------------------------------|------------------------------|
| `dev`     | Desarrollo local              | application-dev.properties   |
| `prod`    | Producción                    | application-prod.properties  |
| `test`    | Tests unitarios (H2 en memoria) | application.properties    |

Activar perfil: `spring.profiles.active=dev` en `application.properties`.

## Frontend

El frontend Angular está en el directorio hermano `proyecto-front/`.

```bash
cd ../proyecto-front
npm install
ng serve
# http://localhost:4200
```

## Roles de Usuario

| Rol        | Descripción                              |
|------------|------------------------------------------|
| `ADMIN`    | Acceso completo al sistema               |
| `OPERATOR` | Gestiona cultivos, lotes y eventos      |
| `VIEWER`   | Solo lectura                             |

## Changelog rápido

### v0.0.1-SNAPSHOT (actual)
- API REST completa con autenticación JWT
- CRUD de Usuarios, Cultivos, Lotes, Eventos, Tipos de Evento
- Dashboard con métricas y gráficos
- Documentación Swagger/OpenAPI completa
- CI/CD con GitHub Actions
- Tests unitarios y de integración (111 tests)