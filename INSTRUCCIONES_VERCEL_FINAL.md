# 🎯 Instrucciones Finales - Conectar Vercel con Render

## Tu Configuración:
- **Frontend Vercel:** `https://road-warnings-narino-frontend.vercel.app`
- **Backend Render:** `https://roadwarningsnarino-backend.onrender.com`

---

## Paso 1: Agregar Variable de Entorno en Vercel

1. Ve a: https://vercel.com/dashboard
2. Selecciona tu proyecto: `road-warnings-narino-frontend`
3. Haz clic en **Settings** (arriba derecha)
4. En el menú lateral izquierdo, selecciona **Environment Variables**
5. Haz clic en **Add New Variable**

### Configura EXACTAMENTE esto:

**Si tu frontend usa React + Vite:**
```
Name:  VITE_API_URL
Value: https://roadwarningsnarino-backend.onrender.com/api
```

**Si tu frontend usa Next.js:**
```
Name:  NEXT_PUBLIC_API_URL
Value: https://roadwarningsnarino-backend.onrender.com/api
```

**Si tu frontend usa Angular:**
```
Name:  API_URL
Value: https://roadwarningsnarino-backend.onrender.com/api
```

6. En **Environment** selecciona: **Production, Preview, and Development**
7. Haz clic en **Save**

---

## Paso 2: Redeploy en Vercel

1. Ve a la pestaña **Deployments**
2. Busca el deployment más reciente (el primero de la lista)
3. Haz clic en los **tres puntos (...)** a la derecha
4. Selecciona **Redeploy**
5. En el modal que aparece, haz clic en **Redeploy** nuevamente
6. Espera 1-3 minutos a que termine el deployment

---

## Paso 3: Verificar Variables en Render

Ve a tu backend en Render y verifica que estas variables estén configuradas:

1. Dashboard de Render → `roadwarningsnarino-backend` → **Environment**
2. Verifica que exista:
   ```
   APP_AUTH_REQUIRE_EMAIL_VERIFICATION = false
   ```
3. Si no existe, agrégala:
   - Click en **Add Environment Variable**
   - Name: `APP_AUTH_REQUIRE_EMAIL_VERIFICATION`
   - Value: `false`
   - Click **Save Changes** (esto hará un redeploy automático)

---

## Paso 4: Verificar el Código del Frontend

Asegúrate de que tu frontend esté usando la variable de entorno correctamente.

### React + Vite

**Archivo: `src/config/api.ts` o `src/services/api.ts`**
```typescript
import axios from 'axios';

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_URL,
  headers: {
    'Content-Type': 'application/json'
  }
});

export default api;
```

### Next.js

**Archivo: `src/config/api.ts`**
```typescript
const API_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api';
```

### Angular

**Archivo: `src/environments/environment.prod.ts`**
```typescript
export const environment = {
  production: true,
  apiUrl: 'https://roadwarningsnarino-backend.onrender.com/api'
};
```

**Archivo: `src/app/services/auth.service.ts`**
```typescript
import { environment } from '../environments/environment';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private apiUrl = environment.apiUrl;
  // ...
}
```

---

## Paso 5: Actualizar el Flujo de Registro (Opcional pero Recomendado)

Como ya deshabilitamos la verificación de email, actualiza tu frontend para que **NO muestre** el mensaje de "verifica tu email" y en su lugar **redirija directamente** al dashboard.

**Busca en tu código el archivo de servicio de autenticación** y modifica:

### Antes:
```typescript
async register(username: string, email: string, password: string) {
  const response = await api.post('/auth/register', { username, email, password });
  // Muestra mensaje de verificación
  showMessage('Se envió un correo para verificar tu cuenta');
  router.push('/login'); // ← Redirige al login
}
```

### Después:
```typescript
async register(username: string, email: string, password: string) {
  const response = await api.post('/auth/register', { username, email, password });

  // Guardar tokens inmediatamente
  localStorage.setItem('token', response.data.token);
  localStorage.setItem('refreshToken', response.data.refreshToken);
  localStorage.setItem('username', response.data.username);

  // Redirigir al dashboard directamente
  router.push('/dashboard'); // ← O '/home' o la ruta principal de tu app
}
```

Si haces este cambio, sube el código:
```bash
git add .
git commit -m "Connect frontend to Render backend and remove email verification"
git push origin main
```

Vercel detectará el push y hará deployment automáticamente.

---

## Paso 6: Testing

Una vez que Vercel termine el deployment:

### 6.1 Abrir el Frontend
1. Ve a: `https://road-warnings-narino-frontend.vercel.app`
2. Abre la consola del navegador (F12)
3. Ve a la pestaña **Network**

### 6.2 Probar el Registro
1. Intenta registrar un nuevo usuario
2. En la pestaña Network, busca la petición `register`
3. Verifica que la URL sea: `https://roadwarningsnarino-backend.onrender.com/api/auth/register`
4. Si es exitoso, deberías:
   - Ver el token en la respuesta
   - Ser redirigido al dashboard (si actualizaste el código)
   - Ver el token en **Application** → **Local Storage**

### 6.3 Probar el Login
1. Intenta iniciar sesión
2. Verifica que la petición vaya a: `https://roadwarningsnarino-backend.onrender.com/api/auth/login`
3. Si es exitoso, deberías entrar al dashboard

---

## ✅ Checklist Final

- [ ] Variable de entorno agregada en Vercel (`VITE_API_URL` o `NEXT_PUBLIC_API_URL`)
- [ ] El valor es: `https://roadwarningsnarino-backend.onrender.com/api`
- [ ] Redeployé en Vercel
- [ ] Verifiqué que `APP_AUTH_REQUIRE_EMAIL_VERIFICATION=false` esté en Render
- [ ] El código del frontend usa la variable de entorno
- [ ] (Opcional) Actualicé el flujo de registro para entrar directamente
- [ ] Probé el registro en producción
- [ ] Probé el login en producción

---

## URLs Correctas

### ❌ Incorrectas:
```
http://localhost:8080/api/auth/login
https://roadwarningsnarino-backend.onrender.com/auth/login (falta /api)
https://roadwarningsnarino-backend.onrender.com/api/auth/login/ (sobra /)
```

### ✅ Correctas:
```
https://roadwarningsnarino-backend.onrender.com/api/auth/register
https://roadwarningsnarino-backend.onrender.com/api/auth/login
https://roadwarningsnarino-backend.onrender.com/api/alert
https://roadwarningsnarino-backend.onrender.com/api/user/me
```

---

## Errores Comunes

### Error: "Failed to fetch" en el frontend
**Causa:** La variable de entorno no está configurada en Vercel o no hiciste redeploy
**Solución:**
1. Verifica que agregaste la variable en Vercel
2. Haz redeploy
3. Limpia caché del navegador (Ctrl + Shift + R)

### Error: Las peticiones van a localhost
**Causa:** La variable de entorno no está siendo usada en el código
**Solución:** Verifica el Paso 4

### Error: CORS en la consola
**Causa:** El backend no permite tu frontend (ya está solucionado en el código)
**Solución:** Asegúrate de que el último commit esté desplegado en Render

---

## Verificación Rápida

Ejecuta esto en tu navegador desde el frontend para verificar la conexión:

**Abre la consola del navegador (F12) y ejecuta:**
```javascript
fetch('https://roadwarningsnarino-backend.onrender.com/api/alert')
  .then(res => res.json())
  .then(data => console.log('✅ Conexión exitosa:', data))
  .catch(err => console.error('❌ Error:', err));
```

Si ves `✅ Conexión exitosa:` y un array de alertas, todo está funcionando.

---

## Próximos Pasos

Una vez que funcione:

1. ✅ Los usuarios podrán registrarse e iniciar sesión
2. ✅ No aparecerá mensaje de verificación de email
3. ✅ Las alertas se cargarán desde el backend de Render
4. ✅ Todo funcionará en producción

---

## ¿Necesitas Ayuda?

Si después de seguir estos pasos sigues viendo "Failed to fetch":

1. Comparte una captura de la pestaña Network en la consola del navegador
2. Comparte una captura de las variables de entorno en Vercel
3. Dime qué framework usas (React, Next.js, Angular, Vue)
