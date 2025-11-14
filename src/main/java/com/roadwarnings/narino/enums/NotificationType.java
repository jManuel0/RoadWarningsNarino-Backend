package com.roadwarnings.narino.enums;

public enum NotificationType {
    ALERT_CREATED,           // Nueva alerta cerca de una ruta guardada
    ALERT_UPDATED,           // Actualización de alerta relevante
    ALERT_RESOLVED,          // Alerta resuelta
    COMMENT_RECEIVED,        // Alguien comentó en tu alerta
    UPVOTE_RECEIVED,         // Alguien votó tu alerta
    ALERT_NEARBY,            // Alerta nueva cerca de tu ubicación
    ROUTE_ALERT,             // Alerta en tu ruta favorita
    BADGE_EARNED,            // Nuevo logro ganado
    REPUTATION_MILESTONE,    // Hito de reputación alcanzado
    ALERT_UNDER_REVIEW,      // Tu alerta está siendo revisada
    ALERT_REJECTED,          // Tu alerta fue rechazada
    ALERT_APPROVED,          // Tu alerta fue aprobada
    SYSTEM_ANNOUNCEMENT      // Anuncio del sistema
}
