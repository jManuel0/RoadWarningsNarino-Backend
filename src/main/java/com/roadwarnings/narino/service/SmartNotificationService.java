package com.roadwarnings.narino.service;

import com.roadwarnings.narino.entity.Alert;
import com.roadwarnings.narino.entity.FavoriteRoute;
import com.roadwarnings.narino.entity.Route;
import com.roadwarnings.narino.enums.NotificationType;
import com.roadwarnings.narino.repository.FavoriteRouteRepository;
import com.roadwarnings.narino.repository.RouteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio para notificaciones inteligentes basadas en rutas favoritas
 * Notifica a usuarios cuando hay alertas nuevas en sus rutas frecuentes
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class SmartNotificationService {

    private final FavoriteRouteRepository favoriteRouteRepository;
    private final RouteRepository routeRepository;
    private final NotificationService notificationService;

    // Radio por defecto en km para considerar que una alerta afecta una ruta
    private static final double DEFAULT_ALERT_RADIUS_KM = 2.0;

    /**
     * Se ejecuta cuando se crea una nueva alerta
     * Notifica a usuarios que tienen rutas favoritas cerca de la alerta
     */
    public void onNewAlert(Alert alert) {
        log.info("Procesando notificaciones inteligentes para alerta: {}", alert.getId());

        if (alert.getLatitude() == null || alert.getLongitude() == null) {
            log.warn("Alerta {} no tiene coordenadas, saltando notificaciones", alert.getId());
            return;
        }

        // Obtener todas las rutas que están cerca de la alerta
        List<Route> nearbyRoutes = findRoutesNearLocation(
                alert.getLatitude(),
                alert.getLongitude(),
                DEFAULT_ALERT_RADIUS_KM
        );

        log.info("Encontradas {} rutas cercanas a la alerta", nearbyRoutes.size());

        // Para cada ruta cercana, notificar a los usuarios que la tienen como favorita
        for (Route route : nearbyRoutes) {
            notifyUsersWithFavoriteRoute(route, alert);
        }
    }

    /**
     * Notifica a todos los usuarios que tienen una ruta como favorita
     */
    private void notifyUsersWithFavoriteRoute(Route route, Alert alert) {
        List<FavoriteRoute> favoriteRoutes = favoriteRouteRepository
                .findByRouteId(route.getId());

        log.info("Notificando a {} usuarios con ruta favorita: {}",
                favoriteRoutes.size(), route.getName());

        for (FavoriteRoute favoriteRoute : favoriteRoutes) {
            // Solo notificar si el usuario tiene las notificaciones activadas
            if (Boolean.TRUE.equals(favoriteRoute.getNotificationsEnabled())) {
                // No notificar al usuario que creó la alerta
                if (!alert.getUser().getId().equals(favoriteRoute.getUser().getId())) {
                    sendRouteAlertNotification(favoriteRoute, alert);
                }
            }
        }
    }

    /**
     * Envía notificación de alerta en ruta favorita
     */
    private void sendRouteAlertNotification(FavoriteRoute favoriteRoute, Alert alert) {
        String routeName = favoriteRoute.getCustomName() != null ?
                favoriteRoute.getCustomName() : favoriteRoute.getRoute().getName();

        String title = "Alerta en tu ruta: " + routeName;
        String message = String.format("%s - %s",
                alert.getType().toString(),
                alert.getLocation() != null ? alert.getLocation() : "ubicación cercana");

        notificationService.createNotification(
                favoriteRoute.getUser().getId(),
                NotificationType.ROUTE_ALERT,
                title,
                message,
                alert.getId()
        );

        log.debug("Notificación enviada a usuario {} para ruta {}",
                favoriteRoute.getUser().getId(), routeName);
    }

    /**
     * Encuentra rutas que pasan cerca de una ubicación
     */
    private List<Route> findRoutesNearLocation(Double latitude, Double longitude, Double radiusKm) {
        List<Route> allRoutes = routeRepository.findByIsActive(true);

        return allRoutes.stream()
                .filter(route -> isRouteNearLocation(route, latitude, longitude, radiusKm))
                .toList();
    }

    /**
     * Verifica si una ruta pasa cerca de una ubicación
     * Considera tanto el origen, destino y la ruta intermedia (simplificado)
     */
    private boolean isRouteNearLocation(Route route, Double latitude, Double longitude, Double radiusKm) {
        // Verificar si el punto está cerca del origen
        double distanceToOrigin = calculateDistance(
                latitude, longitude,
                route.getOriginLatitude(), route.getOriginLongitude()
        );

        if (distanceToOrigin <= radiusKm) {
            return true;
        }

        // Verificar si el punto está cerca del destino
        double distanceToDestination = calculateDistance(
                latitude, longitude,
                route.getDestinationLatitude(), route.getDestinationLongitude()
        );

        if (distanceToDestination <= radiusKm) {
            return true;
        }

        // Verificar si el punto está cerca de la línea entre origen y destino (simplificado)
        // TODO: Para mayor precisión, usar el polyline de la ruta
        double distanceToLine = distanceToLineSegment(
                latitude, longitude,
                route.getOriginLatitude(), route.getOriginLongitude(),
                route.getDestinationLatitude(), route.getDestinationLongitude()
        );

        return distanceToLine <= radiusKm;
    }

    /**
     * Calcula distancia entre dos puntos usando fórmula de Haversine
     */
    private double calculateDistance(Double lat1, Double lon1, Double lat2, Double lon2) {
        if (lat1 == null || lon1 == null || lat2 == null || lon2 == null) {
            return Double.MAX_VALUE;
        }

        final int R = 6371; // Radio de la Tierra en km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    /**
     * Calcula la distancia perpendicular de un punto a un segmento de línea
     */
    private double distanceToLineSegment(Double px, Double py,
                                          Double x1, Double y1,
                                          Double x2, Double y2) {
        double A = px - x1;
        double B = py - y1;
        double C = x2 - x1;
        double D = y2 - y1;

        double dot = A * C + B * D;
        double lenSq = C * C + D * D;
        double param = (lenSq != 0) ? (dot / lenSq) : -1;

        double xx, yy;

        if (param < 0) {
            xx = x1;
            yy = y1;
        } else if (param > 1) {
            xx = x2;
            yy = y2;
        } else {
            xx = x1 + param * C;
            yy = y1 + param * D;
        }

        return calculateDistance(px, py, xx, yy);
    }

    /**
     * Obtiene estadísticas de notificaciones de rutas para un usuario
     */
    public RouteNotificationStatsDTO getNotificationStats(Long userId) {
        List<FavoriteRoute> favoriteRoutes = favoriteRouteRepository.findByUserId(userId);

        long totalRoutes = favoriteRoutes.size();
        long routesWithNotifications = favoriteRoutes.stream()
                .filter(fr -> Boolean.TRUE.equals(fr.getNotificationsEnabled()))
                .count();

        return RouteNotificationStatsDTO.builder()
                .totalFavoriteRoutes(totalRoutes)
                .routesWithNotificationsEnabled(routesWithNotifications)
                .build();
    }

    // ==================== DTOs ====================

    @lombok.Data
    @lombok.Builder
    public static class RouteNotificationStatsDTO {
        private Long totalFavoriteRoutes;
        private Long routesWithNotificationsEnabled;
    }
}
