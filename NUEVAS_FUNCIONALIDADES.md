# 🚀 ROADWARNINGS NARIÑO - NUEVAS FUNCIONALIDADES IMPLEMENTADAS

## 📋 Índice
1. [Resumen de Nuevas Funcionalidades](#resumen)
2. [Entidades Nuevas](#entidades)
3. [Servicios Implementados](#servicios)
4. [Endpoints API](#endpoints)
5. [Configuración](#configuracion)
6. [WebSocket en Tiempo Real](#websocket)
7. [Sistema de Estadísticas y Reputación](#estadisticas)
8. [Sistema de Badges (Logros)](#badges)
9. [Notificaciones Push](#notificaciones)
10. [Rate Limiting](#rate-limiting)
11. [Caché con Redis](#cache)

---

## 🎯 RESUMEN

Se han implementado **TODAS** las funcionalidades faltantes del backend:

### ✅ Funcionalidades Críticas Implementadas:

1. **Subida de Imágenes con Cloudinary** ☁️
2. **WebSocket para Actualizaciones en Tiempo Real** 🔴
3. **Notificaciones Push con Firebase Cloud Messaging** 📱
4. **Scheduler para Expiración Automática de Alertas** ⏰
5. **Sistema de Favoritos (Rutas y Alertas)** ⭐
6. **Sistema de Reportes y Moderación** 🚩
7. **Estadísticas de Usuario** 📊
8. **Sistema de Reputación y Niveles** 🏆
9. **Sistema de Badges (Logros)** 🎖️
10. **Rate Limiting** 🛡️
11. **Servicio de Email** 📧
12. **Caché con Redis** 💾
13. **Actuator y Health Checks** ❤️
14. **Filtros Avanzados** 🔍

---

## 📦 ENTIDADES NUEVAS

### 1. **Notification**
Almacena notificaciones internas del sistema para cada usuario.

```java
- id
- user (User)
- type (NotificationType)
- title
- message
- relatedEntityId
- isRead
- createdAt
- readAt
```

### 2. **DeviceToken**
Almacena tokens FCM de dispositivos para notificaciones push.

```java
- id
- user (User)
- token
- deviceType (iOS, Android, Web)
- deviceName
- isActive
- createdAt
- lastUsed
```

### 3. **AlertReport**
Reportes de alertas falsas o inapropiadas.

```java
- id
- alert (Alert)
- reporter (User)
- reason (ReportReason)
- description
- reviewed
- reviewedBy (User)
- reviewedAt
- reviewNotes
- createdAt
```

### 4. **FavoriteRoute**
Rutas favoritas de los usuarios.

```java
- id
- user (User)
- route (Route)
- customName
- notificationsEnabled
- savedAt
- lastUsed
```

### 5. **FavoriteAlert**
Alertas guardadas por los usuarios.

```java
- id
- user (User)
- alert (Alert)
- savedAt
```

### 6. **UserStatistics**
Estadísticas y métricas de cada usuario.

```java
- id
- user (User)
- alertsCreated
- alertsVerified
- commentsPosted
- upvotesReceived
- downvotesReceived
- reportsSubmitted
- validReports
- reputationPoints
- level (1-10)
- lastAlertAt
- lastCommentAt
```

### 7. **UserBadge**
Logros y badges ganados por los usuarios.

```java
- id
- user (User)
- badgeType (BadgeType)
- earnedAt
```

---

## 🔧 SERVICIOS IMPLEMENTADOS

### 1. **ImageUploadService**
Maneja la subida de imágenes a Cloudinary.

**Métodos:**
- `uploadImage(MultipartFile)` - Subir imagen desde archivo
- `uploadBase64Image(String)` - Subir imagen base64
- `deleteImage(String publicId)` - Eliminar imagen
- `extractPublicIdFromUrl(String)` - Extraer ID de URL

### 2. **NotificationService**
Gestiona notificaciones internas.

**Métodos:**
- `createNotification()` - Crear notificación
- `getUserNotifications()` - Obtener notificaciones del usuario
- `getUnreadNotifications()` - Notificaciones no leídas
- `getUnreadCount()` - Contador de no leídas
- `markAsRead()` - Marcar como leída
- `markAllAsRead()` - Marcar todas como leídas
- `deleteNotification()` - Eliminar notificación

### 3. **PushNotificationService**
Envía notificaciones push vía Firebase.

**Métodos:**
- `registerDeviceToken()` - Registrar token de dispositivo
- `unregisterDeviceToken()` - Desregistrar token
- `sendNotificationToUser()` - Enviar a un usuario
- `sendNotificationToUsers()` - Enviar a múltiples usuarios
- `sendMulticastNotification()` - Enviar a múltiples tokens
- `sendNotificationToTopic()` - Enviar a topic

### 4. **WebSocketService**
Broadcast de eventos en tiempo real.

**Métodos:**
- `broadcastNewAlert()` - Nueva alerta
- `broadcastAlertUpdate()` - Actualización de alerta
- `broadcastAlertDeletion()` - Eliminación de alerta
- `broadcastAlertStatusChange()` - Cambio de estado
- `broadcastAlertVoteUpdate()` - Actualización de votos
- `broadcastNewComment()` - Nuevo comentario
- `sendNearbyAlertsToUser()` - Alertas cercanas personalizadas
- `sendPersonalNotification()` - Notificación personal

### 5. **FavoriteService**
Gestiona favoritos de rutas y alertas.

**Métodos Rutas:**
- `addFavoriteRoute()` - Agregar ruta a favoritos
- `removeFavoriteRoute()` - Quitar ruta de favoritos
- `getUserFavoriteRoutes()` - Obtener rutas favoritas
- `updateFavoriteRouteLastUsed()` - Actualizar último uso
- `isFavoriteRoute()` - Verificar si es favorita

**Métodos Alertas:**
- `addFavoriteAlert()` - Agregar alerta a favoritos
- `removeFavoriteAlert()` - Quitar alerta de favoritos
- `getUserFavoriteAlerts()` - Obtener alertas favoritas
- `isFavoriteAlert()` - Verificar si es favorita

### 6. **AlertReportService**
Sistema de reportes y moderación.

**Métodos:**
- `createReport()` - Crear reporte
- `getAllReports()` - Todos los reportes
- `getPendingReports()` - Reportes pendientes
- `getReportsByAlertId()` - Reportes de una alerta
- `reviewReport()` - Revisar reporte (aprobar/rechazar)
- `getReportCountByAlertId()` - Contador de reportes
- Auto-rechazo después de 5 reportes

### 7. **UserStatisticsService**
Estadísticas y reputación de usuarios.

**Métodos:**
- `getUserStatistics()` - Obtener estadísticas
- `getTopUsersByReputation()` - Top por reputación
- `getTopUsersByAlerts()` - Top por alertas
- `getTopUsersByUpvotes()` - Top por upvotes
- `incrementAlertCreated()` - +1 alerta
- `incrementCommentPosted()` - +1 comentario
- `incrementUpvoteReceived()` - +1 upvote
- `incrementDownvoteReceived()` - +1 downvote

**Sistema de Puntos:**
- Crear alerta: +10 puntos
- Recibir upvote: +5 puntos
- Comentar: +3 puntos
- Alerta verificada: +20 puntos
- Reporte válido: +15 puntos

**Niveles:**
- Nivel 1: 0-19 puntos
- Nivel 2: 20-49 puntos
- Nivel 3: 50-99 puntos
- Nivel 4: 100-249 puntos
- Nivel 5: 250-499 puntos
- Nivel 6: 500-999 puntos
- Nivel 7: 1000-2499 puntos
- Nivel 8: 2500-4999 puntos
- Nivel 9: 5000-9999 puntos
- Nivel 10: 10000+ puntos

### 8. **BadgeService**
Sistema de logros y badges.

**Badges Disponibles:**
- `FIRST_ALERT` - Primera alerta creada
- `ALERTS_10` - 10 alertas creadas
- `ALERTS_50` - 50 alertas creadas
- `ALERTS_100` - 100 alertas creadas
- `HELPFUL_REPORTER` - 5+ reportes válidos
- `TRUSTED_USER` - 500+ puntos de reputación
- `COMMUNITY_HERO` - 2000+ puntos de reputación
- `VERIFIED_ALERTS` - 10+ alertas verificadas
- `ACTIVE_COMMENTER` - 50+ comentarios
- `ROUTE_EXPERT` - 100+ upvotes recibidos

**Métodos:**
- `getUserBadges()` - Obtener badges del usuario
- `awardBadge()` - Otorgar badge
- `checkAndAwardBadges()` - Verificar y otorgar automáticamente
- `hasBadge()` - Verificar si tiene badge

### 9. **EmailService**
Envío de emails transaccionales.

**Métodos:**
- `sendSimpleEmail()` - Email de texto plano
- `sendHtmlEmail()` - Email HTML
- `sendWelcomeEmail()` - Email de bienvenida
- `sendPasswordResetEmail()` - Recuperación de contraseña
- `sendAlertNotificationEmail()` - Notificación de alerta
- `sendAlertResolvedEmail()` - Alerta resuelta
- `sendBadgeEarnedEmail()` - Badge ganado

### 10. **AlertExpirationScheduler**
Tareas programadas automáticas.

**Jobs:**
- **Cada hora**: Expirar alertas vencidas
- **Diario a las 2 AM**: Limpiar alertas antiguas (30+ días)
- **Cada 10 minutos**: Verificar alertas con muchos reportes

---

## 🌐 ENDPOINTS API

### **Imágenes** `/api/images`

```
POST   /upload              - Subir imagen (multipart)
POST   /upload/base64       - Subir imagen base64
DELETE /{publicId}          - Eliminar imagen
```

### **Notificaciones** `/api/notifications`

```
GET    /                    - Mis notificaciones
GET    /paginated          - Mis notificaciones paginadas
GET    /unread             - Notificaciones no leídas
GET    /unread/count       - Contador de no leídas
PATCH  /{id}/read          - Marcar como leída
PATCH  /read-all           - Marcar todas como leídas
DELETE /{id}               - Eliminar notificación
DELETE /read               - Eliminar leídas

POST   /device-token       - Registrar token push
DELETE /device-token/{token} - Desregistrar token
GET    /device-tokens      - Mis tokens registrados
```

### **Favoritos** `/api/favorites`

**Rutas:**
```
POST   /routes             - Agregar ruta a favoritos
GET    /routes             - Mis rutas favoritas
GET    /routes/paginated   - Paginadas
DELETE /routes/{routeId}   - Quitar de favoritos
PATCH  /routes/{routeId}/last-used - Actualizar último uso
GET    /routes/{routeId}/is-favorite - Verificar si es favorita
```

**Alertas:**
```
POST   /alerts/{alertId}   - Agregar alerta a favoritos
GET    /alerts             - Mis alertas favoritas
GET    /alerts/paginated   - Paginadas
DELETE /alerts/{alertId}   - Quitar de favoritos
GET    /alerts/{alertId}/is-favorite - Verificar si es favorita
```

### **Reportes** `/api/reports`

```
POST   /                   - Crear reporte
GET    /                   - Todos los reportes
GET    /paginated          - Paginados
GET    /pending            - Reportes pendientes
GET    /pending/paginated  - Paginados
GET    /alert/{alertId}    - Reportes de una alerta
GET    /alert/{alertId}/count - Contador de reportes
GET    /my-reports         - Mis reportes
PATCH  /{reportId}/review  - Revisar reporte (MODERADOR)
```

### **Estadísticas** `/api/statistics`

```
GET    /me                 - Mis estadísticas
GET    /user/{userId}      - Estadísticas de un usuario
GET    /leaderboard/reputation - Top por reputación
GET    /leaderboard/alerts - Top por alertas creadas
GET    /leaderboard/upvotes - Top por upvotes

GET    /badges/me          - Mis badges
GET    /badges/user/{userId} - Badges de un usuario
```

---

## 🔴 WEBSOCKET EN TIEMPO REAL

### Conexión WebSocket

**Endpoint:** `/ws`

**Usando SockJS + STOMP:**

```javascript
const socket = new SockJS('http://localhost:8080/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({}, function(frame) {
    console.log('Connected: ' + frame);

    // Suscribirse a alertas globales
    stompClient.subscribe('/topic/alerts', function(message) {
        const alert = JSON.parse(message.body);
        console.log('Nueva alerta:', alert);
    });

    // Suscribirse a comentarios de una alerta específica
    stompClient.subscribe('/topic/alerts/123/comments', function(message) {
        const comment = JSON.parse(message.body);
        console.log('Nuevo comentario:', comment);
    });

    // Notificaciones personales (requiere autenticación)
    stompClient.subscribe('/user/queue/notifications', function(message) {
        console.log('Notificación personal:', message.body);
    });
});
```

### Topics Disponibles

1. **`/topic/alerts`** - Todas las actualizaciones de alertas
   - Acción: `CREATED`, `UPDATED`, `DELETED`, `STATUS_CHANGED`, `VOTE_UPDATE`

2. **`/topic/alerts/{alertId}/comments`** - Comentarios de una alerta

3. **`/user/queue/nearby-alerts`** - Alertas cercanas personalizadas

4. **`/user/queue/notifications`** - Notificaciones personales

---

## 📊 SISTEMA DE ESTADÍSTICAS Y REPUTACIÓN

### Cálculo de Reputación

Los puntos de reputación se ganan/pierden automáticamente:

```
ACCIONES POSITIVAS:
+ Crear alerta: 10 puntos
+ Recibir upvote: 5 puntos
+ Comentar: 3 puntos
+ Alerta verificada por otros: 20 puntos
+ Reporte válido aprobado: 15 puntos

ACCIONES NEGATIVAS:
- Recibir downvote: -2 puntos
```

### Niveles de Usuario

Los niveles se calculan automáticamente basados en puntos:

| Nivel | Puntos Requeridos | Título |
|-------|------------------|--------|
| 1 | 0-19 | Novato |
| 2 | 20-49 | Principiante |
| 3 | 50-99 | Aprendiz |
| 4 | 100-249 | Colaborador |
| 5 | 250-499 | Contribuidor |
| 6 | 500-999 | Experto |
| 7 | 1000-2499 | Veterano |
| 8 | 2500-4999 | Maestro |
| 9 | 5000-9999 | Leyenda |
| 10 | 10000+ | Héroe |

### Leaderboards Disponibles

1. **Top por Reputación** - Usuarios con más puntos
2. **Top por Alertas** - Usuarios que más alertas han creado
3. **Top por Upvotes** - Usuarios con más upvotes recibidos

---

## 🎖️ SISTEMA DE BADGES

Los badges se otorgan automáticamente al cumplir condiciones:

| Badge | Condición | Descripción |
|-------|-----------|-------------|
| Primera Alerta | 1 alerta creada | Tu primera contribución |
| Reportero Activo | 10 alertas creadas | Contribuidor regular |
| Reportero Experto | 50 alertas creadas | Experto en reportes |
| Maestro Reportero | 100 alertas creadas | Maestro de alertas |
| Reportes Útiles | 5 reportes válidos | Tus reportes ayudan |
| Usuario Confiable | 500 puntos | Usuario de confianza |
| Héroe de la Comunidad | 2000 puntos | Leyenda de la comunidad |
| Alertas Verificadas | 10 alertas verificadas | Información confiable |
| Comentarista Activo | 50 comentarios | Participación activa |
| Experto en Rutas | 100 upvotes | Conocedor de rutas |

---

## 📱 NOTIFICACIONES PUSH (FCM)

### Configuración Firebase

1. Descargar `firebase-service-account.json` de Firebase Console
2. Colocar en `src/main/resources/`
3. Configurar en `application.properties`:

```properties
firebase.config.file=firebase-service-account.json
```

### Registrar Token de Dispositivo

```bash
POST /api/notifications/device-token
Authorization: Bearer {token}

{
  "token": "fcm_token_del_dispositivo",
  "deviceType": "Android",
  "deviceName": "Samsung Galaxy S21"
}
```

### Tipos de Notificaciones

El sistema envía notificaciones automáticamente para:

- Nueva alerta cerca de ruta favorita
- Alerta resuelta
- Comentario en tu alerta
- Upvote recibido
- Badge ganado
- Alerta en revisión
- Alerta aprobada/rechazada

---

## 🛡️ RATE LIMITING

### Límites Configurados

**General:**
- 20 requests por minuto por usuario/IP

**Creación de Alertas:**
- 5 alertas por hora por usuario

**Creación de Comentarios:**
- 10 comentarios por hora por usuario

### Headers de Respuesta

```
X-Rate-Limit-Remaining: 15
X-Rate-Limit-Retry-After-Seconds: 45
```

### Respuesta cuando se excede:

```
HTTP 429 Too Many Requests
{
  "error": "Has excedido el límite de solicitudes"
}
```

---

## 💾 CACHÉ CON REDIS

### Configuración Redis

```properties
spring.data.redis.host=localhost
spring.data.redis.port=6379
spring.data.redis.password=
spring.cache.type=redis
```

### Cachés Configurados

| Caché | TTL | Uso |
|-------|-----|-----|
| alerts | 5 min | Alertas frecuentemente consultadas |
| routes | 15 min | Rutas populares |
| statistics | 30 min | Estadísticas de usuarios |

---

## ⚙️ CONFIGURACIÓN COMPLETA

### Variables de Entorno Requeridas

```bash
# Base de datos
DATABASE_URL=jdbc:postgresql://localhost:5432/roadwarnings
DB_USER=postgres
DB_PASSWORD=password

# JWT
JWT_SECRET=your-super-secret-jwt-key-minimum-32-characters
JWT_EXPIRATION=86400000

# Cloudinary
CLOUDINARY_CLOUD_NAME=your-cloud-name
CLOUDINARY_API_KEY=your-api-key
CLOUDINARY_API_SECRET=your-api-secret

# Email
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=

# Firebase
FIREBASE_CONFIG_FILE=firebase-service-account.json
```

### Archivo application.properties

Ver: `src/main/resources/application.properties.example`

---

## 🚀 PRÓXIMOS PASOS SUGERIDOS

1. **Frontend:** Integrar todas las nuevas funcionalidades
2. **Testing:** Crear tests de integración completos
3. **Documentación:** Completar Swagger/OpenAPI docs
4. **Monitoring:** Configurar Prometheus + Grafana
5. **CI/CD:** Pipeline de despliegue automático
6. **Performance:** Optimizar queries y añadir índices
7. **Security:** Auditoría de seguridad completa

---

## 📞 SOPORTE

Para dudas o problemas con las nuevas funcionalidades:
- Revisar logs de la aplicación
- Verificar configuración de variables de entorno
- Comprobar conectividad con Redis y Firebase
- Revisar documentación de cada servicio

---

**Desarrollado por:** Claude Code
**Fecha:** Noviembre 2024
**Versión Backend:** 2.0.0
**Estado:** ✅ TODAS LAS FUNCIONALIDADES IMPLEMENTADAS
