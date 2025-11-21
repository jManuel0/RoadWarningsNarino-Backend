# Cómo Limpiar los Datos de Prueba - Guía Rápida

## ✅ Endpoints Creados

He agregado 2 endpoints nuevos al `AdminController` para que puedas limpiar los datos fácilmente:

1. **DELETE `/api/admin/clear-test-data`** - Elimina datos de prueba específicos
2. **DELETE `/api/admin/clear-all`** - Elimina TODOS los datos (usar con precaución)

---

## Opción 1: Limpiar Solo Datos de Prueba (RECOMENDADO)

Este endpoint elimina:
- ✅ Todas las alertas
- ✅ Usuarios de prueba: `admin`, `moderador`, `juan_pasto`
- ✅ Todas las estaciones de gasolina
- ✅ Todas las rutas
- ❌ NO elimina otros usuarios reales que se hayan registrado

### Cómo Usar:

**Desde tu navegador o Postman:**

1. Primero necesitas crear un usuario admin. Ejecuta esto en tu navegador (consola F12):

```javascript
// 1. Registrar un usuario admin temporalmente
fetch('https://roadwarningsnarino-backend.onrender.com/api/auth/register', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    username: 'miadmin',
    email: 'admin@roadwarnings.com',
    password: 'Admin123'
  })
})
.then(res => res.json())
.then(data => {
  console.log('Token:', data.token);
  localStorage.setItem('adminToken', data.token);
});
```

2. **IMPORTANTE:** Ahora necesitas cambiar el rol de este usuario a ADMIN desde la base de datos:
   - Ve a tu base de datos en Render → Shell
   - Ejecuta:
   ```sql
   UPDATE users SET role = 'ADMIN' WHERE username = 'miadmin';
   ```

3. Ahora ejecuta el endpoint para limpiar:

```javascript
// 2. Limpiar datos de prueba
const token = localStorage.getItem('adminToken');

fetch('https://roadwarningsnarino-backend.onrender.com/api/admin/clear-test-data', {
  method: 'DELETE',
  headers: {
    'Authorization': `Bearer ${token}`
  }
})
.then(res => res.json())
.then(data => console.log('Resultado:', data));
```

**Respuesta esperada:**
```json
{
  "success": true,
  "message": "Datos de prueba eliminados exitosamente",
  "deleted": {
    "alerts": 10,
    "users": "admin, moderador, juan_pasto",
    "gasStations": 3,
    "routes": 3
  },
  "remaining": {
    "alerts": 0,
    "users": 1,
    "gasStations": 0,
    "routes": 0
  }
}
```

---

## Opción 2: Limpiar TODO (Usar solo si quieres empezar de cero)

Este endpoint elimina ABSOLUTAMENTE TODO:
- ✅ Todas las alertas
- ✅ TODOS los usuarios (incluyendo el tuyo)
- ✅ Todas las estaciones de gasolina
- ✅ Todas las rutas

### Cómo Usar:

```javascript
const token = localStorage.getItem('adminToken');

fetch('https://roadwarningsnarino-backend.onrender.com/api/admin/clear-all', {
  method: 'DELETE',
  headers: {
    'Authorization': `Bearer ${token}`
  }
})
.then(res => res.json())
.then(data => console.log('Resultado:', data));
```

---

## Opción 3: Desde la Base de Datos Directamente (SQL)

Si prefieres hacerlo directamente desde SQL:

1. Ve a tu dashboard de Render → Base de datos → **Shell**
2. Ejecuta estos comandos:

```sql
-- Ver qué hay actualmente
SELECT COUNT(*) as total_alerts FROM alerts;
SELECT username FROM users;

-- Eliminar todas las alertas
DELETE FROM alerts;

-- Eliminar usuarios de prueba (mantiene otros usuarios)
DELETE FROM users WHERE username IN ('admin', 'moderador', 'juan_pasto');

-- Eliminar estaciones de gasolina
DELETE FROM gas_stations;

-- Eliminar rutas
DELETE FROM routes;

-- Limpiar tokens
DELETE FROM refresh_tokens;

-- Verificar que quedó limpio
SELECT COUNT(*) FROM alerts;
SELECT COUNT(*) FROM users;
```

---

## Después de Limpiar

Una vez que limpies los datos:

1. ✅ Ve a: https://roadwarningsnarino-backend.onrender.com/api/alert
2. ✅ Debería devolver un array vacío: `[]`
3. ✅ Los usuarios podrán registrarse normalmente
4. ✅ Solo las alertas creadas por usuarios reales aparecerán
5. ✅ NO volverán a aparecer datos de prueba (ya deshabilitaste el `DataInitializer`)

---

## Verificar que el Endpoint Funciona

Primero verifica que el deployment en Render haya terminado:

1. Ve a https://dashboard.render.com/
2. Selecciona tu servicio backend
3. Verifica que el último deployment diga "Live"
4. Espera a que termine de desplegar (2-5 minutos)

Luego prueba:

```javascript
// Ver estadísticas actuales
fetch('https://roadwarningsnarino-backend.onrender.com/api/admin/stats', {
  headers: {
    'Authorization': `Bearer ${tu_token_admin}`
  }
})
.then(res => res.json())
.then(data => console.log('Stats:', data));
```

---

## Resumen de Opciones

| Método | Qué Elimina | Dificultad | Recomendado |
|--------|-------------|-----------|-------------|
| Endpoint `/clear-test-data` | Solo datos de prueba | Media | ✅ SÍ |
| Endpoint `/clear-all` | TODO | Fácil | Solo si quieres empezar de cero |
| SQL Directo | Lo que tú elijas | Media | Si sabes SQL |

---

## ⚠️ Notas Importantes

1. **Estos endpoints requieren autenticación** - Solo usuarios con rol `ADMIN` pueden usarlos
2. **El DataInitializer ya está deshabilitado** - Los datos no volverán a aparecer
3. **Espera a que Render despliegue** - Los cambios tardan 2-5 minutos en estar disponibles
4. **Después de limpiar, puedes eliminar estos endpoints** - Si no los necesitas más

---

## Para Eliminar los Endpoints Después (Opcional)

Si quieres eliminar estos endpoints después de limpiar:

1. Abre `AdminController.java`
2. Elimina los métodos `clearTestData()` y `clearAllData()`
3. Haz commit y push
4. Render redeployará automáticamente

---

**¿Prefieres que te ayude con alguna de estas opciones específicamente?**
