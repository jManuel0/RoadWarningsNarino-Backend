# Guía de Despliegue en la Nube

## Problema de Registro/Login en Producción

Si el registro o login **no funciona en producción**, el problema más común es que la verificación de email está habilitada pero no has configurado el servidor de correo.

### Solución Rápida

**Opción 1: Deshabilitar verificación de email (Recomendado para desarrollo)**

En tu panel de Render (o tu plataforma cloud), agrega esta variable de entorno:

```
APP_AUTH_REQUIRE_EMAIL_VERIFICATION=false
```

**Opción 2: Configurar servidor de email (Para producción)**

Si quieres mantener la verificación de email, configura estas variables:

```
APP_AUTH_REQUIRE_EMAIL_VERIFICATION=true
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=tu-email@gmail.com
MAIL_PASSWORD=tu-app-password
FRONTEND_URL=https://tu-frontend.vercel.app
```

---

## Configuración en Render

### Paso 1: Variables de Entorno Obligatorias

En tu dashboard de Render, ve a **Environment** y agrega:

#### Base de Datos
```
DATABASE_URL=jdbc:postgresql://[tu-host]:5432/[tu-db]
DB_USER=tu-usuario
DB_PASSWORD=tu-password
```

#### JWT (Seguridad)
```
JWT_SECRET=UnSecretoMuyLargoYSeguroDeAlMenos256BitsParaHS256Algorithm
JWT_EXPIRATION=86400000
```

#### Autenticación (IMPORTANTE)
```
APP_AUTH_REQUIRE_EMAIL_VERIFICATION=false
```

#### Frontend
```
FRONTEND_URL=https://road-warnings-narino-frontend.vercel.app
```

### Paso 2: Variables de Entorno Opcionales

```
JPA_DDL_AUTO=update
JPA_SHOW_SQL=false
LOG_LEVEL_ROOT=INFO
LOG_LEVEL_APP=INFO
SWAGGER_ENABLED=true
```

### Paso 3: Build Settings

- **Build Command**: `./mvnw clean package -DskipTests`
- **Start Command**: `java -jar target/roadwarnings-narino-backend-0.0.1-SNAPSHOT.jar`

---

## Endpoints de Autenticación

Una vez desplegado, usa estos endpoints (reemplaza `tu-app.onrender.com` con tu URL):

### Registro
```bash
POST https://tu-app.onrender.com/api/auth/register
Content-Type: application/json

{
  "username": "usuario123",
  "email": "usuario@example.com",
  "password": "Password123"
}
```

### Login (con username)
```bash
POST https://tu-app.onrender.com/api/auth/login
Content-Type: application/json

{
  "username": "usuario123",
  "password": "Password123"
}
```

### Login (con email)
```bash
POST https://tu-app.onrender.com/api/auth/login
Content-Type: application/json

{
  "username": "usuario@example.com",
  "password": "Password123"
}
```

---

## Verificar que funciona

1. **Health Check**:
   ```bash
   curl https://tu-app.onrender.com/api/ping
   ```

2. **Probar Registro**:
   ```bash
   curl -X POST https://tu-app.onrender.com/api/auth/register \
     -H "Content-Type: application/json" \
     -d '{"username":"test123","email":"test@test.com","password":"Test123456"}'
   ```

3. **Verificar respuesta exitosa**:
   ```json
   {
     "token": "eyJhbGc...",
     "refreshToken": "abc123...",
     "expiresIn": 86400,
     "username": "test123"
   }
   ```

---

## Troubleshooting

### Error: "Debes verificar tu correo electrónico"

**Causa**: `APP_AUTH_REQUIRE_EMAIL_VERIFICATION=true` pero no hay servidor de correo configurado.

**Solución**: Agrega la variable de entorno:
```
APP_AUTH_REQUIRE_EMAIL_VERIFICATION=false
```

### Error: CORS

**Causa**: El frontend no está en la lista de orígenes permitidos.

**Solución**: Verifica que en [SecurityConfig.java](src/main/java/com/roadwarnings/narino/security/SecurityConfig.java) el `@CrossOrigin(origins = "*")` esté presente, o agrega tu URL específica.

### Error: 502 Bad Gateway

**Causa**: El servidor no está iniciando correctamente.

**Solución**:
1. Verifica que todas las variables de entorno obligatorias estén configuradas
2. Revisa los logs en Render
3. Verifica que la base de datos esté accesible

### Error: Connection timeout

**Causa**: La base de datos de PostgreSQL no es accesible.

**Solución**:
- Verifica que `DATABASE_URL` tenga el formato correcto
- Asegúrate de que la base de datos esté en la misma región de Render
- Verifica las credenciales

---

## Configuración de CORS para Producción

Si necesitas restringir CORS solo a tu frontend, modifica `AuthController.java`:

```java
@CrossOrigin(origins = "${app.frontend.url}")
```

Y agrega en variables de entorno:
```
FRONTEND_URL=https://road-warnings-narino-frontend.vercel.app
```

---

## Despliegue Automático

Para habilitar despliegue automático desde GitHub:

1. Ve a tu servicio en Render
2. Settings → Build & Deploy
3. Auto-Deploy: **Yes**
4. Branch: **main**

Cada push a `main` desplegará automáticamente.

---

## Archivo render.yaml

Hemos incluido un archivo `render.yaml` en la raíz del proyecto que pre-configura todo. Solo necesitas:

1. Conectar tu repositorio en Render
2. Render detectará automáticamente `render.yaml`
3. Configurar las variables secretas (`DATABASE_URL`, `JWT_SECRET`, etc.)

---

## Checklist de Despliegue

- [ ] Base de datos PostgreSQL creada
- [ ] Variable `DATABASE_URL` configurada
- [ ] Variable `DB_USER` configurada
- [ ] Variable `DB_PASSWORD` configurada
- [ ] Variable `JWT_SECRET` configurada (mínimo 256 bits)
- [ ] Variable `APP_AUTH_REQUIRE_EMAIL_VERIFICATION=false` configurada
- [ ] Variable `FRONTEND_URL` configurada
- [ ] Build command: `./mvnw clean package -DskipTests`
- [ ] Start command: `java -jar target/roadwarnings-narino-backend-0.0.1-SNAPSHOT.jar`
- [ ] Probado endpoint `/api/ping`
- [ ] Probado registro `/api/auth/register`
- [ ] Probado login `/api/auth/login`

---

## Notas Importantes

1. **JPA_DDL_AUTO**: En producción usa `update`, **nunca** `create-drop` (borraría todos los datos al reiniciar)

2. **JWT_SECRET**: Debe ser diferente al de desarrollo y tener al menos 256 bits (32 caracteres)

3. **Logs**: En producción mantén `LOG_LEVEL_APP=INFO` para no saturar los logs

4. **Email**: Si no configuras email, debes tener `APP_AUTH_REQUIRE_EMAIL_VERIFICATION=false`

---

## Soporte

Si tienes problemas:
1. Revisa los logs en Render Dashboard
2. Verifica que todas las variables de entorno estén configuradas
3. Prueba los endpoints con curl o Postman
4. Verifica que la base de datos esté corriendo
