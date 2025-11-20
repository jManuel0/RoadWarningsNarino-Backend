# 📋 Guía Completa del Flujo de Registro y Verificación

## RoadWarnings Nariño - Sistema de Autenticación

---

## 🎯 Resumen del Flujo

```
┌─────────────┐
│   Usuario   │
│  se registra│
└──────┬──────┘
       │
       ▼
┌─────────────────────────────────┐
│  1. POST /api/auth/register     │
│  - Valida datos                 │
│  - Crea usuario (emailVerified=false) │
│  - Genera verification token    │
│  - Envía email                  │
│  - Devuelve JWT + RefreshToken  │
└──────┬──────────────────────────┘
       │
       ▼
┌─────────────────────────────────┐
│  2. Usuario recibe email        │
│  - Link con token único         │
│  - Válido por 24 horas          │
└──────┬──────────────────────────┘
       │
       ▼
┌─────────────────────────────────┐
│  3. Usuario hace clic en link   │
│  GET /api/auth/verify-email?token=xxx │
│  - Valida token                 │
│  - Marca emailVerified=true     │
│  - Elimina token usado          │
└──────┬──────────────────────────┘
       │
       ▼
┌─────────────────────────────────┐
│  4. POST /api/auth/login        │
│  - Valida emailVerified=true    │
│  - Autentica usuario            │
│  - Devuelve JWT + RefreshToken  │
└──────┬──────────────────────────┘
       │
       ▼
┌─────────────────────────────────┐
│  Usuario autenticado ✅         │
│  Puede usar la aplicación       │
└─────────────────────────────────┘
```

---

## 📝 Paso a Paso Detallado

### PASO 1: Registro de Usuario

#### Request

**Endpoint**: `POST /api/auth/register`

**Headers**:
```
Content-Type: application/json
```

**Body**:
```json
{
  "username": "juan_manuel",
  "email": "juan@example.com",
  "password": "Password123"
}
```

#### Validaciones

El backend valida automáticamente:

**Username**:
- ✅ Mínimo 3 caracteres, máximo 20
- ✅ Solo letras, números, guiones y guiones bajos
- ✅ Único (no puede existir otro usuario con el mismo username)
- ❌ No puede estar vacío
- ❌ No puede tener espacios o caracteres especiales

Ejemplos:
- ✅ `juan_manuel`
- ✅ `user-123`
- ✅ `JohnDoe2024`
- ❌ `ju` (muy corto)
- ❌ `juan manuel` (tiene espacio)
- ❌ `user@123` (tiene @)

**Email**:
- ✅ Formato válido de email
- ✅ Máximo 100 caracteres
- ✅ Único (no puede existir otro usuario con el mismo email)
- ❌ No puede estar vacío

Ejemplos:
- ✅ `juan@example.com`
- ✅ `usuario.test+tag@dominio.co`
- ❌ `juanexample.com` (falta @)
- ❌ `@example.com` (falta parte local)

**Password**:
- ✅ Mínimo 8 caracteres, máximo 100
- ✅ Debe contener al menos:
  - Una letra mayúscula (A-Z)
  - Una letra minúscula (a-z)
  - Un dígito (0-9)
- ❌ No puede estar vacío

Ejemplos:
- ✅ `Password123`
- ✅ `MiClave2024!`
- ✅ `Secure1Pass`
- ❌ `password` (falta mayúscula y número)
- ❌ `PASSWORD123` (falta minúscula)
- ❌ `Pass1` (muy corto)

#### Response Exitosa

**Status**: `200 OK`

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
  "expiresIn": 3600,
  "username": "juan_manuel"
}
```

**Campos**:
- `token`: JWT access token (válido por 1 hora)
- `refreshToken`: Token para renovar el access token (válido por 7 días)
- `expiresIn`: Tiempo de expiración del access token en segundos
- `username`: Nombre de usuario registrado

#### Errores Posibles

**400 Bad Request** - Username ya existe:
```json
{
  "message": "El username ya está en uso"
}
```

**400 Bad Request** - Email ya registrado:
```json
{
  "message": "El email ya está registrado"
}
```

**400 Bad Request** - Validación fallida:
```json
{
  "message": "Usuario o correo ya están en uso o los datos son inválidos."
}
```

#### Qué pasa internamente

1. **Validación**: Spring Boot valida los datos usando las anotaciones `@Valid`
2. **Verificación de unicidad**: Se consulta la base de datos para verificar que username y email no existan
3. **Encriptación de contraseña**: La contraseña se encripta con BCrypt
4. **Creación de usuario**: Se crea un registro en la tabla `users` con:
   - `emailVerified = false`
   - `isActive = true`
   - `role = USER`
   - `createdAt = now()`
5. **Generación de token de verificación**: Se crea un token UUID único válido por 24 horas
6. **Envío de email asíncrono**: Se envía un email de verificación (si SMTP está configurado)
7. **Generación de JWT**: Se crea un access token y un refresh token
8. **Respuesta**: Se devuelven los tokens al cliente

---

### PASO 2: Verificación de Email

#### Email Recibido

El usuario recibe un email con este contenido:

**De**: `noreply@roadwarnings.com` (o el email configurado en `MAIL_USERNAME`)
**Para**: `juan@example.com`
**Asunto**: Verifica tu correo electrónico

**Contenido**:
```
Hola juan_manuel,

Gracias por registrarte en RoadWarnings Nariño.

Por favor verifica tu correo haciendo clic en el siguiente enlace:

https://road-warnings-narino-frontend.vercel.app/verify-email?token=abc123def456...

Este enlace expira en 24 horas.

Saludos,
El equipo de RoadWarnings Nariño
```

#### Click en el Link

Cuando el usuario hace clic en el link, el frontend hace una request:

**Endpoint**: `GET /api/auth/verify-email?token={token}`

**Headers**: Ninguno requerido

**Query Parameters**:
- `token`: El token UUID recibido en el email

#### Response Exitosa

**Status**: `200 OK`

```
Correo verificado correctamente.
```

#### Errores Posibles

**400 Bad Request** - Token inválido:
```
Token de verificación inválido.
```

**400 Bad Request** - Token expirado:
```
El enlace de verificación ha expirado.
```

#### Qué pasa internamente

1. **Búsqueda del token**: Se busca el token en la tabla `email_verification_tokens`
2. **Validación de expiración**: Se verifica que `expiresAt > now()`
3. **Actualización del usuario**: Se marca `emailVerified = true` en la tabla `users`
4. **Eliminación del token**: Se elimina el token usado de la base de datos
5. **Respuesta**: Se confirma la verificación exitosa

---

### PASO 3: Login

Una vez verificado el email, el usuario puede hacer login.

#### Request

**Endpoint**: `POST /api/auth/login`

**Headers**:
```
Content-Type: application/json
```

**Body** (con username):
```json
{
  "username": "juan_manuel",
  "password": "Password123"
}
```

O **Body** (con email):
```json
{
  "username": "juan@example.com",
  "password": "Password123"
}
```

**Nota**: El campo se llama `username` pero acepta tanto username como email.

#### Response Exitosa

**Status**: `200 OK`

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "g58bc20c-69dd-5483-b678-1f13c3d4e590",
  "expiresIn": 3600,
  "username": "juan_manuel"
}
```

#### Errores Posibles

**400 Bad Request** - Email no verificado:
```json
{
  "message": "Debes verificar tu correo electrónico antes de iniciar sesión."
}
```

**401 Unauthorized** - Credenciales incorrectas:
```json
{
  "message": "Usuario o contraseña incorrectos"
}
```

**404 Not Found** - Usuario no encontrado:
```json
{
  "message": "Usuario no encontrado: juan_manuel"
}
```

#### Qué pasa internamente

1. **Búsqueda del usuario**: Se busca por email o username
2. **Validación de verificación**: Se verifica que `emailVerified = true`
3. **Validación de estado**: Se verifica que `isActive = true`
4. **Autenticación**: Spring Security valida la contraseña con BCrypt
5. **Generación de tokens**: Se crean nuevo access token y refresh token
6. **Respuesta**: Se devuelven los tokens

---

### PASO 4: Uso del Access Token

Una vez autenticado, el usuario usa el access token en cada request.

#### Request con Autenticación

**Headers**:
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Ejemplo** - Crear una alerta:
```
POST /api/alert
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json

{
  "title": "Derrumbe en vía Pasto-Tumaco",
  "description": "Vía bloqueada completamente",
  "latitude": 1.2136,
  "longitude": -77.2817,
  "severity": "HIGH"
}
```

#### Validación del Token

En cada request protegido:

1. **Extracción**: `JwtAuthenticationFilter` extrae el token del header `Authorization`
2. **Validación**: `JwtService` valida:
   - Firma del token (usando `JWT_SECRET`)
   - Expiración (1 hora desde creación)
   - Formato correcto
3. **Carga de usuario**: `CustomUserDetailsService` carga los datos del usuario
4. **Verificación de estado**: Se verifica que `emailVerified = true` y `isActive = true`
5. **Autenticación**: Se establece el contexto de seguridad
6. **Ejecución**: El controller procesa la request con el usuario autenticado

---

### PASO 5: Renovar Access Token

Cuando el access token expira (después de 1 hora), el frontend puede renovarlo sin que el usuario tenga que hacer login de nuevo.

#### Request

**Endpoint**: `POST /api/auth/refresh`

**Headers**:
```
Content-Type: application/json
```

**Body**:
```json
{
  "refreshToken": "f47ac10b-58cc-4372-a567-0e02b2c3d479"
}
```

#### Response Exitosa

**Status**: `200 OK`

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
  "expiresIn": 3600,
  "username": "juan_manuel"
}
```

**Nota**: El `refreshToken` NO cambia, solo se genera un nuevo `token` (access token).

#### Errores Posibles

**400 Bad Request** - Token inválido o expirado:
```json
{
  "message": "Refresh token inválido"
}
```

#### Qué pasa internamente

1. **Búsqueda del token**: Se busca en la tabla `refresh_tokens`
2. **Validaciones**:
   - Token existe
   - No está revocado (`revoked = false`)
   - No está expirado (`expiryDate > now()`)
3. **Carga del usuario**: Se obtiene el usuario asociado al token
4. **Generación de nuevo access token**: Se crea un nuevo JWT
5. **Respuesta**: Se devuelve el nuevo access token (el refresh token sigue igual)

---

### PASO 6: Logout

Cuando el usuario cierra sesión, se revoca el refresh token.

#### Request

**Endpoint**: `POST /api/auth/logout`

**Headers**:
```
Content-Type: application/json
```

**Body**:
```json
{
  "refreshToken": "f47ac10b-58cc-4372-a567-0e02b2c3d479"
}
```

#### Response Exitosa

**Status**: `200 OK`

```
Sesión cerrada correctamente
```

#### Qué pasa internamente

1. **Búsqueda del token**: Se busca en la tabla `refresh_tokens`
2. **Revocación**: Se actualiza:
   - `revoked = true`
   - `revokedAt = now()`
3. **Respuesta**: Se confirma el logout

**Importante**: El access token (JWT) sigue siendo válido hasta que expire (1 hora). Por seguridad, el frontend debe eliminarlo del localStorage/sessionStorage.

---

## 🔒 Seguridad Implementada

### Contraseñas

- ✅ Encriptadas con BCrypt (no se almacenan en texto plano)
- ✅ Validación de complejidad en frontend y backend
- ✅ Nunca se devuelven en responses

### JWT Tokens

- ✅ Firmados con HS256 y secret key de 256+ bits
- ✅ Expiración de 1 hora
- ✅ Incluyen solo información no sensible (username)
- ✅ Validados en cada request

### Refresh Tokens

- ✅ Almacenados en base de datos
- ✅ Pueden ser revocados
- ✅ Tienen expiración de 7 días
- ✅ Limpieza automática de tokens expirados (diariamente a las 3 AM)

### Email Verification

- ✅ Tokens únicos (UUID)
- ✅ Expiración de 24 horas
- ✅ Un solo uso (se eliminan después de verificar)
- ✅ No se puede hacer login sin verificar email

### CORS

- ✅ Configurado para permitir solo orígenes específicos
- ✅ Soporta credenciales (cookies, auth headers)
- ✅ Maneja correctamente requests preflight (OPTIONS)

---

## 📊 Esquema de Base de Datos

### Tabla: `users`

```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(20) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,  -- BCrypt hash
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    is_active BOOLEAN NOT NULL DEFAULT true,
    email_verified BOOLEAN NOT NULL DEFAULT false,
    preferred_theme VARCHAR(20) DEFAULT 'light',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

### Tabla: `email_verification_tokens`

```sql
CREATE TABLE email_verification_tokens (
    id BIGSERIAL PRIMARY KEY,
    token VARCHAR(255) UNIQUE NOT NULL,  -- UUID
    user_id BIGINT NOT NULL REFERENCES users(id),
    expires_at TIMESTAMP NOT NULL
);
```

### Tabla: `refresh_tokens`

```sql
CREATE TABLE refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    token VARCHAR(500) UNIQUE NOT NULL,  -- UUID
    user_id BIGINT NOT NULL REFERENCES users(id),
    expiry_date TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    revoked_at TIMESTAMP,
    revoked BOOLEAN NOT NULL DEFAULT false
);
```

---

## 🧪 Testing

### Script de Prueba

Usa los scripts incluidos para probar todos los endpoints:

**PowerShell**:
```powershell
.\test-auth-endpoints.ps1
```

**Bash**:
```bash
bash test-auth-endpoints.sh
```

### Tests Incluidos

1. ✅ Health check (`/api/ping`)
2. ✅ Registro de usuario nuevo
3. ✅ Intento de registro duplicado (debe fallar)
4. ✅ Login sin verificación de email (debe fallar)
5. ✅ Login con contraseña incorrecta (debe fallar)
6. ✅ Refresh token válido
7. ✅ Refresh token inválido (debe fallar)
8. ✅ Logout
9. ✅ Uso de token revocado (debe fallar)
10. ✅ Verificación con token inválido (debe fallar)

---

## ⚠️ Limitaciones Actuales

### Email NO Verificado

**Situación actual**:
- ✅ Usuario puede registrarse
- ✅ Recibe tokens (access + refresh)
- ❌ **NO puede hacer login** hasta verificar email

**Comportamiento**:
```
1. POST /api/auth/register → 200 OK (tokens devueltos)
2. POST /api/auth/login → 400 Bad Request
   "Debes verificar tu correo electrónico antes de iniciar sesión."
```

**Consideración**: Esto puede ser confuso porque el registro devuelve tokens pero no puedes usarlos para login. Se podría considerar:

**Opción A** (actual): Devolver tokens en registro pero requerir verificación para login
**Opción B**: No devolver tokens en registro, solo un mensaje de "verifica tu email"
**Opción C**: Permitir login sin verificación pero con funcionalidad limitada

### Configuración de Email

Si `MAIL_*` variables no están configuradas:
- ✅ El registro funciona
- ✅ Se genera el token de verificación
- ❌ El email NO se envía
- ⚠️ El usuario nunca recibirá el link de verificación
- ⚠️ No podrá hacer login

**Solución**: Ver [EMAIL_CONFIGURATION.md](EMAIL_CONFIGURATION.md)

---

## 🔄 Diagrama de Estados del Usuario

```
[Registrado]
    │
    │ emailVerified = false
    │ isActive = true
    ▼
[Pendiente de Verificación] ─────► [NO puede hacer login]
    │
    │ Verifica email
    │
    ▼
[Email Verificado]
    │
    │ emailVerified = true
    │ isActive = true
    ▼
[Activo] ───────────────────────► [PUEDE hacer login]
    │
    │ Admin desactiva
    │
    ▼
[Inactivo]
    │
    │ isActive = false
    ▼
[NO puede hacer login]
```

---

## 📱 Integración con Frontend

### Almacenamiento de Tokens

**Recomendado**: `localStorage` o `sessionStorage`

```typescript
// Después de login o registro
const response = await fetch('/api/auth/login', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ username, password })
});

const data = await response.json();

// Guardar tokens
localStorage.setItem('accessToken', data.token);
localStorage.setItem('refreshToken', data.refreshToken);
localStorage.setItem('username', data.username);
```

### Uso del Access Token

```typescript
// En cada request autenticado
const token = localStorage.getItem('accessToken');

const response = await fetch('/api/alert', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${token}`
  },
  body: JSON.stringify(alertData)
});
```

### Manejo de Expiración

```typescript
// Interceptor de Axios (o similar)
axios.interceptors.response.use(
  response => response,
  async error => {
    if (error.response?.status === 401) {
      // Token expirado, intentar refresh
      const refreshToken = localStorage.getItem('refreshToken');

      const response = await fetch('/api/auth/refresh', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ refreshToken })
      });

      if (response.ok) {
        const data = await response.json();
        localStorage.setItem('accessToken', data.token);

        // Reintentar request original
        return axios(error.config);
      } else {
        // Refresh falló, redirigir a login
        localStorage.clear();
        window.location.href = '/login';
      }
    }
    return Promise.reject(error);
  }
);
```

### Logout

```typescript
async function logout() {
  const refreshToken = localStorage.getItem('refreshToken');

  // Revocar refresh token en backend
  await fetch('/api/auth/logout', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ refreshToken })
  });

  // Limpiar storage
  localStorage.removeItem('accessToken');
  localStorage.removeItem('refreshToken');
  localStorage.removeItem('username');

  // Redirigir
  window.location.href = '/login';
}
```

---

## 🐛 Troubleshooting

### "El username ya está en uso"

**Causa**: Ya existe un usuario con ese username
**Solución**: Usar otro username diferente

### "El email ya está registrado"

**Causa**: Ya existe un usuario con ese email
**Solución**: Usar otro email o hacer login con ese email

### "Debes verificar tu correo electrónico"

**Causa**: Intentaste hacer login sin verificar el email
**Solución**:
1. Revisa tu correo (incluye spam)
2. Haz clic en el link de verificación
3. Intenta hacer login de nuevo

### "El enlace de verificación ha expirado"

**Causa**: El token tiene más de 24 horas
**Solución**: Actualmente no hay endpoint para reenviar email. Opciones:
1. Registrarse con otro email
2. Agregar endpoint de reenvío de verificación (feature pendiente)

### "Refresh token inválido"

**Causa**: El refresh token fue revocado, expiró o no existe
**Solución**: Hacer login de nuevo

### "401 Unauthorized" en requests protegidos

**Causa**: Access token inválido, expirado o no enviado
**Solución**:
1. Verificar que estás enviando el header `Authorization: Bearer {token}`
2. Intentar refresh del token
3. Si falla, hacer login de nuevo

---

## 🎓 Mejoras Futuras Sugeridas

1. **Reenvío de email de verificación**
   ```
   POST /api/auth/resend-verification
   { "email": "user@example.com" }
   ```

2. **Recuperación de contraseña**
   ```
   POST /api/auth/forgot-password
   POST /api/auth/reset-password
   ```

3. **Cambio de contraseña**
   ```
   POST /api/auth/change-password
   ```

4. **Autenticación de dos factores (2FA)**

5. **Login con Google/Facebook (OAuth2)**

6. **Rate limiting en endpoints de auth**

7. **Logs de intentos de login fallidos**

8. **Notificación de login desde nuevo dispositivo**

---

## 📞 Soporte

Si tienes problemas:

1. Revisa los logs del backend
2. Usa el script de testing: `test-auth-endpoints.ps1`
3. Verifica la configuración de email: [EMAIL_CONFIGURATION.md](EMAIL_CONFIGURATION.md)
4. Revisa la documentación de Swagger: `http://localhost:8080/api/swagger-ui.html`

---

**Última actualización**: 2025-11-20
**Versión del backend**: 1.0.0
