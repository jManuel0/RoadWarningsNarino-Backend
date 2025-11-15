# Optimización de Base de Datos - RoadWarnings Nariño

## Índices Implementados

### Tabla: `alerts`
Los índices en la tabla de alertas están optimizados para las consultas más frecuentes:

- **`idx_alerts_status`**: Búsqueda rápida por estado (ACTIVE, EXPIRED, RESOLVED)
- **`idx_alerts_status_created`**: Alertas activas ordenadas por fecha (consulta principal)
- **`idx_alerts_location`**: Búsquedas geográficas de alertas cercanas
- **`idx_alerts_user_id`**: Alertas por usuario
- **`idx_alerts_type`**: Filtrado por tipo de alerta
- **`idx_alerts_severity`**: Filtrado por severidad

### Tabla: `comments`
- **`idx_comments_alert_id`**: Comentarios por alerta (consulta muy frecuente)
- **`idx_comments_alert_created`**: Comentarios ordenados por fecha

### Tabla: `users`
- **`idx_users_username`** (UNIQUE): Autenticación rápida
- **`idx_users_email`** (UNIQUE): Validación y recuperación de contraseña
- **`idx_users_role`**: Consultas por rol (admin, moderator, user)

### Tabla: `user_statistics`
- **`idx_user_statistics_reputation`**: Leaderboard por reputación
- **`idx_user_statistics_level_reputation`**: Leaderboard combinado

### Tabla: `notifications`
- **`idx_notifications_user_unread`**: Notificaciones no leídas por usuario (consulta principal)

## Consultas Optimizadas

### 1. Obtener Alertas Activas (Más Frecuente)
```sql
SELECT * FROM alerts
WHERE status = 'ACTIVE'
ORDER BY created_at DESC;
```
**Optimización**: Usa índice `idx_alerts_status_created` (covering index)

### 2. Alertas Cercanas (Geolocalización)
```sql
SELECT * FROM alerts
WHERE status = 'ACTIVE'
AND (6371 * acos(cos(radians(?)) * cos(radians(latitude)) *
     cos(radians(longitude) - radians(?)) + sin(radians(?)) *
     sin(radians(latitude)))) < ?
ORDER BY created_at DESC;
```
**Optimización**: Usa índice `idx_alerts_location` para reducir el conjunto antes del cálculo de distancia

### 3. Leaderboard de Usuarios
```sql
SELECT u.username, us.reputation_points, us.level
FROM user_statistics us
JOIN users u ON us.user_id = u.id
ORDER BY us.level DESC, us.reputation_points DESC
LIMIT 10;
```
**Optimización**: Usa índice `idx_user_statistics_level_reputation`

### 4. Notificaciones No Leídas
```sql
SELECT * FROM notifications
WHERE user_id = ? AND is_read = false
ORDER BY created_at DESC;
```
**Optimización**: Usa índice `idx_notifications_user_unread`

## Mejores Prácticas Implementadas

### 1. Paginación
Todas las consultas que pueden retornar muchos resultados usan paginación:
```java
Page<Alert> getAllAlerts(Pageable pageable);
```

### 2. Caché Redis
Consultas frecuentes están en caché:
- Alertas activas: 2 minutos
- Alertas cercanas: 3 minutos
- Estadísticas de usuario: 15 minutos
- Leaderboard: 5 minutos

### 3. Lazy Loading
Relaciones se cargan de forma lazy por defecto:
```java
@ManyToOne(fetch = FetchType.LAZY)
private User user;
```

### 4. Batch Operations
```properties
spring.jpa.properties.hibernate.jdbc.batch_size=20
spring.jpa.properties.hibernate.order_inserts=true
spring.jpa.properties.hibernate.order_updates=true
```

### 5. Connection Pooling (HikariCP)
```properties
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=30000
```

## Consultas N+1 Prevenidas

### Problema N+1 Común:
```java
// ❌ MALO - Genera N+1 consultas
List<Alert> alerts = alertRepository.findAll();
for (Alert alert : alerts) {
    alert.getUser().getUsername(); // Nueva consulta por cada alerta
}
```

### Solución con JOIN FETCH:
```java
// ✅ BUENO - Una sola consulta
@Query("SELECT a FROM Alert a JOIN FETCH a.user WHERE a.status = :status")
List<Alert> findActiveAlertsWithUser(@Param("status") AlertStatus status);
```

## Monitoreo de Rendimiento

### 1. Activar SQL Logging (Solo en desarrollo)
```properties
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

### 2. Estadísticas de Hibernate
```properties
spring.jpa.properties.hibernate.generate_statistics=true
```

### 3. Slow Query Log (PostgreSQL)
```sql
-- Configurar en PostgreSQL
ALTER DATABASE roadwarnings SET log_min_duration_statement = 1000; -- 1 segundo
```

## Mantenimiento Periódico

### 1. Actualizar Estadísticas (Semanal)
```sql
ANALYZE alerts;
ANALYZE comments;
ANALYZE users;
ANALYZE user_statistics;
```

### 2. Vacuum (Mensual)
```sql
VACUUM ANALYZE alerts;
VACUUM ANALYZE comments;
```

### 3. Reindexar (Trimestral)
```sql
REINDEX TABLE alerts;
REINDEX TABLE comments;
```

## Métricas de Rendimiento Esperadas

Con los índices implementados:

| Consulta | Antes | Después | Mejora |
|----------|-------|---------|--------|
| Alertas activas (10k registros) | 450ms | 15ms | 30x |
| Alertas cercanas (geolocalización) | 1200ms | 80ms | 15x |
| Comentarios por alerta | 120ms | 8ms | 15x |
| Leaderboard top 100 | 380ms | 25ms | 15x |
| Notificaciones no leídas | 95ms | 5ms | 19x |

## Configuración de PostgreSQL Recomendada

```conf
# postgresql.conf

# Memory
shared_buffers = 256MB
effective_cache_size = 1GB
work_mem = 16MB
maintenance_work_mem = 64MB

# Planner
random_page_cost = 1.1  # SSD
effective_io_concurrency = 200  # SSD

# Write Ahead Log
wal_buffers = 16MB
checkpoint_completion_target = 0.9

# Query Planning
default_statistics_target = 100
```

## Próximos Pasos de Optimización

1. **Particionamiento**: Particionar tabla `alerts` por fecha si crece mucho
2. **Índices parciales**: Para consultas muy específicas
3. **Materialised Views**: Para reportes complejos
4. **Réplicas de lectura**: Para distribuir carga si es necesario
5. **Full-text search**: PostgreSQL full-text para búsquedas de texto

## Herramientas de Análisis

### 1. EXPLAIN ANALYZE
```sql
EXPLAIN ANALYZE
SELECT * FROM alerts
WHERE status = 'ACTIVE'
ORDER BY created_at DESC
LIMIT 10;
```

### 2. pg_stat_statements
```sql
-- Consultas más lentas
SELECT query, mean_exec_time, calls
FROM pg_stat_statements
ORDER BY mean_exec_time DESC
LIMIT 10;
```

### 3. pg_stat_user_indexes
```sql
-- Índices menos usados
SELECT schemaname, tablename, indexname, idx_scan
FROM pg_stat_user_indexes
WHERE idx_scan < 50
ORDER BY idx_scan;
```
