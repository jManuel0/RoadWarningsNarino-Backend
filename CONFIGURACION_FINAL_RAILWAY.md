# ✅ Configuración Final - Railway + Vercel

## Tu Configuración:

- **Backend Railway:** `https://roadwarningsnarino-backend-production.up.railway.app`
- **Frontend Vercel:** `https://road-warnings-narino-frontend.vercel.app`
- **Base de datos:** PostgreSQL en Railway

---

## Paso 1: Verificar Variables de Entorno en Railway

Ve a tu servicio **roadwarningsnarino-backend** en Railway → **Variables**

Asegúrate de que tengas TODAS estas variables:

```bash
# Database
DATABASE_URL=jdbc:postgresql://${{Postgres.PGHOST}}:${{Postgres.PGPORT}}/${{Postgres.PGDATABASE}}
DB_USER=${{Postgres.PGUSER}}
DB_PASSWORD=${{Postgres.PGPASSWORD}}

# JWT
JWT_SECRET=MiSecretoSuperSeguroParaProduccion2024RoadWarnings123456789
JWT_EXPIRATION=86400000
JWT_REFRESH_EXPIRATION=604800000

# Authentication
APP_AUTH_REQUIRE_EMAIL_VERIFICATION=false

# Frontend
FRONTEND_URL=https://road-warnings-narino-frontend.vercel.app

# JPA
JPA_DDL_AUTO=update
JPA_SHOW_SQL=false
JPA_FORMAT_SQL=false

# Server
PORT=8080

# Logging
LOG_LEVEL_ROOT=INFO
LOG_LEVEL_APP=INFO

# Swagger
SWAGGER_ENABLED=true
```

**Si falta alguna:**
1. Haz clic en **New Variable**
2. Copia el nombre y valor
3. Railway redeployará automáticamente

---

## Paso 2: Esperar a que Railway Termine de Desplegar

1. En Railway, ve a la pestaña **Deployments**
2. Verás el deployment actual en progreso
3. Espera a que aparezca **✓ Success** (puede tomar 3-5 minutos)
4. Verás logs que terminan con: `Started NarinoApplication in X.XXX seconds`

---

## Paso 3: Probar el Backend

Una vez que el deployment termine, abre en tu navegador:

### Swagger UI (Documentación de la API):
```
https://roadwarningsnarino-backend-production.up.railway.app/api/swagger-ui/index.html
```

### Endpoint de alertas:
```
https://roadwarningsnarino-backend-production.up.railway.app/api/alert
```

Deberías ver un array JSON con las alertas (o `[]` si está vacío).

---

## Paso 4: Actualizar Vercel con la URL de Railway

1. Ve a: https://vercel.com/dashboard
2. Selecciona tu proyecto: `road-warnings-narino-frontend`
3. **Settings** → **Environment Variables**
4. Busca `VITE_API_URL`

**Si ya existe:**
- Haz clic en los **tres puntos (...)** → **Edit**
- Cambia el valor a:
  ```
  https://roadwarningsnarino-backend-production.up.railway.app/api
  ```
- Haz clic en **Save**

**Si NO existe:**
- Haz clic en **Add New**
- Name: `VITE_API_URL`
- Value: `https://roadwarningsnarino-backend-production.up.railway.app/api`
- Environments: **Production, Preview, and Development**
- Haz clic en **Save**

---

## Paso 5: Redeploy en Vercel

1. Ve a **Deployments**
2. Busca el último deployment
3. Haz clic en los **tres puntos (...)** → **Redeploy**
4. Confirma haciendo clic en **Redeploy** nuevamente
5. Espera 1-2 minutos a que termine

---

## Paso 6: Probar Todo Junto

Una vez que Vercel termine de desplegar:

1. Abre: `https://road-warnings-narino-frontend.vercel.app`
2. Abre la consola del navegador (F12) → **Network**
3. Intenta **registrar un nuevo usuario**
4. Verifica en Network que la petición vaya a:
   ```
   https://roadwarningsnarino-backend-production.up.railway.app/api/auth/register
   ```
5. Si el registro es exitoso:
   - Deberías recibir un `token` en la respuesta
   - Deberías ser redirigido al dashboard
   - El token debería guardarse en **Application** → **Local Storage**

---

## Paso 7: Verificar que el Frontend Usa la Variable Correcta

Si el frontend sigue apuntando a localhost, verifica tu código:

**Archivo: `src/config/api.ts` o `src/lib/axios.ts`**

```typescript
import axios from 'axios';

// Debe usar import.meta.env.VITE_API_URL
const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_URL,
  headers: {
    'Content-Type': 'application/json'
  },
  withCredentials: true
});

export default api;
```

**Si está usando `process.env` en lugar de `import.meta.env`:**
Cámbialo a `import.meta.env.VITE_API_URL` (así funciona Vite).

---

## Troubleshooting

### Error: "Failed to fetch" desde el frontend

**Causa:** La variable `VITE_API_URL` no está configurada en Vercel o el frontend no la está usando

**Solución:**
1. Verifica que agregaste `VITE_API_URL` en Vercel
2. Verifica que redeployaste después de agregar la variable
3. Verifica que el código use `import.meta.env.VITE_API_URL`

### Error: CORS en la consola

**Causa:** Railway no ha desplegado los últimos cambios del CORS

**Solución:** Ya está configurado en `CorsConfig.java` para permitir `*.vercel.app`

### Backend responde 503 o no responde

**Causa:** Railway aún está desplegando o el servicio está "dormido" (en el plan free se duerme después de inactividad)

**Solución:** Espera 30-60 segundos. La primera petición puede tardar en responder.

### Error: "Connection refused" en Railway

**Causa:** Las variables de la base de datos no están configuradas correctamente

**Solución:** Verifica que tengas las variables `DATABASE_URL`, `DB_USER`, `DB_PASSWORD` con las referencias a `${{Postgres.xxx}}`

---

## Checklist Final ✅

- [ ] Todas las variables de entorno agregadas en Railway
- [ ] Railway ha desplegado exitosamente (✓ Success en Deployments)
- [ ] Swagger UI funciona: `https://roadwarningsnarino-backend-production.up.railway.app/api/swagger-ui/index.html`
- [ ] Endpoint `/api/alert` responde correctamente
- [ ] Variable `VITE_API_URL` agregada en Vercel
- [ ] Vercel redeployado después de agregar la variable
- [ ] Frontend apunta a Railway (verificado en Network tab)
- [ ] Registro y login funcionan en producción

---

## URLs Finales

### Backend (Railway):
- **Base:** `https://roadwarningsnarino-backend-production.up.railway.app`
- **API:** `https://roadwarningsnarino-backend-production.up.railway.app/api`
- **Swagger:** `https://roadwarningsnarino-backend-production.up.railway.app/api/swagger-ui/index.html`
- **Alertas:** `https://roadwarningsnarino-backend-production.up.railway.app/api/alert`

### Frontend (Vercel):
- **URL:** `https://road-warnings-narino-frontend.vercel.app`

### Variable en Vercel:
```
VITE_API_URL=https://roadwarningsnarino-backend-production.up.railway.app/api
```

---

## Próximos Pasos (Opcional)

### Limpiar Datos de Prueba

Una vez que todo funcione, puedes limpiar los datos de prueba:

1. Ve a Railway → Base de datos **Postgres** → **Data**
2. Haz clic en **Query**
3. Ejecuta:
   ```sql
   DELETE FROM alerts;
   DELETE FROM gas_stations;
   DELETE FROM routes;
   DELETE FROM users WHERE username IN ('admin', 'moderador', 'juan_pasto');
   DELETE FROM refresh_tokens;
   ```

### Monitorear Uso de Railway

1. Railway → Dashboard → Tu proyecto
2. Ve a **Usage**
3. Verás cuánto has gastado de los $5 gratuitos mensuales

---

**¡Todo está listo! Sigue estos pasos en orden y tu aplicación estará funcionando en producción.**
