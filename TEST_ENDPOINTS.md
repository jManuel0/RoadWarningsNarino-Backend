# Guía para Probar los Endpoints con Autenticación

## Configuración Previa

Antes de probar los endpoints, asegúrate de tener configuradas las siguientes variables de entorno en Render (o en tu entorno local):

```bash
DATABASE_URL=jdbc:postgresql://tu-host:5432/tu-db
DB_USER=tu-usuario
DB_PASSWORD=tu-password
JWT_SECRET=tu-secreto-jwt-de-al-menos-32-caracteres-aqui
JWT_EXPIRATION=86400000
```

## Flujo de Prueba

### 1. Registrar un Usuario

```bash
POST /api/auth/register
Content-Type: application/json

{
  "username": "juanmanuel",
  "email": "juan@example.com",
  "password": "MiPassword123"
}
```

**Respuesta:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqdWFubWFudWVsIi..."
}
```

### 2. Iniciar Sesión

```bash
POST /api/auth/login
Content-Type: application/json

{
  "username": "juanmanuel",
  "password": "MiPassword123"
}
```

**O con email:**
```bash
{
  "username": "juan@example.com",
  "password": "MiPassword123"
}
```

**Respuesta:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqdWFubWFudWVsIi..."
}
```

### 3. Crear una Alerta (Requiere Autenticación)

```bash
POST /alert
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqdWFubWFudWVsIi...
Content-Type: application/json

{
  "type": "ACCIDENTE",
  "title": "Accidente en la vía Panamericana",
  "description": "Colisión entre dos vehículos",
  "latitude": 1.2136,
  "longitude": -77.2811,
  "location": "Pasto, Nariño",
  "severity": "HIGH"
}
```

**Respuesta:**
```json
{
  "id": 1,
  "type": "ACCIDENTE",
  "title": "Accidente en la vía Panamericana",
  "description": "Colisión entre dos vehículos",
  "latitude": 1.2136,
  "longitude": -77.2811,
  "location": "Pasto, Nariño",
  "severity": "HIGH",
  "status": "ACTIVE",
  "username": "juanmanuel",
  "upvotes": 0,
  "downvotes": 0,
  "createdAt": "2025-11-13T15:30:00"
}
```

### 4. Obtener Todas las Alertas (Requiere Autenticación)

```bash
GET /alert
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqdWFubWFudWVsIi...
```

### 5. Obtener Alertas Activas (Requiere Autenticación)

```bash
GET /alert/active
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqdWFubWFudWVsIi...
```

### 6. Obtener Alerta por ID (Requiere Autenticación)

```bash
GET /alert/1
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqdWFubWFudWVsIi...
```

### 7. Obtener Alertas Cercanas (Requiere Autenticación)

```bash
GET /alert/nearby?latitude=1.2136&longitude=-77.2811&radius=10.0
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqdWFubWFudWVsIi...
```

### 8. Actualizar una Alerta (Solo el creador puede actualizarla)

```bash
PUT /alert/1
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqdWFubWFudWVsIi...
Content-Type: application/json

{
  "type": "ACCIDENTE",
  "title": "Accidente RESUELTO en la vía Panamericana",
  "description": "Ya fue atendido por las autoridades",
  "latitude": 1.2136,
  "longitude": -77.2811,
  "location": "Pasto, Nariño",
  "severity": "MEDIUM"
}
```

### 9. Cambiar Estado de una Alerta (Requiere Autenticación)

```bash
PATCH /alert/1/status?status=RESOLVED
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqdWFubWFudWVsIi...
```

### 10. Eliminar una Alerta (Solo el creador puede eliminarla)

```bash
DELETE /alert/1
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqdWFubWFudWVsIi...
```

## Endpoints Públicos (No Requieren Autenticación)

### Health Check

```bash
GET /public/health
```

```bash
GET /ping
```

```bash
GET /
```

### Swagger UI

```bash
GET /swagger-ui.html
```

## Códigos de Error Esperados

- **401 Unauthorized**: No se proporcionó token JWT o el token es inválido
- **403 Forbidden**: No tienes permiso para realizar esta acción (ej. intentar borrar una alerta de otro usuario)
- **404 Not Found**: El recurso no existe
- **400 Bad Request**: Datos de entrada inválidos

## Ejemplo con cURL

### Registrar usuario
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"juanmanuel","email":"juan@example.com","password":"MiPassword123"}'
```

### Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"juanmanuel","password":"MiPassword123"}'
```

### Crear alerta (reemplaza TOKEN con el token obtenido)
```bash
curl -X POST http://localhost:8080/alert \
  -H "Authorization: Bearer TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "type":"ACCIDENTE",
    "title":"Accidente en vía",
    "description":"Colisión",
    "latitude":1.2136,
    "longitude":-77.2811,
    "location":"Pasto",
    "severity":"HIGH"
  }'
```

### Obtener alertas
```bash
curl -X GET http://localhost:8080/alert \
  -H "Authorization: Bearer TOKEN"
```

## Tipos de Alerta Disponibles

- DERRUMBE
- PROTESTA
- ACCIDENTE
- TRAFICO_PESADO
- VIA_CERRADA
- POLICIA
- TRANSITO
- EJERCITO
- SEMAFORO_DANADO
- OBRAS_VIALES
- INUNDACION
- NEBLINA
- VEHICULO_VARADO
- ANIMALES_EN_VIA
- GASOLINA
- OTROS

## Niveles de Severidad

- LOW
- MEDIUM
- HIGH
- CRITICAL

## Estados de Alerta

- ACTIVE
- RESOLVED
- EXPIRED
