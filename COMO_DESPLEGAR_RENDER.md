# Guía Rápida: Cómo Desplegar el Backend en Render

## 🎯 Situación Actual

- ✅ Frontend desplegado en Vercel: `https://road-warnings-narino-frontend.vercel.app`
- ❌ Backend NO está desplegado todavía
- ✅ Código del backend en GitHub: `https://github.com/jManuel0/RoadWarningsnarino-backend.git`

**Necesitamos:** Desplegar el backend en Render.com

---

## Paso 1: Crear Cuenta en Render (si no la tienes)

1. Ve a: https://render.com/
2. Haz clic en **Get Started** o **Sign Up**
3. Elige **Sign up with GitHub**
4. Autoriza a Render para acceder a tus repositorios

---

## Paso 2: Crear Base de Datos PostgreSQL en Render

**IMPORTANTE:** El backend necesita una base de datos PostgreSQL. Créala primero.

1. En el dashboard de Render, haz clic en **New +** (arriba derecha)
2. Selecciona **PostgreSQL**
3. Configura:
   - **Name:** `roadwarnings-database` (o el nombre que prefieras)
   - **Database:** `roadwarnings_db`
   - **User:** Se genera automáticamente
   - **Region:** `Oregon (US West)` (o el más cercano a Colombia)
   - **Plan:** **Free** (para empezar)
4. Haz clic en **Create Database**
5. Espera 1-2 minutos a que se cree

**Importante:** Después de crear la base de datos, ve a la pestaña **Info** y copia:
- **Internal Database URL** (empieza con `postgres://...`)
- **Username**
- **Password**

**¡Guarda estos valores! Los necesitarás en el siguiente paso.**

---

## Paso 3: Crear Web Service para el Backend

1. En el dashboard de Render, haz clic en **New +** → **Web Service**
2. Conecta tu repositorio de GitHub:
   - Si no aparece, haz clic en **Configure GitHub App**
   - Selecciona: `jManuel0/RoadWarningsnarino-backend`
   - Haz clic en **Connect**

3. Configura el servicio:

   **Name:** `roadwarnings-backend` (o el nombre que prefieras)

   **Region:** `Oregon (US West)` (el mismo que la base de datos)

   **Branch:** `main`

   **Root Directory:** (déjalo vacío)

   **Runtime:** `Java`

   **Build Command:**
   ```bash
   ./mvnw clean package -DskipTests
   ```

   **Start Command:**
   ```bash
   java -jar target/*.jar
   ```

   **Plan:** **Free** (para empezar)

4. **NO hagas clic en "Create Web Service" todavía**

---

## Paso 4: Configurar Variables de Entorno

Antes de crear el servicio, necesitas agregar las variables de entorno.

Haz scroll hacia abajo hasta **Environment Variables** y haz clic en **Add Environment Variable**.

Agrega las siguientes variables **UNA POR UNA**:

### Variables Obligatorias:

```bash
DATABASE_URL=jdbc:postgresql://dpg-XXXX-a.oregon-postgres.render.com/roadwarnings_db
# ↑ Reemplaza con tu Internal Database URL (cambia postgres:// por jdbc:postgresql://)

DB_USER=roadwarnings_db_user
# ↑ Reemplaza con el username que copiaste de la base de datos

DB_PASSWORD=XXXXXXXXXXXXXXXXXXXX
# ↑ Reemplaza con la password que copiaste de la base de datos

JWT_SECRET=MiSecretoSuperSeguroParaProduccion2024RoadWarnings123456789
# ↑ Puedes usar este o generar uno nuevo (mínimo 32 caracteres)

APP_AUTH_REQUIRE_EMAIL_VERIFICATION=false
# ↑ Muy importante: false para permitir login sin verificación

FRONTEND_URL=https://road-warnings-narino-frontend.vercel.app
# ↑ Tu URL de Vercel

JPA_DDL_AUTO=update
# ↑ Para que actualice las tablas sin borrarlas

PORT=8080
# ↑ Puerto por defecto
```

### Variables Opcionales (agregar si quieres):

```bash
JPA_SHOW_SQL=false
JPA_FORMAT_SQL=false
LOG_LEVEL_ROOT=INFO
LOG_LEVEL_APP=INFO
SWAGGER_ENABLED=true
```

---

## Paso 5: Crear el Servicio

1. Después de agregar todas las variables, haz clic en **Create Web Service**
2. Render comenzará a construir y desplegar tu backend
3. Este proceso toma **5-10 minutos** la primera vez
4. Verás los logs en tiempo real

**Espera a que aparezca:**
```
Started NarinoApplication in X.XXX seconds
```

---

## Paso 6: Obtener la URL del Backend

Una vez que el deployment termine:

1. En la parte superior de la página verás tu URL:
   ```
   https://roadwarnings-backend-XXXX.onrender.com
   ```
2. **Copia esta URL completa**

---

## Paso 7: Probar el Backend

Abre tu navegador y ve a:
```
https://TU-URL-DE-RENDER.onrender.com/api/swagger-ui/index.html
```

Deberías ver la documentación de la API (Swagger).

También puedes probar:
```
https://TU-URL-DE-RENDER.onrender.com/api/alert
```

Debería devolver un array vacío: `[]`

---

## Paso 8: Configurar Vercel con la URL del Backend

Ahora que tienes la URL del backend, ve a tu proyecto en Vercel:

1. https://vercel.com/dashboard
2. Selecciona: `road-warnings-narino-frontend`
3. **Settings** → **Environment Variables** → **Add New**

**Para React/Vite:**
```
Name:  VITE_API_URL
Value: https://TU-URL-DE-RENDER.onrender.com/api
```

**Para Next.js:**
```
Name:  NEXT_PUBLIC_API_URL
Value: https://TU-URL-DE-RENDER.onrender.com/api
```

4. Selecciona: **Production, Preview, and Development**
5. Haz clic en **Save**
6. Ve a **Deployments** → Último deployment → **...** → **Redeploy**

---

## Paso 9: Testing Final

1. Abre: https://road-warnings-narino-frontend.vercel.app
2. Abre la consola del navegador (F12) → Pestaña **Network**
3. Intenta registrar un usuario
4. Verifica que la petición vaya a: `https://TU-URL.onrender.com/api/auth/register`
5. Si el registro es exitoso, deberías entrar automáticamente

---

## Errores Comunes

### Error: "Build failed"
**Causa:** Variables de entorno incorrectas o faltantes
**Solución:** Verifica que agregaste todas las variables obligatorias

### Error: "Connection refused" en los logs
**Causa:** La base de datos no está corriendo o las credenciales son incorrectas
**Solución:** Verifica que la base de datos esté en "Available" y que las credenciales sean correctas

### Error: El servicio se reinicia constantemente
**Causa:** Probablemente el `DATABASE_URL` está mal formado
**Solución:** Asegúrate de que empiece con `jdbc:postgresql://` (no `postgres://`)

### Error: "Failed to fetch" desde el frontend
**Causa:** La variable de entorno en Vercel no está configurada
**Solución:** Sigue el Paso 8

---

## Conversión de Database URL

Render te da la URL en este formato:
```
postgres://username:password@host:5432/database
```

Necesitas convertirla a:
```
jdbc:postgresql://host:5432/database
```

**Ejemplo:**
```
# Render te da:
postgres://roadwarnings_db_user:abc123@dpg-abc123-a.oregon-postgres.render.com:5432/roadwarnings_db

# Tú usas en DATABASE_URL:
jdbc:postgresql://dpg-abc123-a.oregon-postgres.render.com:5432/roadwarnings_db
```

---

## ¿Necesitas Ayuda?

Si encuentras algún error durante el deployment:

1. Ve a tu servicio en Render
2. Haz clic en **Logs**
3. Copia el error
4. Compártelo para ayudarte

---

## Plan Free de Render - Limitaciones

⚠️ **Importante:** El plan free de Render tiene estas limitaciones:

- El servicio se "duerme" después de 15 minutos sin tráfico
- La primera petición después de dormir toma 30-60 segundos
- La base de datos PostgreSQL free expira después de 90 días (necesitarás migrar los datos)

**Solución para que no se duerma:**
Puedes usar un servicio de ping como **UptimeRobot** o **Cron-job.org** para hacer peticiones cada 10 minutos a:
```
https://TU-URL.onrender.com/api/alert
```

---

## Resumen

1. ✅ Crear base de datos PostgreSQL en Render
2. ✅ Crear Web Service en Render
3. ✅ Configurar variables de entorno
4. ✅ Obtener URL del backend
5. ✅ Configurar Vercel con la URL
6. ✅ Redeploy en Vercel
7. ✅ Probar registro y login

---

**¡Una vez que despliegues en Render, compárteme la URL del backend y te ayudo con el resto!**
