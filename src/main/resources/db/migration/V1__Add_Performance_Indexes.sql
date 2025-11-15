-- ====================================================================
-- Índices de rendimiento para RoadWarnings Nariño
-- ====================================================================
-- Este archivo crea índices estratégicos para optimizar las consultas
-- más frecuentes del sistema
-- ====================================================================

-- ====================================================================
-- TABLA: alerts
-- ====================================================================

-- Índice para búsqueda por estado (muy usado para obtener alertas activas)
CREATE INDEX IF NOT EXISTS idx_alerts_status ON alerts(status);

-- Índice para búsqueda por tipo de alerta
CREATE INDEX IF NOT EXISTS idx_alerts_type ON alerts(type);

-- Índice para búsqueda por severidad
CREATE INDEX IF NOT EXISTS idx_alerts_severity ON alerts(severity);

-- Índice compuesto para búsqueda de alertas activas (consulta muy frecuente)
CREATE INDEX IF NOT EXISTS idx_alerts_status_created ON alerts(status, created_at DESC);

-- Índice para búsqueda por usuario (para obtener alertas de un usuario)
CREATE INDEX IF NOT EXISTS idx_alerts_user_id ON alerts(user_id);

-- Índice espacial para búsquedas geográficas (alertas cercanas)
CREATE INDEX IF NOT EXISTS idx_alerts_location ON alerts(latitude, longitude);

-- Índice para búsqueda por fecha de creación (ordenamiento)
CREATE INDEX IF NOT EXISTS idx_alerts_created_at ON alerts(created_at DESC);

-- Índice para búsqueda por fecha de expiración
CREATE INDEX IF NOT EXISTS idx_alerts_expires_at ON alerts(expires_at);

-- Índice compuesto para alertas activas por tipo (consulta común)
CREATE INDEX IF NOT EXISTS idx_alerts_status_type ON alerts(status, type);

-- ====================================================================
-- TABLA: comments
-- ====================================================================

-- Índice para obtener comentarios por alerta (consulta muy frecuente)
CREATE INDEX IF NOT EXISTS idx_comments_alert_id ON comments(alert_id);

-- Índice para obtener comentarios por usuario
CREATE INDEX IF NOT EXISTS idx_comments_user_id ON comments(user_id);

-- Índice para ordenamiento por fecha de creación
CREATE INDEX IF NOT EXISTS idx_comments_created_at ON comments(created_at DESC);

-- Índice compuesto para comentarios de una alerta ordenados por fecha
CREATE INDEX IF NOT EXISTS idx_comments_alert_created ON comments(alert_id, created_at DESC);

-- ====================================================================
-- TABLA: users
-- ====================================================================

-- Índice único para username (búsqueda de autenticación)
CREATE UNIQUE INDEX IF NOT EXISTS idx_users_username ON users(username);

-- Índice único para email (validación y recuperación de contraseña)
CREATE UNIQUE INDEX IF NOT EXISTS idx_users_email ON users(email);

-- Índice para búsqueda por rol
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);

-- Índice para usuarios activos
CREATE INDEX IF NOT EXISTS idx_users_is_active ON users(is_active);

-- ====================================================================
-- TABLA: user_statistics
-- ====================================================================

-- Índice único para búsqueda por user_id (relación 1:1)
CREATE UNIQUE INDEX IF NOT EXISTS idx_user_statistics_user_id ON user_statistics(user_id);

-- Índice para ordenamiento por puntos de reputación (leaderboard)
CREATE INDEX IF NOT EXISTS idx_user_statistics_reputation ON user_statistics(reputation_points DESC);

-- Índice para ordenamiento por nivel
CREATE INDEX IF NOT EXISTS idx_user_statistics_level ON user_statistics(level DESC);

-- Índice compuesto para leaderboard (nivel + reputación)
CREATE INDEX IF NOT EXISTS idx_user_statistics_level_reputation ON user_statistics(level DESC, reputation_points DESC);

-- ====================================================================
-- TABLA: user_badges
-- ====================================================================

-- Índice para obtener badges por usuario
CREATE INDEX IF NOT EXISTS idx_user_badges_user_id ON user_badges(user_id);

-- Índice para búsqueda por tipo de badge
CREATE INDEX IF NOT EXISTS idx_user_badges_type ON user_badges(badge_type);

-- Índice compuesto para verificar si un usuario tiene un badge específico
CREATE UNIQUE INDEX IF NOT EXISTS idx_user_badges_user_type ON user_badges(user_id, badge_type);

-- Índice para ordenamiento por fecha de obtención
CREATE INDEX IF NOT EXISTS idx_user_badges_earned_at ON user_badges(earned_at DESC);

-- ====================================================================
-- TABLA: notifications
-- ====================================================================

-- Índice para obtener notificaciones por usuario
CREATE INDEX IF NOT EXISTS idx_notifications_user_id ON notifications(user_id);

-- Índice para filtrar notificaciones no leídas
CREATE INDEX IF NOT EXISTS idx_notifications_is_read ON notifications(is_read);

-- Índice compuesto para notificaciones no leídas de un usuario
CREATE INDEX IF NOT EXISTS idx_notifications_user_unread ON notifications(user_id, is_read, created_at DESC);

-- Índice para ordenamiento por fecha
CREATE INDEX IF NOT EXISTS idx_notifications_created_at ON notifications(created_at DESC);

-- Índice por tipo de notificación
CREATE INDEX IF NOT EXISTS idx_notifications_type ON notifications(type);

-- ====================================================================
-- TABLA: alert_reports
-- ====================================================================

-- Índice para obtener reportes por alerta
CREATE INDEX IF NOT EXISTS idx_alert_reports_alert_id ON alert_reports(alert_id);

-- Índice para obtener reportes por usuario (moderador)
CREATE INDEX IF NOT EXISTS idx_alert_reports_user_id ON alert_reports(user_id);

-- Índice para filtrar por estado de revisión
CREATE INDEX IF NOT EXISTS idx_alert_reports_review_status ON alert_reports(review_status);

-- Índice compuesto para reportes pendientes
CREATE INDEX IF NOT EXISTS idx_alert_reports_pending ON alert_reports(review_status, created_at DESC);

-- ====================================================================
-- TABLA: routes
-- ====================================================================

-- Índice para rutas activas
CREATE INDEX IF NOT EXISTS idx_routes_is_active ON routes(is_active);

-- Índice espacial para origen de ruta
CREATE INDEX IF NOT EXISTS idx_routes_origin ON routes(origin_latitude, origin_longitude);

-- Índice espacial para destino de ruta
CREATE INDEX IF NOT EXISTS idx_routes_destination ON routes(destination_latitude, destination_longitude);

-- Índice para ordenamiento por distancia
CREATE INDEX IF NOT EXISTS idx_routes_distance ON routes(distance_km);

-- ====================================================================
-- TABLA: favorite_routes
-- ====================================================================

-- Índice para obtener rutas favoritas por usuario
CREATE INDEX IF NOT EXISTS idx_favorite_routes_user_id ON favorite_routes(user_id);

-- Índice para obtener usuarios que tienen una ruta como favorita
CREATE INDEX IF NOT EXISTS idx_favorite_routes_route_id ON favorite_routes(route_id);

-- Índice para rutas con notificaciones habilitadas
CREATE INDEX IF NOT EXISTS idx_favorite_routes_notifications ON favorite_routes(notifications_enabled);

-- Índice compuesto para rutas favoritas activas de un usuario
CREATE INDEX IF NOT EXISTS idx_favorite_routes_user_notifications ON favorite_routes(user_id, notifications_enabled);

-- ====================================================================
-- ESTADÍSTICAS DE TABLAS
-- ====================================================================

-- Actualizar estadísticas para el optimizador de consultas
ANALYZE alerts;
ANALYZE comments;
ANALYZE users;
ANALYZE user_statistics;
ANALYZE user_badges;
ANALYZE notifications;
ANALYZE alert_reports;
ANALYZE routes;
ANALYZE favorite_routes;
