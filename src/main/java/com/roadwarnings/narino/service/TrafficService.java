package com.roadwarnings.narino.service;

import com.roadwarnings.narino.entity.Alert;
import com.roadwarnings.narino.entity.Route;
import com.roadwarnings.narino.enums.AlertSeverity;
import com.roadwarnings.narino.repository.AlertRepository;
import com.roadwarnings.narino.repository.RouteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio para estimación de condiciones de tráfico
 * Calcula niveles de tráfico basándose en alertas activas, hora del día y datos históricos
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TrafficService {

    private final AlertRepository alertRepository;
    private final RouteRepository routeRepository;

    private static final double TRAFFIC_RADIUS_KM = 5.0; // Radio para considerar alertas relevantes

    /**
     * Obtiene las condiciones de tráfico para una ubicación
     */
    @Cacheable(value = "traffic", key = "#latitude + '_' + #longitude", unless = "#result == null")
    public TrafficConditionDTO getTrafficConditions(Double latitude, Double longitude) {
        if (latitude == null || longitude == null) {
            return null;
        }

        // Obtener alertas activas cercanas
        List<Alert> nearbyAlerts = findAlertsNearLocation(latitude, longitude, TRAFFIC_RADIUS_KM);

        // Calcular nivel de tráfico base según hora del día
        String baseTrafficLevel = calculateBaseTrafficLevel();

        // Ajustar nivel basándose en alertas
        TrafficImpact impact = calculateTrafficImpact(nearbyAlerts);

        // Nivel final de tráfico
        String finalTrafficLevel = adjustTrafficLevel(baseTrafficLevel, impact);

        return TrafficConditionDTO.builder()
                .trafficLevel(finalTrafficLevel)
                .baseTrafficLevel(baseTrafficLevel)
                .activeIncidents(nearbyAlerts.size())
                .estimatedDelay(impact.getEstimatedDelayMinutes())
                .incidents(mapToIncidentDTOs(nearbyAlerts))
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Obtiene condiciones de tráfico para una ruta específica
     */
    public RouteTrafficDTO getRouteTraffic(Long routeId) {
        Route route = routeRepository.findById(routeId).orElse(null);

        if (route == null) {
            return null;
        }

        // Verificar tráfico en origen
        TrafficConditionDTO originTraffic = getTrafficConditions(
                route.getOriginLatitude(),
                route.getOriginLongitude()
        );

        // Verificar tráfico en destino
        TrafficConditionDTO destinationTraffic = getTrafficConditions(
                route.getDestinationLatitude(),
                route.getDestinationLongitude()
        );

        // Calcular tráfico promedio de la ruta
        String overallTraffic = calculateOverallTraffic(originTraffic, destinationTraffic);

        int totalDelay = (originTraffic != null ? originTraffic.getEstimatedDelay() : 0) +
                        (destinationTraffic != null ? destinationTraffic.getEstimatedDelay() : 0);

        int adjustedTime = route.getEstimatedTimeMinutes() + totalDelay;

        return RouteTrafficDTO.builder()
                .routeId(routeId)
                .routeName(route.getName())
                .normalTimeMinutes(route.getEstimatedTimeMinutes())
                .currentTimeMinutes(adjustedTime)
                .delayMinutes(totalDelay)
                .trafficLevel(overallTraffic)
                .originTraffic(originTraffic)
                .destinationTraffic(destinationTraffic)
                .build();
    }

    /**
     * Calcula nivel de tráfico base según la hora del día
     */
    private String calculateBaseTrafficLevel() {
        LocalTime now = LocalTime.now();
        DayOfWeek day = LocalDateTime.now().getDayOfWeek();

        // Fin de semana - tráfico generalmente ligero
        if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
            if (now.isAfter(LocalTime.of(10, 0)) && now.isBefore(LocalTime.of(14, 0))) {
                return "MODERATE"; // Hora pico de compras
            }
            return "LIGHT";
        }

        // Entre semana
        // Hora pico mañana (6:00 - 9:00)
        if (now.isAfter(LocalTime.of(6, 0)) && now.isBefore(LocalTime.of(9, 0))) {
            return "HEAVY";
        }

        // Hora pico tarde (17:00 - 20:00)
        if (now.isAfter(LocalTime.of(17, 0)) && now.isBefore(LocalTime.of(20, 0))) {
            return "HEAVY";
        }

        // Hora almuerzo (12:00 - 14:00)
        if (now.isAfter(LocalTime.of(12, 0)) && now.isBefore(LocalTime.of(14, 0))) {
            return "MODERATE";
        }

        // Resto del día
        return "LIGHT";
    }

    /**
     * Calcula el impacto de las alertas en el tráfico
     */
    private TrafficImpact calculateTrafficImpact(List<Alert> alerts) {
        int totalDelay = 0;
        int severeIncidents = 0;

        for (Alert alert : alerts) {
            int delay = calculateAlertDelay(alert);
            totalDelay += delay;

            if (alert.getSeverity() == AlertSeverity.CRITICAL ||
                alert.getSeverity() == AlertSeverity.HIGH) {
                severeIncidents++;
            }
        }

        return new TrafficImpact(totalDelay, severeIncidents);
    }

    /**
     * Calcula el retraso estimado causado por una alerta
     */
    private int calculateAlertDelay(Alert alert) {
        // Tiempo de retraso base según tipo de alerta
        int baseDelay = switch (alert.getType()) {
            case ACCIDENTE -> 15;
            case OBRAS -> 10;
            case CONGESTION -> 8;
            case MANIFESTATION, EVENTO -> 12;
            case CIERRE_VIA -> 20;
            default -> 5;
        };

        // Ajustar según severidad
        double severityMultiplier = switch (alert.getSeverity()) {
            case CRITICAL -> 2.0;
            case HIGH -> 1.5;
            case MEDIUM -> 1.0;
            case LOW -> 0.5;
        };

        return (int) (baseDelay * severityMultiplier);
    }

    /**
     * Ajusta el nivel de tráfico basándose en incidentes
     */
    private String adjustTrafficLevel(String baseLevel, TrafficImpact impact) {
        if (impact.getSevereIncidents() >= 3 || impact.getEstimatedDelayMinutes() > 30) {
            return "SEVERE";
        }

        if (impact.getSevereIncidents() >= 1 || impact.getEstimatedDelayMinutes() > 15) {
            if ("LIGHT".equals(baseLevel)) return "MODERATE";
            if ("MODERATE".equals(baseLevel)) return "HEAVY";
            return "HEAVY";
        }

        if (impact.getEstimatedDelayMinutes() > 5 && "LIGHT".equals(baseLevel)) return "MODERATE";
        

        return baseLevel;
    }

    /**
     * Calcula el nivel de tráfico general de una ruta
     */
    private String calculateOverallTraffic(TrafficConditionDTO origin, TrafficConditionDTO destination) {
        if (origin == null && destination == null) {
            return "UNKNOWN";
        }

        String level1 = origin != null ? origin.getTrafficLevel() : "LIGHT";
        String level2 = destination != null ? destination.getTrafficLevel() : "LIGHT";

        // Tomar el peor de los dos
        if ("SEVERE".equals(level1) || "SEVERE".equals(level2)) return "SEVERE";
        if ("HEAVY".equals(level1) || "HEAVY".equals(level2)) return "HEAVY";
        if ("MODERATE".equals(level1) || "MODERATE".equals(level2)) return "MODERATE";
        return "LIGHT";
    }

    /**
     * Encuentra alertas cerca de una ubicación
     */
    private List<Alert> findAlertsNearLocation(Double latitude, Double longitude, Double radiusKm) {
        List<Alert> allAlerts = alertRepository.findAll();
        List<Alert> nearbyAlerts = new ArrayList<>();

        for (Alert alert : allAlerts) {
            if (alert.getLatitude() != null && alert.getLongitude() != null) {
                double distance = calculateDistance(
                        latitude, longitude,
                        alert.getLatitude(), alert.getLongitude()
                );

                if (distance <= radiusKm) {
                    nearbyAlerts.add(alert);
                }
            }
        }

        return nearbyAlerts;
    }

    /**
     * Calcula distancia usando fórmula de Haversine
     */
    private double calculateDistance(Double lat1, Double lon1, Double lat2, Double lon2) {
        final int R = 6371; // Radio de la Tierra en km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    private List<TrafficIncidentDTO> mapToIncidentDTOs(List<Alert> alerts) {
        return alerts.stream()
                .map(alert -> TrafficIncidentDTO.builder()
                        .alertId(alert.getId())
                        .type(alert.getType().toString())
                        .severity(alert.getSeverity().toString())
                        .location(alert.getLocation())
                        .description(alert.getDescription())
                        .estimatedDelay(calculateAlertDelay(alert))
                        .build())
                .toList();
    }

    // ==================== Helper Classes ====================

    @lombok.AllArgsConstructor
    @lombok.Getter
    private static class TrafficImpact {
        private int estimatedDelayMinutes;
        private int severeIncidents;
    }

    // ==================== DTOs ====================

    @lombok.Data
    @lombok.Builder
    public static class TrafficConditionDTO {
        private String trafficLevel; // LIGHT, MODERATE, HEAVY, SEVERE
        private String baseTrafficLevel;
        private Integer activeIncidents;
        private Integer estimatedDelay;
        private List<TrafficIncidentDTO> incidents;
        private LocalDateTime timestamp;
    }

    @lombok.Data
    @lombok.Builder
    public static class TrafficIncidentDTO {
        private Long alertId;
        private String type;
        private String severity;
        private String location;
        private String description;
        private Integer estimatedDelay;
    }

    @lombok.Data
    @lombok.Builder
    public static class RouteTrafficDTO {
        private Long routeId;
        private String routeName;
        private Integer normalTimeMinutes;
        private Integer currentTimeMinutes;
        private Integer delayMinutes;
        private String trafficLevel;
        private TrafficConditionDTO originTraffic;
        private TrafficConditionDTO destinationTraffic;
    }
}
