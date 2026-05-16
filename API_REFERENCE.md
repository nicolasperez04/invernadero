# Referencia Rápida de API

Base URL: `http://localhost:8080/api`

> Todos los endpoints requieren autenticación JWT excepto los marcados como **PÚBLICO**.
> Uso: `Authorization: Bearer <token>` en el header.

---

## Autenticación *(PÚBLICO)*

| Método | Path                  | Descripción                       | Auth  |
|--------|-----------------------|-----------------------------------|-------|
| POST   | `/auth/login`         | Iniciar sesión                    | No    |
| POST   | `/auth/register`      | Registrar nuevo usuario           | No    |

---

## Dashboard

| Método | Path            | Descripción                          | Auth  |
|--------|-----------------|--------------------------------------|-------|
| GET    | `/dashboard`    | Métricas generales y resúmenes       | Sí    |

**Respuesta incluye:**
- Total de lotes, cultivos y eventos
- Lotes por estado (active/inactive/completed)
- Gráfico de eventos por día (últimos 7 días)
- Progreso de lotes activos (días transcurridos vs estimados)

---

## Cultivos (Crops)

| Método | Path             | Descripción              | Auth  |
|--------|------------------|--------------------------|-------|
| GET    | `/crops`         | Listar todos los cultivos | Sí    |
| GET    | `/crops/{id}`    | Obtener cultivo por ID   | Sí    |
| POST   | `/crops`         | Crear nuevo cultivo      | Sí    |
| PUT    | `/crops/{id}`    | Actualizar cultivo       | Sí    |
| DELETE | `/crops/{id}`    | Eliminar cultivo         | Sí    |

---

## Lotes (Lots)

| Método | Path                          | Descripción                        | Auth  |
|--------|-------------------------------|------------------------------------|-------|
| GET    | `/lots`                       | Listar todos los lotes             | Sí    |
| GET    | `/lots/{id}`                  | Obtener lote por ID                | Sí    |
| POST   | `/lots`                       | Crear nuevo lote                   | Sí    |
| PUT    | `/lots/{id}`                  | Actualizar lote                    | Sí    |
| DELETE | `/lots/{id}`                  | Eliminar lote                      | Sí    |
| GET    | `/lots/{id}/status`           | Estado detallado del lote          | Sí    |
| GET    | `/lots/{id}/progress`         | Progreso del lote vs fecha cosecha | Sí    |
| GET    | `/lots/active`                | Lotes activos (sin fecha fin)      | Sí    |
| GET    | `/lots/status/summary`        | Resumen de estados                 | Sí    |
| GET    | `/lots/alert/inactive`        | Lotes inactivos ( umbral superado) | Sí    |

---

## Eventos (Events)

| Método | Path                         | Descripción                    | Auth  |
|--------|------------------------------|--------------------------------|-------|
| GET    | `/events`                    | Listar todos los eventos       | Sí    |
| GET    | `/events/{id}`               | Obtener evento por ID          | Sí    |
| POST   | `/events`                    | Registrar nuevo evento         | Sí    |
| PUT    | `/events/{id}`               | Actualizar evento              | Sí    |
| DELETE | `/events/{id}`               | Eliminar evento                | Sí    |
| GET    | `/events/chart/daily`         | Eventos por día (últimos 7 días) | Sí  |
| GET    | `/events/lot/{lotId}`        | Eventos de un lote específico  | Sí    |

---

## Tipos de Evento (Event Types)

| Método | Path                         | Descripción                   | Auth  |
|--------|------------------------------|-------------------------------|-------|
| GET    | `/event-types`               | Listar todos los tipos        | Sí    |
| GET    | `/event-types/{id}`          | Obtener tipo por ID           | Sí    |
| POST   | `/event-types`               | Crear nuevo tipo              | Sí    |
| PUT    | `/event-types/{id}`          | Actualizar tipo               | Sí    |
| DELETE | `/event-types/{id}`          | Eliminar tipo                 | Sí    |

---

## Usuarios (Users)

| Método | Path             | Descripción              | Auth  |
|--------|------------------|--------------------------|-------|
| GET    | `/users`         | Listar todos los usuarios | Sí    |
| GET    | `/users/{id}`    | Obtener usuario por ID   | Sí    |
| POST   | `/users`         | Crear nuevo usuario      | Sí    |
| PUT    | `/users/{id}`    | Actualizar usuario       | Sí    |
| DELETE | `/users/{id}`    | Eliminar usuario (soft)  | Sí    |

---

## Health Check *(PÚBLICO)*

| Método | Path        | Descripción      | Auth  |
|--------|-------------|------------------|-------|
| GET    | `/health`   | Estado del API   | No    |

---

## Códigos de Respuesta HTTP

| Código | Significado                                   |
|--------|-----------------------------------------------|
| 200    | OK (operación exitosa)                        |
| 201    | Creado (recurso nuevo)                        |
| 400    | Bad Request (validación fallida)              |
| 401    | Unauthorized (token inválido o ausente)      |
| 403    | Forbidden (sin permisos para el recurso)      |
| 404    | Not Found (recurso no existe)                 |
| 409    | Conflict (dato duplicado, ej: email)          |
| 500    | Internal Server Error                         |

## Formato de Fechas

Todas las fechas se envían en formato **ISO 8601** con zona horaria:

```json
"startDate": "2025-01-15T00:00:00Z"
"timestamp": "2025-01-15T10:30:00Z"
```

## Ejemplo de Solicitud Autenticada

```bash
curl -X GET http://localhost:8080/api/crops \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -H "Content-Type: application/json"
```

## Roles y Permisos

| Endpoint              | ADMIN | OPERATOR | VIEWER |
|-----------------------|-------|----------|--------|
| GET (lectura)         | ✓     | ✓        | ✓      |
| POST/PUT/DELETE       | ✓     | ✓        | ✗      |
| Gestión de usuarios   | ✓     | ✗        | ✗      |