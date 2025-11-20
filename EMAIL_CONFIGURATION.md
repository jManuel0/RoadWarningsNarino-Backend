# 📧 Configuración de Email para RoadWarnings Nariño

## Estado Actual

Tu backend está configurado para enviar emails de verificación, pero **las credenciales SMTP no están configuradas**.

### ¿Qué significa esto?

- ✅ El código de registro funciona correctamente
- ✅ Se generan tokens de verificación
- ⚠️ Los emails NO se envían porque falta la configuración SMTP
- ⚠️ Verás warnings en los logs: `"Email no enviado (JavaMailSender no configurado)"`

---

## Solución: Configurar Gmail SMTP

### Opción 1: Usar Gmail (Recomendado para Testing)

#### Paso 1: Crear una "Contraseña de Aplicación" en Gmail

1. Ve a tu cuenta de Google: https://myaccount.google.com/
2. Navega a **Seguridad**
3. Activa **Verificación en dos pasos** (si no lo tienes activado)
4. Ve a **Contraseñas de aplicaciones**: https://myaccount.google.com/apppasswords
5. Selecciona:
   - App: **Correo**
   - Dispositivo: **Otro (nombre personalizado)** → Escribe "RoadWarnings Backend"
6. Haz clic en **Generar**
7. Copia la contraseña de 16 caracteres (sin espacios)

#### Paso 2: Configurar Variables de Entorno

**Para desarrollo local** (archivo `.env`):

```bash
# Email Configuration (Gmail SMTP)
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=tu_email@gmail.com
MAIL_PASSWORD=xxxx xxxx xxxx xxxx  # La contraseña de aplicación de 16 caracteres
```

**Para producción en Render**:

Ve a tu servicio en Render Dashboard → Environment → Add Environment Variable:

| Variable | Value |
|----------|-------|
| `MAIL_HOST` | `smtp.gmail.com` |
| `MAIL_PORT` | `587` |
| `MAIL_USERNAME` | `tu_email@gmail.com` |
| `MAIL_PASSWORD` | `xxxx xxxx xxxx xxxx` |

#### Paso 3: Agregar FRONTEND_URL

También necesitas configurar la URL del frontend para que los enlaces de verificación funcionen:

**Local (.env)**:
```bash
FRONTEND_URL=http://localhost:5173
```

**Render (Environment Variables)**:
```
FRONTEND_URL=https://road-warnings-narino-frontend.vercel.app
```

---

## Verificar Configuración

### 1. Verificar Variables de Entorno Locales

Tu archivo `.env` debería verse así:

```bash
# Database Configuration
DATABASE_URL=jdbc:postgresql://localhost:5432/roadwarnings
DB_USER=postgres
DB_PASSWORD=Roadwarnings2025

# JWT Configuration
JWT_SECRET=RoadWarningsNarino2024SecretKeyMustBeAtLeast256BitsLongForHS256Algorithm
JWT_EXPIRATION=86400000

# Email Configuration (NUEVO)
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=tu_email@gmail.com
MAIL_PASSWORD=xxxx xxxx xxxx xxxx

# Frontend URL (NUEVO)
FRONTEND_URL=http://localhost:5173

# Redis Configuration
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=
REDIS_TIMEOUT=2000
CACHE_TTL=600000

# Logging
LOG_LEVEL_ROOT=INFO
LOG_LEVEL_APP=DEBUG
LOG_LEVEL_WEB=DEBUG
LOG_LEVEL_HIBERNATE=INFO

# Swagger
SWAGGER_ENABLED=true

# File Upload
MAX_FILE_SIZE=5MB
MAX_REQUEST_SIZE=10MB

# Server Port
PORT=8080
```

### 2. Probar Localmente

Después de agregar las variables:

```bash
# Detener el backend si está corriendo
# Ctrl+C

# Reiniciar el backend
./mvnw spring-boot:run
```

Busca en los logs al inicio:
```
✅ BIEN: No deberías ver "JavaMailSender no configurado"
```

### 3. Probar el Envío de Email

Usa el script de prueba:

```powershell
# PowerShell
.\test-auth-endpoints.ps1
```

o

```bash
# Bash/Git Bash
bash test-auth-endpoints.sh
```

Revisa tu email (el que configuraste en `MAIL_USERNAME`) para ver si llegó el email de verificación.

---

## Alternativas a Gmail

### Opción 2: SendGrid (Gratuito hasta 100 emails/día)

1. Crea una cuenta en https://sendgrid.com/
2. Genera una API Key
3. Configura:

```bash
MAIL_HOST=smtp.sendgrid.net
MAIL_PORT=587
MAIL_USERNAME=apikey
MAIL_PASSWORD=tu_api_key_de_sendgrid
```

### Opción 3: Mailgun (Gratuito hasta 5,000 emails/mes)

1. Crea una cuenta en https://www.mailgun.com/
2. Verifica tu dominio o usa el sandbox domain
3. Obtén credenciales SMTP
4. Configura:

```bash
MAIL_HOST=smtp.mailgun.org
MAIL_PORT=587
MAIL_USERNAME=postmaster@tu-dominio.mailgun.org
MAIL_PASSWORD=tu_password_de_mailgun
```

### Opción 4: Mailtrap (Solo para Testing - NO envía emails reales)

Perfecto para desarrollo sin afectar emails reales:

1. Crea cuenta en https://mailtrap.io/
2. Obtén credenciales de tu inbox de prueba
3. Configura:

```bash
MAIL_HOST=smtp.mailtrap.io
MAIL_PORT=2525
MAIL_USERNAME=tu_username_mailtrap
MAIL_PASSWORD=tu_password_mailtrap
```

**Nota**: Con Mailtrap, los emails NO llegan a inboxes reales. Solo los ves en su interfaz web.

---

## Troubleshooting

### Problema: "Authentication failed"

**Solución**:
- Verifica que usaste una **Contraseña de Aplicación** de Gmail, NO tu contraseña normal
- Asegúrate de que la verificación en dos pasos esté activada
- Copia la contraseña sin espacios

### Problema: "Connection timeout"

**Solución**:
- Verifica que el puerto sea `587` (no `465` ni `25`)
- Verifica que `MAIL_HOST=smtp.gmail.com`
- Revisa tu firewall

### Problema: Los emails llegan a Spam

**Solución**:
- Usa un dominio verificado (no Gmail para producción)
- Configura SPF, DKIM y DMARC records
- Considera usar SendGrid o Mailgun para producción

### Problema: "JavaMailSender no configurado"

**Solución**:
- Verifica que las variables `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD` estén en `.env`
- Reinicia el backend después de agregar las variables
- Verifica que no haya errores de sintaxis en el archivo `.env`

---

## Verificar que Funciona

### En los Logs del Backend

Busca estos mensajes:

✅ **Éxito**:
```
Email simple enviado a: test@example.com
```

❌ **Error (sin configuración)**:
```
Email no enviado (JavaMailSender no configurado) - To: test@example.com
```

❌ **Error (credenciales incorrectas)**:
```
Error al enviar email simple a test@example.com: Authentication failed
```

### En tu Email

El email de verificación debería verse así:

```
De: noreply@roadwarnings.com (o tu MAIL_USERNAME)
Para: tu_email@example.com
Asunto: Verifica tu correo electrónico

Hola tu_username,

Gracias por registrarte en RoadWarnings Nariño.

Por favor verifica tu correo haciendo clic en el siguiente enlace:

https://road-warnings-narino-frontend.vercel.app/verify-email?token=abc123...

Este enlace expira en 24 horas.

Saludos,
El equipo de RoadWarnings Nariño
```

---

## Recomendaciones para Producción

### No uses Gmail directamente en producción

Gmail tiene límites:
- ⚠️ Máximo 500 emails/día
- ⚠️ Máximo 100 destinatarios por email
- ⚠️ Puede bloquear tu cuenta si detecta "actividad sospechosa"

### Usa un servicio profesional:

1. **SendGrid** (Recomendado)
   - ✅ 100 emails/día gratis
   - ✅ Escalable
   - ✅ Analytics incluido

2. **Mailgun**
   - ✅ 5,000 emails/mes gratis
   - ✅ API potente
   - ✅ Validación de emails

3. **Amazon SES**
   - ✅ Muy barato ($0.10 por 1,000 emails)
   - ✅ Alta entregabilidad
   - ⚠️ Requiere configuración más compleja

---

## Configuración Actual en tu Código

Tu `EmailService.java` ya está bien configurado:

- ✅ Maneja correctamente cuando `JavaMailSender` no está configurado (modo graceful)
- ✅ Usa `@Async` para no bloquear requests
- ✅ Tiene fallbacks y logging apropiado
- ✅ Soporta emails simples y HTML

Solo necesitas agregar las credenciales SMTP y funcionará perfectamente.

---

## Checklist de Configuración

### Para Desarrollo Local:

- [ ] Crear Contraseña de Aplicación en Gmail
- [ ] Agregar `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD` a `.env`
- [ ] Agregar `FRONTEND_URL=http://localhost:5173` a `.env`
- [ ] Reiniciar el backend
- [ ] Probar registro con el script `test-auth-endpoints.ps1`
- [ ] Verificar que llegó el email

### Para Producción (Render):

- [ ] Configurar variables de entorno en Render Dashboard:
  - `MAIL_HOST=smtp.gmail.com`
  - `MAIL_PORT=587`
  - `MAIL_USERNAME=tu_email@gmail.com`
  - `MAIL_PASSWORD=contraseña_de_aplicación`
  - `FRONTEND_URL=https://road-warnings-narino-frontend.vercel.app`
- [ ] Hacer redeploy del backend
- [ ] Probar registro desde el frontend en Vercel
- [ ] Verificar que el email llega correctamente

---

## Siguiente Paso

Una vez configurado el email, el flujo completo será:

1. Usuario se registra → ✅ Cuenta creada
2. Backend envía email → ✅ Email recibido
3. Usuario hace clic en el link → ✅ Email verificado
4. Usuario puede hacer login → ✅ Sesión iniciada

¿Necesitas ayuda con alguno de estos pasos?
