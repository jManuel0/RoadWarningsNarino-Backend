package com.roadwarnings.narino.controller;

import com.roadwarnings.narino.enums.AlertSeverity;
import com.roadwarnings.narino.enums.AlertStatus;
import com.roadwarnings.narino.enums.AlertType;
import com.roadwarnings.narino.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controlador para analytics y métricas del sistema
 */
@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    /**
     * Obtiene estadísticas generales del sistema
     * GET /api/analytics/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<AnalyticsService.SystemStatsDTO> getSystemStats() {
        return ResponseEntity.ok(analyticsService.getSystemStats());
    }

    /**
     * Obtiene distribución de alertas por tipo
     * GET /api/analytics/alerts-by-type
     */
    @GetMapping("/alerts-by-type")
    public ResponseEntity<Map<AlertType, Long>> getAlertsByType() {
        return ResponseEntity.ok(analyticsService.getAlertsByType());
    }

    /**
     * Obtiene distribución de alertas por severidad
     * GET /api/analytics/alerts-by-severity
     */
    @GetMapping("/alerts-by-severity")
    public ResponseEntity<Map<AlertSeverity, Long>> getAlertsBySeverity() {
        return ResponseEntity.ok(analyticsService.getAlertsBySeverity());
    }

    /**
     * Obtiene distribución de alertas por estado
     * GET /api/analytics/alerts-by-status
     */
    @GetMapping("/alerts-by-status")
    public ResponseEntity<Map<AlertStatus, Long>> getAlertsByStatus() {
        return ResponseEntity.ok(analyticsService.getAlertsByStatus());
    }

    /**
     * Obtiene tendencia de alertas en los últimos N días
     * GET /api/analytics/trend?days=30
     */
    @GetMapping("/trend")
    public ResponseEntity<List<AnalyticsService.AlertTrendDTO>> getAlertTrend(
            @RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(analyticsService.getAlertTrend(days));
    }

    /**
     * Obtiene zonas con más alertas activas (heatmap)
     * GET /api/analytics/hotspots?limit=10
     */
    @GetMapping("/hotspots")
    public ResponseEntity<List<AnalyticsService.HotspotDTO>> getAlertHotspots(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(analyticsService.getAlertHotspots(limit));
    }

    /**
     * Obtiene usuarios más activos (leaderboard)
     * GET /api/analytics/top-contributors?limit=10
     */
    @GetMapping("/top-contributors")
    public ResponseEntity<List<AnalyticsService.TopContributorDTO>> getTopContributors(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(analyticsService.getTopContributors(limit));
    }

    /**
     * Obtiene horas pico de actividad
     * GET /api/analytics/peak-hours
     */
    @GetMapping("/peak-hours")
    public ResponseEntity<Map<Integer, Long>> getPeakHours() {
        return ResponseEntity.ok(analyticsService.getPeakHours());
    }

    /**
     * Obtiene actividad por día de la semana
     * GET /api/analytics/by-day-of-week
     */
    @GetMapping("/by-day-of-week")
    public ResponseEntity<Map<String, Long>> getAlertsByDayOfWeek() {
        return ResponseEntity.ok(analyticsService.getAlertsByDayOfWeek());
    }

    /**
     * Obtiene tiempo promedio de resolución de alertas
     * GET /api/analytics/avg-resolution-time
     */
    @GetMapping("/avg-resolution-time")
    public ResponseEntity<Double> getAverageResolutionTime() {
        return ResponseEntity.ok(analyticsService.getAverageResolutionTime());
    }

    /**
     * Obtiene dashboard completo con todas las métricas
     * GET /api/analytics/dashboard
     */
    @GetMapping("/dashboard")
    public ResponseEntity<DashboardDTO> getDashboard() {
        DashboardDTO dashboard = DashboardDTO.builder()
                .systemStats(analyticsService.getSystemStats())
                .alertsByType(analyticsService.getAlertsByType())
                .alertsBySeverity(analyticsService.getAlertsBySeverity())
                .alertsByStatus(analyticsService.getAlertsByStatus())
                .trend(analyticsService.getAlertTrend(30))
                .hotspots(analyticsService.getAlertHotspots(10))
                .topContributors(analyticsService.getTopContributors(10))
                .peakHours(analyticsService.getPeakHours())
                .alertsByDayOfWeek(analyticsService.getAlertsByDayOfWeek())
                .build();

        return ResponseEntity.ok(dashboard);
    }

    // ==================== DTOs ====================

    @lombok.Data
    @lombok.Builder
    public static class DashboardDTO {
        private AnalyticsService.SystemStatsDTO systemStats;
        private Map<AlertType, Long> alertsByType;
        private Map<AlertSeverity, Long> alertsBySeverity;
        private Map<AlertStatus, Long> alertsByStatus;
        private List<AnalyticsService.AlertTrendDTO> trend;
        private List<AnalyticsService.HotspotDTO> hotspots;
        private List<AnalyticsService.TopContributorDTO> topContributors;
        private Map<Integer, Long> peakHours;
        private Map<String, Long> alertsByDayOfWeek;
    }
}
