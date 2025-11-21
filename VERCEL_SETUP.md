# Configuración de Vercel para Road Warnings Nariño

## URL del Frontend
`https://road-warnings-narino-frontend.vercel.app`

---

## Pasos para Conectar el Frontend con el Backend

### 1. Obtener la URL del Backend en Render

1. Ve a tu dashboard de Render: https://dashboard.render.com/
2. Busca tu servicio backend (debería llamarse algo como "roadwarnings-narino-backend" o similar)
3. Copia la URL que aparece arriba (ejemplo: `https://roadwarnings-narino-backend.onrender.com`)

**IMPORTANTE:** La URL de Render termina en `.onrender.com`

---

### 2. Configurar Variable de Entorno en Vercel

1. Ve a: https://vercel.com/dashboard
2. Selecciona tu proyecto: `road-warnings-narino-frontend`
3. Ve a **Settings** (arriba derecha)
4. En el menú lateral, selecciona **Environment Variables**
5. Haz clic en **Add New**

**Configura según tu framework:**

#### Si usas React + Vite:
```
Name:  VITE_API_URL
Value: https://TU-URL-DE-RENDER.onrender.com/api
```

#### Si usas Next.js:
```
Name:  NEXT_PUBLIC_API_URL
Value: https://TU-URL-DE-RENDER.onrender.com/api
```

#### Si usas Angular:
Necesitarás actualizar el archivo `src/environments/environment.prod.ts` en tu código:
```typescript
export const environment = {
  production: true,
  apiUrl: 'https://TU-URL-DE-RENDER.onrender.com/api'
};
```

**NOTA IMPORTANTE:**
- Reemplaza `TU-URL-DE-RENDER` con tu URL real de Render
- **NO OLVIDES** el `/api` al final
- Ejemplo completo: `https://roadwarnings-narino-backend.onrender.com/api`

6. Selecciona **Production, Preview, and Development** (para todos los ambientes)
7. Haz clic en **Save**

---

### 3. Redeploy en Vercel

Después de agregar la variable de entorno:

1. Ve a la pestaña **Deployments**
2. Busca el último deployment exitoso
3. Haz clic en los 3 puntos (...) a la derecha
4. Selecciona **Redeploy**
5. Confirma haciendo clic en **Redeploy** nuevamente
6. Espera a que termine el deployment (toma 1-3 minutos)

---

### 4. Verificar que las Variables de Entorno en Render están Correctas

1. Ve a tu servicio backend en Render
2. Ve a **Environment** en el menú lateral
3. Verifica que estas variables estén configuradas:

```bash
APP_AUTH_REQUIRE_EMAIL_VERIFICATION=false
DATABASE_URL=jdbc:postgresql://... (tu URL de PostgreSQL)
DB_USER=tu_usuario
DB_PASSWORD=tu_password
JWT_SECRET=tu_secreto_jwt
FRONTEND_URL=https://road-warnings-narino-frontend.vercel.app
```

**Si falta `APP_AUTH_REQUIRE_EMAIL_VERIFICATION=false`:**
- Agrégala
- Haz clic en **Save Changes**
- El servicio se redeployará automáticamente

---

### 5. Actualizar el Código del Frontend (Quitar Mensaje de Email)

Busca en tu código donde manejas la respuesta del registro exitoso.

**Archivo típico:**
- `src/services/auth.service.ts` (Angular)
- `src/services/authService.ts` (React)
- `src/composables/useAuth.ts` (Vue)

**Busca código similar a esto:**

```typescript
// ❌ ANTES - Con mensaje de verificación
async register(username: string, email: string, password: string) {
  const response = await api.post('/auth/register', { username, email, password });
  alert('Se envió un correo para verificar tu cuenta');
  // o
  showNotification('Revisa tu email para activar tu cuenta');
  router.push('/login');
}
```

**Cámbialo por esto:**

```typescript
// ✅ DESPUÉS - Sin verificación, entrada directa
async register(username: string, email: string, password: string) {
  const response = await api.post('/auth/register', { username, email, password });

  // Guardar el token
  localStorage.setItem('token', response.data.token);
  localStorage.setItem('refreshToken', response.data.refreshToken);
  localStorage.setItem('username', response.data.username);

  // Redirigir al dashboard o página principal
  router.push('/dashboard'); // o '/home' o la ruta que uses
}
```

---

### 6. Verificar que el Servicio de Auth usa la Variable de Entorno

**React/Vite - Verifica tu archivo de configuración API:**

```typescript
// src/config/api.ts o similar
const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

export default axios.create({
  baseURL: API_URL,
  headers: {
    'Content-Type': 'application/json'
  }
});
```

**Next.js:**

```typescript
const API_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api';
```

**Angular - environment.prod.ts:**

```typescript
export const environment = {
  production: true,
  apiUrl: 'https://TU-URL-DE-RENDER.onrender.com/api'
};
```

---

### 7. Hacer Push de los Cambios del Frontend

Si modificaste código (paso 5 y 6):

```bash
git add .
git commit -m "Fix: Connect to Render backend and remove email verification message"
git push origin main
```

Vercel detectará el push y hará deployment automáticamente.

---

### 8. Testing Final

Una vez que Vercel termine el deployment:

1. Abre: https://road-warnings-narino-frontend.vercel.app
2. Abre la consola del navegador (F12)
3. Ve a la pestaña **Network**
4. Intenta registrar un nuevo usuario
5. Verifica que la petición vaya a: `https://TU-URL-DE-RENDER.onrender.com/api/auth/register`
6. Si el registro es exitoso, deberías ser redirigido al dashboard automáticamente
7. Verifica en **Application** → **Local Storage** que se guardaron:
   - `token`
   - `refreshToken`
   - `username`

---

## Checklist de Verificación ✅

- [ ] Obtuve mi URL de Render backend
- [ ] Variable de entorno configurada en Vercel (`VITE_API_URL` o `NEXT_PUBLIC_API_URL`)
- [ ] La URL termina en `/api` (ej: `https://mi-backend.onrender.com/api`)
- [ ] Redeployé en Vercel después de agregar la variable
- [ ] `APP_AUTH_REQUIRE_EMAIL_VERIFICATION=false` está en Render
- [ ] `FRONTEND_URL=https://road-warnings-narino-frontend.vercel.app` está en Render
- [ ] Código del frontend actualizado para entrar directamente después del registro
- [ ] Mensaje de "verifica tu email" removido
- [ ] Hice push de los cambios del frontend
- [ ] Probé el registro y login en producción

---

## Ejemplo de URLs Correctas

### ❌ URLs Incorrectas:
```
http://localhost:8080/api/auth/login
https://mi-backend.onrender.com/auth/login  (falta /api)
https://mi-backend.onrender.com/api/auth/login/  (sobra / al final)
```

### ✅ URLs Correctas:
```
https://mi-backend.onrender.com/api/auth/register
https://mi-backend.onrender.com/api/auth/login
https://mi-backend.onrender.com/api/auth/logout
https://mi-backend.onrender.com/api/alert
```

---

## Errores Comunes y Soluciones

### Error: "Failed to fetch"
**Causa:** La variable de entorno no está configurada o tiene la URL incorrecta
**Solución:**
1. Verifica que agregaste la variable en Vercel
2. Verifica que la URL sea correcta
3. Redeploy en Vercel

### Error: "CORS policy" en consola
**Causa:** El backend no está configurado para permitir tu frontend
**Solución:** El backend ya está configurado para permitir `*.vercel.app`, pero verifica que:
1. El backend esté corriendo en Render
2. La variable `FRONTEND_URL` esté configurada en Render

### Error: "401 Unauthorized" después de registro
**Causa:** El token no se guardó correctamente
**Solución:** Verifica que después del registro exitoso, guardes el token en localStorage

### Los usuarios aún ven mensaje de "verifica tu email"
**Causa:** El código del frontend no se actualizó
**Solución:** Actualiza el código (paso 5) y haz push

---

## Próximos Pasos

Una vez que todo funcione:

1. ✅ Los usuarios podrán registrarse e iniciar sesión sin verificar email
2. ✅ Entrarán directamente al dashboard después del registro
3. ✅ No aparecerán alertas de prueba (ya deshabilitadas)
4. ✅ El backend y frontend estarán 100% integrados

---

## Contacto

Si después de seguir estos pasos sigues teniendo problemas:

1. Comparte la URL de Render que obtuviste
2. Comparte una captura de las variables de entorno en Vercel
3. Comparte el error exacto que aparece en la consola del navegador
