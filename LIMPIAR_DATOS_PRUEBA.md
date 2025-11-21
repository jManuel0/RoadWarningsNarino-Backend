# Cómo Limpiar los Datos de Prueba de la Base de Datos en Render

## Situación Actual

Tu backend en Render tiene datos de prueba que fueron creados antes de deshabilitar el `DataInitializer`. Necesitas eliminarlos para empezar con una base de datos limpia donde solo los usuarios registrados puedan crear alertas.

---

## Opción 1: Limpiar desde Render Dashboard (RECOMENDADO)

### Paso 1: Conectarte a la Base de Datos

1. Ve a tu dashboard de Render: https://dashboard.render.com/
2. Selecciona tu base de datos PostgreSQL (algo como `roadwarnings-database`)
3. En la parte superior, haz clic en **Connect** → **External Connection**
4. Copia el comando PSQL que aparece (se ve así):
   ```bash
   PGPASSWORD=xxx psql -h dpg-xxx.oregon-postgres.render.com -U roadwarnings_db_user roadwarnings_db
   ```

### Paso 2: Conectarte usando PSQL

**Si tienes PostgreSQL instalado localmente:**

Abre tu terminal (PowerShell o CMD) y ejecuta el comando que copiaste.

**Si NO tienes PostgreSQL instalado:**

Usa la **Web Shell** de Render:
1. En tu base de datos en Render, ve a la pestaña **Shell**
2. Esto te dará acceso directo a la base de datos

### Paso 3: Eliminar los Datos de Prueba

Una vez conectado, ejecuta estos comandos **UNO POR UNO**:

```sql
-- Ver cuántas alertas hay
SELECT COUNT(*) FROM alerts;

-- Ver cuántos usuarios hay
SELECT username FROM users;

-- Eliminar todas las alertas
DELETE FROM alerts;

-- Eliminar usuarios de prueba (mantén solo los usuarios reales que quieras conservar)
DELETE FROM users WHERE username IN ('admin', 'moderador', 'juan_pasto');

-- Si quieres eliminar TODOS los usuarios y empezar de cero:
DELETE FROM users;

-- Verificar que quedó todo limpio
SELECT COUNT(*) FROM alerts;
SELECT COUNT(*) FROM users;
```

**IMPORTANTE:** Si eliminas todos los usuarios, también se eliminarán todas las alertas asociadas automáticamente (por CASCADE).

---

## Opción 2: Crear un Endpoint Temporal para Limpiar (MÁS FÁCIL)

Voy a crear un endpoint temporal en tu backend que puedes llamar para limpiar los datos.

**Nota:** Este endpoint solo funcionará si tu usuario tiene el rol `ADMIN`.

---

## Opción 3: Reiniciar la Base de Datos Completa

Si quieres empezar completamente de cero:

### Paso 1: Cambiar JPA_DDL_AUTO temporalmente

1. Ve a tu servicio backend en Render
2. **Environment** → Busca `JPA_DDL_AUTO`
3. Cámbialo temporalmente de `update` a `create-drop`
4. Guarda los cambios (esto hará un redeploy)
5. Espera a que el servicio se reinicie
6. **IMPORTANTE:** Vuelve a cambiarlo a `update` inmediatamente

**Advertencia:** Esto borrará TODAS las tablas y las volverá a crear vacías.

### Paso 2: Verificar que quedó limpio

Ejecuta:
```bash
curl https://roadwarningsnarino-backend.onrender.com/api/alert
```

Debería devolver: `[]`

---

## Opción 4: Crear Script SQL de Limpieza

Puedes ejecutar este script SQL desde la Shell de Render:

```sql
-- Script de limpieza completa
BEGIN;

-- Eliminar todas las alertas (también elimina media y roads asociados por CASCADE)
DELETE FROM alert_affected_roads;
DELETE FROM alert_media;
DELETE FROM alerts;

-- Eliminar todas las estaciones de gasolina
DELETE FROM gas_stations;

-- Eliminar todas las rutas
DELETE FROM routes;

-- Eliminar usuarios de prueba (modifica según tus necesidades)
DELETE FROM users WHERE username IN ('admin', 'moderador', 'juan_pasto');

-- O eliminar TODOS los usuarios:
-- DELETE FROM users;

-- Eliminar refresh tokens
DELETE FROM refresh_tokens;

COMMIT;

-- Verificar
SELECT 'Alerts restantes:' AS tabla, COUNT(*) AS total FROM alerts
UNION ALL
SELECT 'Users restantes:', COUNT(*) FROM users
UNION ALL
SELECT 'Gas Stations:', COUNT(*) FROM gas_stations
UNION ALL
SELECT 'Routes:', COUNT(*) FROM routes;
```

---

## ¿Cuál Opción Usar?

| Opción | Dificultad | Tiempo | Recomendado Para |
|--------|-----------|--------|------------------|
| Opción 1 (PSQL) | Media | 5 min | Si sabes usar SQL |
| Opción 2 (Endpoint) | Fácil | 2 min | Si prefieres API |
| Opción 3 (create-drop) | Fácil | 5 min | Si quieres empezar 100% de cero |
| Opción 4 (Script SQL) | Media | 3 min | Si quieres eliminar selectivamente |

---

## Recomendación

Te recomiendo **Opción 2** porque:
1. Es la más fácil
2. No requiere instalar nada
3. Puedes ejecutarla desde el navegador
4. Es segura (solo admin puede usarla)

¿Quieres que cree el endpoint temporal para limpiar los datos?

---

## Después de Limpiar

Una vez que limpies los datos:

1. ✅ La base de datos estará vacía
2. ✅ Los usuarios podrán registrarse normalmente
3. ✅ Solo las alertas creadas por usuarios reales aparecerán
4. ✅ No volverán a aparecer datos de prueba (ya deshabilitaste el DataInitializer)

---

## Notas Importantes

- El `DataInitializer` ya está deshabilitado (comentaste el `@Component`)
- Los datos que ves ahora son los que se crearon ANTES de deshabilitarlo
- Una vez que los elimines, NO volverán a aparecer
- Los nuevos registros que se hagan serán permanentes
