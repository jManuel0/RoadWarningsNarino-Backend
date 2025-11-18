package com.roadwarnings.narino.service;

import com.roadwarnings.narino.entity.UserStatistics;
import com.roadwarnings.narino.enums.BadgeType;
import com.roadwarnings.narino.enums.NotificationType;
import com.roadwarnings.narino.repository.UserStatisticsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio para gestionar el sistema de reputación y niveles de usuarios
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ReputationService {

    private final UserStatisticsRepository statisticsRepository;
    private final BadgeService badgeService;
    private final NotificationService notificationService;

    // Constantes de puntos de reputación
    private static final int POINTS_ALERT_CREATED = 10;
    private static final int POINTS_ALERT_UPVOTED = 5;
    private static final int POINTS_ALERT_DOWNVOTED = -3;
    private static final int POINTS_COMMENT_CREATED = 2;
    private static final int POINTS_ALERT_VERIFIED = 15;
    private static final int POINTS_ALERT_REPORTED_VALID = 20;
    private static final int POINTS_ALERT_REPORTED_INVALID = -10;

    // Constantes de niveles
    private static final int[] LEVEL_THRESHOLDS = {
            0,      // Nivel 1: 0-99 puntos
            100,    // Nivel 2: 100-249 puntos
            250,    // Nivel 3: 250-499 puntos
            500,    // Nivel 4: 500-999 puntos
            1000,   // Nivel 5: 1000-1999 puntos
            2000,   // Nivel 6: 2000-3999 puntos
            4000,   // Nivel 7: 4000-7999 puntos
            8000,   // Nivel 8: 8000-15999 puntos
            16000   // Nivel 9: 16000+ puntos
    };

    /**
     * Otorga puntos cuando un usuario crea una alerta
     */
    public void onAlertCreated(Long userId) {
        UserStatistics stats = getOrCreateStatistics(userId);
        stats.setAlertsCreated(stats.getAlertsCreated() + 1);
        addReputationPoints(stats, POINTS_ALERT_CREATED, "Alerta creada");
        statisticsRepository.save(stats);
        badgeService.checkAndAwardBadges(userId);
    }

    /**
     * Otorga puntos cuando una alerta recibe upvote
     */
    public void onAlertUpvoted(Long alertOwnerId) {
        UserStatistics stats = getOrCreateStatistics(alertOwnerId);
        stats.setUpvotesReceived(stats.getUpvotesReceived() + 1);
        addReputationPoints(stats, POINTS_ALERT_UPVOTED, "Tu alerta recibió un upvote");
        statisticsRepository.save(stats);
        badgeService.checkAndAwardBadges(alertOwnerId);
    }

    /**
     * Resta puntos cuando una alerta recibe downvote
     */
    public void onAlertDownvoted(Long alertOwnerId) {
        UserStatistics stats = getOrCreateStatistics(alertOwnerId);
        stats.setDownvotesReceived(stats.getDownvotesReceived() + 1);
        addReputationPoints(stats, POINTS_ALERT_DOWNVOTED, "Tu alerta recibió un downvote");
        statisticsRepository.save(stats);
    }

    /**
     * Otorga puntos cuando un usuario crea un comentario
     */
    public void onCommentCreated(Long userId) {
        UserStatistics stats = getOrCreateStatistics(userId);
        stats.setCommentsPosted(stats.getCommentsPosted() + 1);
        addReputationPoints(stats, POINTS_COMMENT_CREATED, "Comentario creado");
        statisticsRepository.save(stats);
        badgeService.checkAndAwardBadges(userId);
    }

    /**
     * Otorga puntos cuando una alerta es verificada por otros usuarios
     */
    public void onAlertVerified(Long alertOwnerId) {
        UserStatistics stats = getOrCreateStatistics(alertOwnerId);
        stats.setAlertsVerified(stats.getAlertsVerified() + 1);
        addReputationPoints(stats, POINTS_ALERT_VERIFIED, "Tu alerta fue verificada");
        statisticsRepository.save(stats);
        badgeService.checkAndAwardBadges(alertOwnerId);
    }

    /**
     * Otorga puntos cuando un reporte es validado
     */
    public void onReportValidated(Long reporterId) {
        UserStatistics stats = getOrCreateStatistics(reporterId);
        stats.setValidReports(stats.getValidReports() + 1);
        addReputationPoints(stats, POINTS_ALERT_REPORTED_VALID, "Tu reporte fue validado");
        statisticsRepository.save(stats);
        badgeService.checkAndAwardBadges(reporterId);
    }

    /**
     * Resta puntos cuando un reporte es inválido
     */
    public void onReportRejected(Long reporterId) {
        UserStatistics stats = getOrCreateStatistics(reporterId);
        addReputationPoints(stats, POINTS_ALERT_REPORTED_INVALID, "Tu reporte fue rechazado");
        statisticsRepository.save(stats);
    }

    /**
     * Añade puntos de reputación y verifica si sube de nivel
     */
    private void addReputationPoints(UserStatistics stats, int points, String reason) {
        int oldPoints = stats.getReputationPoints();
        int newPoints = Math.max(0, oldPoints + points); // No permitir puntos negativos
        int oldLevel = stats.getLevel();

        stats.setReputationPoints(newPoints);
        int newLevel = calculateLevel(newPoints);
        stats.setLevel(newLevel);

        log.info("Usuario {} ganó {} puntos de reputación ({}): {} -> {}",
                stats.getUser().getId(), points, reason, oldPoints, newPoints);

        // Verificar si subió de nivel
        if (newLevel > oldLevel) {
            onLevelUp(stats.getUser().getId(), oldLevel, newLevel);
        }
    }

    /**
     * Calcula el nivel basado en puntos de reputación
     */
    private int calculateLevel(int points) {
        for (int i = LEVEL_THRESHOLDS.length - 1; i >= 0; i--) {
            if (points >= LEVEL_THRESHOLDS[i]) {
                return i + 1;
            }
        }
        return 1;
    }

    /**
     * Se ejecuta cuando un usuario sube de nivel
     */
    private void onLevelUp(Long userId, int oldLevel, int newLevel) {
        log.info("¡Usuario {} subió de nivel! {} -> {}", userId, oldLevel, newLevel);

        // Enviar notificación
        notificationService.createNotification(
                userId,
                NotificationType.REPUTATION_MILESTONE,
                "¡Subiste de nivel!",
                "¡Felicidades! Has alcanzado el nivel %d".formatted(newLevel),
                Long.valueOf(newLevel)
        );

        // Otorgar badges especiales por nivel
        awardLevelBadges(userId, newLevel);
    }

    /**
     * Otorga badges especiales al alcanzar ciertos niveles
     */
    private void awardLevelBadges(Long userId, int level) {
        switch (level) {
            case 5 -> badgeService.awardBadgeIfNotExists(userId, BadgeType.TRUSTED_USER);
            case 8 -> badgeService.awardBadgeIfNotExists(userId, BadgeType.COMMUNITY_HERO);
        }
    }

    /**
     * Obtiene o crea las estadísticas de un usuario
     */
    private UserStatistics getOrCreateStatistics(Long userId) {
        return statisticsRepository.findByUserId(userId)
                .orElseGet(() -> {
                    log.warn("No se encontraron estadísticas para usuario {}, creando nuevas", userId);
                    return statisticsRepository.findByUserId(userId)
                            .orElseThrow(() -> new RuntimeException("No se pudieron crear estadísticas para el usuario"));
                });
    }

    /**
     * Obtiene el progreso del usuario hacia el siguiente nivel
     */
    public LevelProgressDTO getLevelProgress(Long userId) {
        UserStatistics stats = getOrCreateStatistics(userId);
        int currentLevel = stats.getLevel();
        int currentPoints = stats.getReputationPoints();

        int pointsForCurrentLevel = currentLevel <= LEVEL_THRESHOLDS.length ?
                LEVEL_THRESHOLDS[currentLevel - 1] : LEVEL_THRESHOLDS[LEVEL_THRESHOLDS.length - 1];

        int pointsForNextLevel = currentLevel < LEVEL_THRESHOLDS.length ?
                LEVEL_THRESHOLDS[currentLevel] : Integer.MAX_VALUE;

        int pointsInCurrentLevel = currentPoints - pointsForCurrentLevel;
        int pointsNeededForNextLevel = pointsForNextLevel - pointsForCurrentLevel;

        double progressPercentage = currentLevel >= LEVEL_THRESHOLDS.length ? 100.0 :
                (double) pointsInCurrentLevel / pointsNeededForNextLevel * 100;

        return LevelProgressDTO.builder()
                .currentLevel(currentLevel)
                .currentPoints(currentPoints)
                .pointsForCurrentLevel(pointsForCurrentLevel)
                .pointsForNextLevel(pointsForNextLevel)
                .pointsInCurrentLevel(pointsInCurrentLevel)
                .pointsNeededForNextLevel(pointsNeededForNextLevel)
                .progressPercentage(progressPercentage)
                .build();
    }

    // ==================== DTO ====================

    @lombok.Data
    @lombok.Builder
    public static class LevelProgressDTO {
        private Integer currentLevel;
        private Integer currentPoints;
        private Integer pointsForCurrentLevel;
        private Integer pointsForNextLevel;
        private Integer pointsInCurrentLevel;
        private Integer pointsNeededForNextLevel;
        private Double progressPercentage;
    }
}
