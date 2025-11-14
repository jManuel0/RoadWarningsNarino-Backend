# Guía de Validaciones de DTOs

Este documento describe todas las validaciones implementadas en los DTOs del backend.

## 1. RegisterRequest (Registro de Usuarios)

### Validaciones

**Username:**
- ✅ Obligatorio
- ✅ Entre 3 y 20 caracteres
- ✅ Solo letras, números, guiones (-) y guiones bajos (_)

**Email:**
- ✅ Obligatorio
- ✅ Formato de email válido
- ✅ Máximo 100 caracteres

**Password:**
- ✅ Obligatorio
- ✅ Entre 8 y 100 caracteres
- ✅ Debe contener al menos:
  - Una letra mayúscula
  - Una letra minúscula
  - Un número

### Ejemplos

#### ✅ Válido:
```json
{
  "username": "juan_manuel",
  "email": "juan@example.com",
  "password": "MiPassword123"
}
```

#### ❌ Inválido (username muy corto):
```json
{
  "username": "jm",
  "email": "juan@example.com",
  "password": "MiPassword123"
}
```

**Respuesta de error:**
```json
{
  "timestamp": "2025-11-13T15:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Error de validación en los campos",
  "errors": {
    "username": "El username debe tener entre 3 y 20 caracteres"
  }
}
```

#### ❌ Inválido (contraseña débil):
```json
{
  "username": "juanmanuel",
  "email": "juan@example.com",
  "password": "password"
}
```

**Respuesta de error:**
```json
{
  "timestamp": "2025-11-13T15:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Error de validación en los campos",
  "errors": {
    "password": "La contraseña debe contener al menos una mayúscula, una minúscula y un número"
  }
}
```

#### ❌ Inválido (email inválido):
```json
{
  "username": "juanmanuel",
  "email": "juan@",
  "password": "MiPassword123"
}
```

**Respuesta de error:**
```json
{
  "timestamp": "2025-11-13T15:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Error de validación en los campos",
  "errors": {
    "email": "El email debe tener un formato válido"
  }
}
```

---

## 2. LoginRequest (Inicio de Sesión)

### Validaciones

**Username (o Email):**
- ✅ Obligatorio

**Password:**
- ✅ Obligatorio

### Ejemplos

#### ✅ Válido:
```json
{
  "username": "juanmanuel",
  "password": "MiPassword123"
}
```

#### ❌ Inválido (campos vacíos):
```json
{
  "username": "",
  "password": ""
}
```

**Respuesta de error:**
```json
{
  "timestamp": "2025-11-13T15:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Error de validación en los campos",
  "errors": {
    "username": "El username o email es obligatorio",
    "password": "La contraseña es obligatoria"
  }
}
```

---

## 3. GasStationRequestDTO (Estaciones de Servicio)

### Validaciones

**Name:**
- ✅ Obligatorio
- ✅ Máximo 100 caracteres

**Brand:**
- ✅ Opcional
- ✅ Máximo 50 caracteres

**Latitude:**
- ✅ Obligatorio
- ✅ Entre -90.0 y 90.0

**Longitude:**
- ✅ Obligatorio
- ✅ Entre -180.0 y 180.0

**Address:**
- ✅ Obligatorio
- ✅ Máximo 200 caracteres

**Municipality:**
- ✅ Obligatorio
- ✅ Máximo 100 caracteres

**PhoneNumber:**
- ✅ Opcional
- ✅ Debe tener 7 o 10 dígitos

**GasolinePrice:**
- ✅ Opcional
- ✅ Debe ser mayor a 0

**DieselPrice:**
- ✅ Opcional
- ✅ Debe ser mayor a 0

**OpeningTime / ClosingTime:**
- ✅ Opcional
- ✅ Formato HH:mm (ej: 06:00, 18:30)

### Ejemplos

#### ✅ Válido:
```json
{
  "name": "Estación Terpel Centro",
  "brand": "Terpel",
  "latitude": 1.2136,
  "longitude": -77.2811,
  "address": "Calle 18 # 25-40",
  "municipality": "Pasto",
  "phoneNumber": "3001234567",
  "hasGasoline": true,
  "hasDiesel": true,
  "gasolinePrice": 10500,
  "dieselPrice": 9800,
  "isOpen24Hours": false,
  "openingTime": "06:00",
  "closingTime": "22:00",
  "isAvailable": true
}
```

#### ❌ Inválido (latitud fuera de rango):
```json
{
  "name": "Estación Terpel",
  "latitude": 95.0,
  "longitude": -77.2811,
  "address": "Calle 18 # 25-40",
  "municipality": "Pasto"
}
```

**Respuesta de error:**
```json
{
  "timestamp": "2025-11-13T15:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Error de validación en los campos",
  "errors": {
    "latitude": "La latitud debe estar entre -90 y 90"
  }
}
```

#### ❌ Inválido (teléfono inválido):
```json
{
  "name": "Estación Terpel",
  "latitude": 1.2136,
  "longitude": -77.2811,
  "address": "Calle 18 # 25-40",
  "municipality": "Pasto",
  "phoneNumber": "123"
}
```

**Respuesta de error:**
```json
{
  "timestamp": "2025-11-13T15:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Error de validación en los campos",
  "errors": {
    "phoneNumber": "El teléfono debe tener 7 o 10 dígitos"
  }
}
```

#### ❌ Inválido (horario con formato incorrecto):
```json
{
  "name": "Estación Terpel",
  "latitude": 1.2136,
  "longitude": -77.2811,
  "address": "Calle 18 # 25-40",
  "municipality": "Pasto",
  "openingTime": "6:00",
  "closingTime": "25:00"
}
```

**Respuesta de error:**
```json
{
  "timestamp": "2025-11-13T15:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Error de validación en los campos",
  "errors": {
    "openingTime": "El horario de apertura debe tener formato HH:mm",
    "closingTime": "El horario de cierre debe tener formato HH:mm"
  }
}
```

---

## 4. AlertaRequestDTO (Ya implementado previamente)

Las validaciones para AlertaRequestDTO ya estaban implementadas:

- **Type:** Obligatorio
- **Title:** Obligatorio, no vacío
- **Latitude/Longitude:** Validaciones de rango
- **Severity:** Obligatorio

---

## Formato General de Respuestas de Error

Todas las respuestas de error de validación tienen la siguiente estructura:

```json
{
  "timestamp": "2025-11-13T15:30:00.123456",
  "status": 400,
  "error": "Bad Request",
  "message": "Error de validación en los campos",
  "errors": {
    "campo1": "Mensaje de error del campo 1",
    "campo2": "Mensaje de error del campo 2"
  }
}
```

## Códigos de Estado HTTP

- **400 Bad Request**: Error de validación en los datos de entrada
- **401 Unauthorized**: Token JWT no proporcionado o inválido
- **403 Forbidden**: Sin permisos para realizar la acción
- **404 Not Found**: Recurso no encontrado
- **500 Internal Server Error**: Error del servidor

## Pruebas con cURL

### Registro con validaciones:
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "jm",
    "email": "invalido",
    "password": "123"
  }'
```

### Crear estación con validaciones:
```bash
curl -X POST http://localhost:8080/api/gas-stations \
  -H "Authorization: Bearer TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "",
    "latitude": 95.0,
    "longitude": -200.0,
    "address": "",
    "municipality": ""
  }'
```
