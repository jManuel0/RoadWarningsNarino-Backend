# Instrucciones para Integrar el Frontend con el Backend

## 🎯 Resumen de Cambios Necesarios

El backend está funcionando correctamente. Para que el frontend se conecte con el backend en producción, necesitas hacer los siguientes cambios:

---

## 1. Configurar la URL del Backend

### Opción A: Si usas Angular

Busca el archivo `environment.prod.ts` (o `environment.production.ts`) y actualiza:

```typescript
export const environment = {
  production: true,
  apiUrl: 'https://tu-backend.onrender.com/api'  // ← Cambia esto por tu URL de Render
};
```

**IMPORTANTE:** Nota el `/api` al final. El backend tiene configurado `server.servlet.context-path=/api`

### Opción B: Si usas React

Crea o modifica el archivo `.env.production` en la raíz del proyecto:

```bash
REACT_APP_API_URL=https://tu-backend.onrender.com/api
```

### Opción C: Si usas Vue

Crea o modifica el archivo `.env.production`:

```bash
VUE_APP_API_URL=https://tu-backend.onrender.com/api
```

### Opción D: Si usas Next.js

Crea o modifica el archivo `.env.production`:

```bash
NEXT_PUBLIC_API_URL=https://tu-backend.onrender.com/api
```

---

## 2. Verificar los Endpoints de Autenticación

Asegúrate de que tu servicio de autenticación use estos endpoints:

```typescript
// Base URL
const API_URL = 'https://tu-backend.onrender.com/api';

// Endpoints
const ENDPOINTS = {
  register: `${API_URL}/auth/register`,      // POST
  login: `${API_URL}/auth/login`,            // POST
  refresh: `${API_URL}/auth/refresh`,        // POST
  logout: `${API_URL}/auth/logout`,          // POST
  verifyEmail: `${API_URL}/auth/verify-email` // GET (opcional)
};
```

---

## 3. Formato de las Peticiones

### Registro (POST /api/auth/register)

**Request:**
```json
{
  "username": "usuario123",
  "email": "usuario@example.com",
  "password": "Password123"
}
```

**Validaciones:**
- `username`: 3-20 caracteres, solo letras, números, guiones y guiones bajos
- `email`: formato de email válido
- `password`: mínimo 8 caracteres, debe incluir:
  - Al menos 1 letra mayúscula
  - Al menos 1 letra minúscula
  - Al menos 1 número

**Response exitosa (200):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "2010d8cc-1ef7-45db-861a-f214a661a41e",
  "expiresIn": 86400,
  "username": "usuario123"
}
```

**Errores posibles:**
- 400: "El username ya está en uso"
- 400: "El email ya está registrado"
- 400: Errores de validación (contraseña débil, formato incorrecto, etc.)

---

### Login (POST /api/auth/login)

**Request:**
```json
{
  "username": "usuario123",
  "password": "Password123"
}
```

**Nota:** El campo `username` puede ser el username O el email del usuario.

**Response exitosa (200):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "273ffcf2-97ce-4d40-87fe-202af1600fc6",
  "expiresIn": 86400,
  "username": "usuario123"
}
```

**Errores posibles:**
- 401: "Usuario no encontrado"
- 401: Contraseña incorrecta
- 400: "Debes verificar tu correo electrónico" (solo si está habilitada la verificación)

---

### Refresh Token (POST /api/auth/refresh)

**Request:**
```json
{
  "refreshToken": "273ffcf2-97ce-4d40-87fe-202af1600fc6"
}
```

**Response exitosa (200):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "273ffcf2-97ce-4d40-87fe-202af1600fc6",
  "expiresIn": 86400,
  "username": "usuario123"
}
```

---

### Logout (POST /api/auth/logout)

**Request:**
```json
{
  "refreshToken": "273ffcf2-97ce-4d40-87fe-202af1600fc6"
}
```

**Response exitosa (200):**
```
"Sesión cerrada correctamente"
```

---

## 4. Headers Requeridos

Para todas las peticiones de autenticación:

```typescript
headers: {
  'Content-Type': 'application/json'
}
```

Para peticiones protegidas (endpoints que requieren autenticación):

```typescript
headers: {
  'Content-Type': 'application/json',
  'Authorization': `Bearer ${token}`
}
```

---

## 5. Validación de Contraseña en el Frontend

Implementa esta validación antes de enviar el formulario:

```typescript
function validatePassword(password: string): boolean {
  // Al menos 8 caracteres, 1 mayúscula, 1 minúscula, 1 número
  const regex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).{8,}$/;
  return regex.test(password);
}

function validateUsername(username: string): boolean {
  // 3-20 caracteres, solo letras, números, - y _
  const regex = /^[a-zA-Z0-9_-]{3,20}$/;
  return regex.test(username);
}

function validateEmail(email: string): boolean {
  const regex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return regex.test(email);
}
```

---

## 6. Manejo del Token JWT

Después de un login o registro exitoso:

```typescript
// Guardar el token y refreshToken
localStorage.setItem('token', response.token);
localStorage.setItem('refreshToken', response.refreshToken);
localStorage.setItem('username', response.username);

// Para peticiones autenticadas, recuperar el token
const token = localStorage.getItem('token');

// Configurar interceptor o agregar header manualmente
axios.defaults.headers.common['Authorization'] = `Bearer ${token}`;
```

---

## 7. Ejemplo Completo de Servicio de Autenticación

### Angular

```typescript
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../environments/environment';

interface AuthResponse {
  token: string;
  refreshToken: string;
  expiresIn: number;
  username: string;
}

interface RegisterRequest {
  username: string;
  email: string;
  password: string;
}

interface LoginRequest {
  username: string;
  password: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  register(data: RegisterRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/auth/register`, data);
  }

  login(data: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/auth/login`, data);
  }

  logout(): Observable<string> {
    const refreshToken = localStorage.getItem('refreshToken');
    return this.http.post<string>(`${this.apiUrl}/auth/logout`, { refreshToken });
  }

  saveToken(response: AuthResponse): void {
    localStorage.setItem('token', response.token);
    localStorage.setItem('refreshToken', response.refreshToken);
    localStorage.setItem('username', response.username);
  }

  getToken(): string | null {
    return localStorage.getItem('token');
  }

  clearTokens(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('username');
  }
}
```

### React (con axios)

```typescript
import axios from 'axios';

const API_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

export interface AuthResponse {
  token: string;
  refreshToken: string;
  expiresIn: number;
  username: string;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export const authService = {
  register: async (data: RegisterRequest): Promise<AuthResponse> => {
    const response = await axios.post(`${API_URL}/auth/register`, data);
    return response.data;
  },

  login: async (data: LoginRequest): Promise<AuthResponse> => {
    const response = await axios.post(`${API_URL}/auth/login`, data);
    return response.data;
  },

  logout: async (): Promise<void> => {
    const refreshToken = localStorage.getItem('refreshToken');
    await axios.post(`${API_URL}/auth/logout`, { refreshToken });
  },

  saveToken: (response: AuthResponse): void => {
    localStorage.setItem('token', response.token);
    localStorage.setItem('refreshToken', response.refreshToken);
    localStorage.setItem('username', response.username);
  },

  getToken: (): string | null => {
    return localStorage.getItem('token');
  },

  clearTokens: (): void => {
    localStorage.removeItem('token');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('username');
  }
};

// Configurar interceptor para agregar el token automáticamente
axios.interceptors.request.use(
  (config) => {
    const token = authService.getToken();
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);
```

---

## 8. Checklist Final

Antes de desplegar el frontend, verifica:

- [ ] La URL del backend está correcta y termina en `/api`
- [ ] Los endpoints de autenticación incluyen `/api` (ej: `/api/auth/register`)
- [ ] Las validaciones de contraseña están implementadas
- [ ] El token JWT se guarda después del login/registro
- [ ] El token se envía en el header `Authorization: Bearer {token}`
- [ ] El manejo de errores está implementado
- [ ] Se guardan tanto `token` como `refreshToken`
- [ ] La URL del backend en producción es diferente a desarrollo

---

## 9. URLs Importantes

### Desarrollo (Local)
- Backend: `http://localhost:8080/api`
- Frontend: `http://localhost:4200` (o el puerto que uses)

### Producción
- Backend: `https://tu-backend.onrender.com/api`
- Frontend: `https://road-warnings-narino-frontend.vercel.app`

---

## 10. Testing en Producción

Una vez que hagas los cambios y despliegues:

1. Abre la consola del navegador (F12)
2. Intenta registrar un usuario
3. Verifica que la petición vaya a: `https://tu-backend.onrender.com/api/auth/register`
4. Si hay error de CORS, avísame inmediatamente
5. Si el registro es exitoso, verifica que el token se guardó en localStorage

---

## 11. Errores Comunes

### Error: "Failed to fetch" o "Network Error"
**Causa:** La URL del backend está mal configurada o el backend no está corriendo.
**Solución:** Verifica que la URL sea correcta y que el backend esté desplegado en Render.

### Error: "CORS policy"
**Causa:** El backend no permite peticiones desde tu frontend.
**Solución:** Ya está configurado en el backend con `@CrossOrigin(origins = "*")`. Si persiste, avísame.

### Error: 404 Not Found
**Causa:** Falta el `/api` en la URL o el endpoint está mal escrito.
**Solución:** Asegúrate de que todas las URLs incluyan `/api` después del dominio.

### Error: "El email ya está registrado"
**Causa:** Intentas registrar un email que ya existe.
**Solución:** Usa otro email o implementa la funcionalidad de "olvidé mi contraseña".

---

## 12. Variables de Entorno por Framework

Asegúrate de crear el archivo correcto:

| Framework | Archivo | Variable |
|-----------|---------|----------|
| Angular | `environment.prod.ts` | `apiUrl: 'https://...'` |
| React | `.env.production` | `REACT_APP_API_URL=https://...` |
| Vue | `.env.production` | `VUE_APP_API_URL=https://...` |
| Next.js | `.env.production` | `NEXT_PUBLIC_API_URL=https://...` |

---

## 📞 Contacto

Si tienes algún problema después de implementar estos cambios:

1. Revisa la consola del navegador para ver el error exacto
2. Verifica que el backend esté corriendo en Render
3. Prueba los endpoints directamente con curl o Postman
4. Comparte el error específico para ayudarte mejor

---

**Nota:** Todos los cambios en el backend ya están listos y funcionando. Solo necesitas actualizar el frontend con estas instrucciones.
