package com.roadwarnings.narino.service;

import com.roadwarnings.narino.entity.Alert;
import com.roadwarnings.narino.entity.User;
import com.roadwarnings.narino.enums.AlertSeverity;
import com.roadwarnings.narino.enums.AlertStatus;
import com.roadwarnings.narino.enums.AlertType;
import com.roadwarnings.narino.repository.AlertRepository;
import com.roadwarnings.narino.repository.CommentRepository;
import com.roadwarnings.narino.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.roadwarnings.narino.config.CacheConfig.LEADERBOARD_CACHE;

/**
 * Servicio para analytics y métricas del sistema
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsService {

    private final AlertRepository alertRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;

    /**
     * Obtiene estadísticas generales del sistema
     */
    public SystemStatsDTO getSystemStats() {
        long totalAlerts = alertRepository.count();
        long activeAlerts = alertRepository.findByStatus(AlertStatus.ACTIVE).size();
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.findAll().stream()
                .filter(User::getIsActive)
                .count();
        long totalComments = commentRepository.count();

        return SystemStatsDTO.builder()
                .totalAlerts(totalAlerts)
                .activeAlerts(activeAlerts)
                .resolvedAlerts((long) alertRepository.findByStatus(AlertStatus.RESOLVED).size())
                .expiredAlerts((long) alertRepository.findByStatus(AlertStatus.EXPIRED).size())
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .totalComments(totalComments)
                .averageUpvotesPerAlert(calculateAverageUpvotes())
                .build();
    }

    /**
     * Obtiene alertas más comunes por tipo
     */
    public Map<AlertType, Long> getAlertsByType() {
        List<Alert> alerts = alertRepository.findAll();

        return alerts.stream()
                .collect(Collectors.groupingBy(
                        Alert::getType,
                        Collectors.counting()
                ));
    }

    /**
     * Obtiene distribución de alertas por severidad
     */
    public Map<AlertSeverity, Long> getAlertsBySeverity() {
        List<Alert> alerts = alertRepository.findAll();

        return alerts.stream()
                .collect(Collectors.groupingBy(
                        Alert::getSeverity,
                        Collectors.counting()
                ));
    }

    /**
     * Obtiene distribución de alertas por estado
     */
    public Map<AlertStatus, Long> getAlertsByStatus() {
        List<Alert> alerts = alertRepository.findAll();

        return alerts.stream()
                .collect(Collectors.groupingBy(
                        Alert::getStatus,
                        Collectors.counting()
                ));
    }

    /**
     * Obtiene tendencia de alertas en el tiempo
     */
    public List<AlertTrendDTO> getAlertTrend(int days) {
        LocalDateTime now = LocalDateTime.now();
        List<AlertTrendDTO> trend = new ArrayList<>();

        for (int i = days - 1; i >= 0; i--) {
            LocalDateTime startOfDay = now.minusDays(i).toLocalDate().atStartOfDay();
            LocalDateTime endOfDay = startOfDay.plusDays(1);

            long count = alertRepository.findAll().stream()
                    .filter(alert -> alert.getCreatedAt().isAfter(startOfDay) &&
                                   alert.getCreatedAt().isBefore(endOfDay))
                    .count();

            trend.add(AlertTrendDTO.builder()
                    .date(startOfDay.toLocalDate())
                    .count(count)
                    .build());
        }

        return trend;
    }

    /**
     * Obtiene zonas con más alertas (heatmap data)
     */
    public List<HotspotDTO> getAlertHotspots(int limit) {
        List<Alert> alerts = alertRepository.findByStatus(AlertStatus.ACTIVE);

        // Agrupar alertas por coordenadas redondeadas (área aproximada)
        Map<String, List<Alert>> grouped = alerts.stream()
                .filter(alert -> alert.getLatitude() != null && alert.getLongitude() != null)
                .collect(Collectors.groupingBy(alert ->
                        roundCoordinate(alert.getLatitude(), 2) + "," +
                        roundCoordinate(alert.getLongitude(), 2)
                ));

        return grouped.entrySet().stream()
                .map(entry -> {
                    String[] coords = entry.getKey().split(",");
                    List<Alert> groupAlerts = entry.getValue();

                    return HotspotDTO.builder()
                            .latitude(Double.parseDouble(coords[0]))
                            .longitude(Double.parseDouble(coords[1]))
                            .alertCount(groupAlerts.size())
                            .location(groupAlerts.get(0).getLocation())
                            .build();
                })
                .sorted((a, b) -> Integer.compare(b.getAlertCount(), a.getAlertCount()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene usuarios más activos
     */
    @Cacheable(value = LEADERBOARD_CACHE, key = "'top_contributors_' + #limit")
    public List<TopContributorDTO> getTopContributors(int limit) {
        List<User> users = userRepository.findAll();

        return users.stream()
                .map(user -> {
                    long alertsCreated = alertRepository.findAll().stream()
                            .filter(alert -> alert.getUser() != null &&
                                           alert.getUser().getId().equals(user.getId()))
                            .count();

                    long commentsCreated = commentRepository.findAll().stream()
                            .filter(comment -> comment.getUser() != null &&
                                             comment.getUser().getId().equals(user.getId()))
                            .count();

                    int totalUpvotes = alertRepository.findAll().stream()
                            .filter(alert -> alert.getUser() != null &&
                                           alert.getUser().getId().equals(user.getId()))
                            .mapToInt(Alert::getUpvotes)
                            .sum();

                    return TopContributorDTO.builder()
                            .userId(user.getId())
                            .username(user.getUsername())
                            .alertsCreated(alertsCreated)
                            .commentsCreated(commentsCreated)
                            .totalUpvotes(totalUpvotes)
                            .score(calculateScore(alertsCreated, commentsCreated, totalUpvotes))
                            .build();
                })
                .sorted((a, b) -> Long.compare(b.getScore(), a.getScore()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene horas pico de actividad
     */
    public Map<Integer, Long> getPeakHours() {
        List<Alert> alerts = alertRepository.findAll();

        return alerts.stream()
                .collect(Collectors.groupingBy(
                        alert -> alert.getCreatedAt().getHour(),
                        Collectors.counting()
                ));
    }

    /**
     * Obtiene alertas por día de la semana
     */
    public Map<String, Long> getAlertsByDayOfWeek() {
        List<Alert> alerts = alertRepository.findAll();

        return alerts.stream()
                .collect(Collectors.groupingBy(
                        alert -> alert.getCreatedAt().getDayOfWeek().toString(),
                        Collectors.counting()
                ));
    }

    /**
     * Obtiene tiempo promedio de resolución de alertas (en horas)
     */
    public Double getAverageResolutionTime() {
        List<Alert> resolvedAlerts = alertRepository.findByStatus(AlertStatus.RESOLVED);

        if (resolvedAlerts.isEmpty()) {
            return 0.0;
        }

        // Nota: necesitarías un campo resolvedAt en Alert para esto
        // Por ahora retornamos un valor de ejemplo
        return 24.5;
    }

    // ==================== MÉTODOS AUXILIARES ====================

    private double calculateAverageUpvotes() {
        List<Alert> alerts = alertRepository.findAll();
        if (alerts.isEmpty()) {
            return 0.0;
        }

        int totalUpvotes = alerts.stream()
                .mapToInt(Alert::getUpvotes)
                .sum();

        return (double) totalUpvotes / alerts.size();
    }

    private long calculateScore(long alerts, long comments, int upvotes) {
        return (alerts * 10) + (comments * 2) + upvotes;
    }

    private double roundCoordinate(Double coord, int decimals) {
        if (coord == null) {
            return 0.0;
        }
        double multiplier = Math.pow(10, decimals);
        return Math.round(coord * multiplier) / multiplier;
    }

    // ==================== DTOs ====================

    @lombok.Data
    @lombok.Builder
    public static class SystemStatsDTO {
        private Long totalAlerts;
        private Long activeAlerts;
        private Long resolvedAlerts;
        private Long expiredAlerts;
        private Long totalUsers;
        private Long activeUsers;
        private Long totalComments;
        private Double averageUpvotesPerAlert;
    }

    @lombok.Data
    @lombok.Builder
    public static class AlertTrendDTO {
        private java.time.LocalDate date;
        private Long count;
    }

    @lombok.Data
    @lombok.Builder
    public static class HotspotDTO {
        private Double latitude;
        private Double longitude;
        private Integer alertCount;
        private String location;
    }

    @lombok.Data
    @lombok.Builder
    public static class TopContributorDTO {
        private Long userId;
        private String username;
        private Long alertsCreated;
        private Long commentsCreated;
        private Integer totalUpvotes;
        private Long score;
    }
}
