# Guía Completa: Desplegar Backend en Railway

## ✅ Ventajas de Railway sobre Render

- ✅ $5 de crédito gratis al mes (sin tarjeta de crédito)
- ✅ Despliegues ilimitados (no hay límite de minutos de build)
- ✅ Más rápido que Render
- ✅ Interfaz más intuitiva
- ✅ PostgreSQL incluido gratis

---

## Paso 1: Crear Cuenta en Railway

1. Ve a: <https://railway.app/>
2. Haz clic en **Login** (arriba derecha)
3. Selecciona **Login with GitHub**
4. Autoriza a Railway para acceder a tus repositorios
5. ✅ Railway te dará **$5 de crédito gratis** cada mes (sin tarjeta)

---

## Paso 2: Crear Nuevo Proyecto

1. En el dashboard de Railway, haz clic en **New Project**
2. Selecciona **Deploy from GitHub repo**
3. Busca y selecciona: `RoadWarningsnarino-backend` (o `RoadWarningsNarino-Backend`)
4. Haz clic en **Deploy Now**

Railway comenzará a analizar tu proyecto.

---

## Paso 3: Configurar la Base de Datos PostgreSQL

1. En tu proyecto de Railway, haz clic en **New** (botón morado arriba derecha)
2. Selecciona **Database** → **Add PostgreSQL**
3. Railway creará automáticamente una base de datos PostgreSQL
4. Espera 30 segundos a que se cree

---

## Paso 4: Obtener las Credenciales de la Base de Datos

1. Haz clic en el servicio **Postgres** (en tu proyecto)
2. Ve a la pestaña **Variables**
3. Verás estas variables generadas automáticamente:
   - `DATABASE_URL`
   - `PGDATABASE`
   - `PGHOST`
   - `PGPASSWORD`
   - `PGPORT`
   - `PGUSER`

**IMPORTANTE:** Railway usa formato `postgres://` pero Java necesita `jdbc:postgresql://`

---

## Paso 5: Configurar Variables de Entorno del Backend

1. Haz clic en tu servicio backend (el que dice `roadwarningsnarino-backend`)
2. Ve a la pestaña **Variables**
3. Haz clic en **New Variable** y agrega estas variables **UNA POR UNA**:

### Variables Obligatorias

```bash
# Database (Railway conecta automáticamente si usas ${{Postgres.xxx}})
DATABASE_URL=${{Postgres.DATABASE_URL}}
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

**IMPORTANTE sobre DATABASE_URL:**

Railway te da la URL en formato `postgres://...`, pero Java Spring Boot necesita `jdbc:postgresql://...`

Railway puede hacer la conversión automática usando `${{Postgres.DATABASE_URL}}`, pero si no funciona, conviértela manualmente:

**Ejemplo:**

```
# Railway te da:
postgres://postgres:password@containers-us-west-123.railway.app:5432/railway

# Tú usas:
jdbc:postgresql://containers-us-west-123.railway.app:5432/railway
```

---

## Paso 6: Configurar el Build

Railway detecta automáticamente que es un proyecto Java/Maven, pero asegúrate de configurar:

1. En tu servicio backend, ve a **Settings**
2. Verifica que:
   - **Build Command:** `./mvnw clean package -DskipTests`
   - **Start Command:** `java -jar target/*.jar`
3. Si no están configurados, agrégalos en **Settings** → **Deploy**

---

## Paso 7: Desplegar

1. Railway desplegará automáticamente después de configurar las variables
2. Verás los logs en tiempo real
3. Espera 3-5 minutos a que termine el build
4. Cuando veas `Started NarinoApplication in X.XXX seconds`, está listo

---

## Paso 8: Obtener la URL del Backend

1. En tu servicio backend, ve a **Settings**
2. Busca la sección **Networking**
3. Haz clic en **Generate Domain**
4. Railway generará una URL como:
   ```
   https://roadwarningsnarino-backend-production.up.railway.app
   ```
5. **Copia esta URL** - la necesitarás para Vercel

---

## Paso 9: Probar el Backend

Abre tu navegador y ve a:

```
https://TU-URL-DE-RAILWAY.up.railway.app/api/swagger-ui/index.html
```

Deberías ver la documentación de la API (Swagger).

También prueba:
```
https://TU-URL-DE-RAILWAY.up.railway.app/api/alert
```

Debería devolver un array de alertas (o vacío `[]` si limpiaste los datos).

---

## Paso 10: Actualizar Vercel con la Nueva URL

1. Ve a: https://vercel.com/dashboard
2. Selecciona tu proyecto: `road-warnings-narino-frontend`
3. **Settings** → **Environment Variables**
4. Busca la variable `VITE_API_URL` (o `NEXT_PUBLIC_API_URL`)
5. **Edítala** y cambia el valor a:

   ```
   https://TU-URL-DE-RAILWAY.up.railway.app/api
   ```

6. Haz clic en **Save**
7. Ve a **Deployments** → Redeploy

---

## Paso 11: Verificar que Todo Funciona

1. Abre: <https://road-warnings-narino-frontend.vercel.app>
2. Abre la consola del navegador (F12) → **Network**
3. Intenta registrar un usuario
4. Verifica que la petición vaya a: `https://TU-URL-DE-RAILWAY.up.railway.app/api/auth/register`
5. Si es exitoso, ¡ya está funcionando! 🎉

---

## Paso 12: Limpiar Datos de Prueba (Opcional)

Si quieres limpiar los datos de prueba:

1. En Railway, haz clic en tu base de datos **Postgres**
2. Ve a la pestaña **Data**
3. Haz clic en **Query**
4. Ejecuta:

   ```sql
   DELETE FROM alerts;
   DELETE FROM gas_stations;
   DELETE FROM routes;
   DELETE FROM users WHERE username IN ('admin', 'moderador', 'juan_pasto');
   DELETE FROM refresh_tokens;
   ```

---

## Diferencias Importantes: Railway vs Render

| Característica | Railway | Render |
|---------------|---------|--------|
| Crédito gratis | $5/mes | 750 horas build/mes |
| Despliegues | Ilimitados | Limitados por minutos |
| Base de datos gratis | ✅ PostgreSQL | ✅ PostgreSQL |
| Velocidad | Más rápido | Más lento |
| Precio después del free tier | Pay-as-you-go | $7/mes por servicio |

---

## Configuración Adicional de Railway

### Conectar con GitHub para Auto-Deploy

Railway ya está conectado con GitHub, así que cada vez que hagas `git push origin main`, Railway detectará los cambios y redeployará automáticamente.

### Ver Logs

1. Haz clic en tu servicio backend
2. Ve a la pestaña **Deployments**
3. Haz clic en el deployment activo
4. Verás los logs en tiempo real

### Variables de Entorno Compartidas

Railway permite que tu backend use las variables de la base de datos automáticamente:

```bash
DATABASE_URL=${{Postgres.DATABASE_URL}}
DB_USER=${{Postgres.PGUSER}}
DB_PASSWORD=${{Postgres.PGPASSWORD}}
```

Esto hace que las credenciales se actualicen automáticamente si cambias la base de datos.

---

## Solución de Problemas Comunes

### Error: "Failed to connect to database"

**Causa:** `DATABASE_URL` tiene formato incorrecto

**Solución:** Asegúrate de que empiece con `jdbc:postgresql://` (no `postgres://`)

### Error: "Port 8080 already in use"

**Causa:** Railway usa una variable `PORT` dinámica

**Solución:** Ya está configurado con `PORT=8080`, pero Railway puede sobrescribirlo automáticamente

### El backend se despliega pero no responde

**Causa:** Las variables de entorno no están configuradas

**Solución:** Verifica que todas las variables obligatorias estén agregadas

---

## Migrar Datos de Render a Railway (Opcional)

Si quieres migrar los datos existentes de Render a Railway:

### Opción 1: Exportar/Importar SQL

**Desde Render:**
1. Conecta a tu base de datos de Render
2. Exporta:
   ```bash
   pg_dump -h dpg-xxx.oregon-postgres.render.com -U usuario -d database > backup.sql
   ```

**A Railway:**
1. Ve a Railway → Postgres → **Data** → **Query**
2. Copia y pega el contenido de `backup.sql`

### Opción 2: Empezar de Cero (RECOMENDADO)

Dado que tienes datos de prueba, te recomiendo empezar con una base de datos limpia en Railway.

---

## Checklist de Migración ✅

- [ ] Cuenta creada en Railway
- [ ] Proyecto creado desde GitHub
- [ ] Base de datos PostgreSQL agregada
- [ ] Variables de entorno configuradas
- [ ] Backend desplegado exitosamente
- [ ] URL del backend obtenida
- [ ] Vercel actualizado con nueva URL
- [ ] Frontend redeployado en Vercel
- [ ] Probado registro y login en producción

---

## URLs Finales

### Railway:
- **Backend:** `https://tu-proyecto.up.railway.app`
- **Swagger:** `https://tu-proyecto.up.railway.app/api/swagger-ui/index.html`
- **Dashboard:** https://railway.app/dashboard

### Vercel:
- **Frontend:** `https://road-warnings-narino-frontend.vercel.app`

---

## Costos Estimados

Con el plan gratuito de Railway ($5/mes):

- Backend: ~$3-4/mes
- PostgreSQL: ~$1-2/mes
- **Total: ~$5/mes (GRATIS con el crédito mensual)**

**Nota:** Si excedes los $5/mes, Railway te cobrará. Puedes configurar un límite de gasto en **Settings** → **Usage**.

---

## Próximos Pasos

Una vez que todo funcione:

1. ✅ Puedes eliminar tu proyecto de Render (si quieres)
2. ✅ Configura notificaciones de despliegue en Railway
3. ✅ Monitorea el uso en Railway Dashboard
4. ✅ Configura backups automáticos de la base de datos (Railway Pro)

---

**¿Listo para empezar? Sigue los pasos y cuando tengas tu URL de Railway, actualiza Vercel y estarás listo!**
