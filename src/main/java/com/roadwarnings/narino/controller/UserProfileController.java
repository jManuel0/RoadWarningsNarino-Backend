package com.roadwarnings.narino.controller;

import com.roadwarnings.narino.dto.response.UserBadgeResponseDTO;
import com.roadwarnings.narino.entity.UserStatistics;
import com.roadwarnings.narino.repository.UserStatisticsRepository;
import com.roadwarnings.narino.service.BadgeService;
import com.roadwarnings.narino.service.ReputationService;
import com.roadwarnings.narino.util.AuthenticationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador para perfil de usuario, estadísticas, reputación y badges
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserProfileController {

    private final UserStatisticsRepository statisticsRepository;
    private final BadgeService badgeService;
    private final ReputationService reputationService;
    private final AuthenticationUtil authenticationUtil;

    /**
     * Obtiene las estadísticas del usuario autenticado
     * GET /api/users/me/statistics
     */
    @GetMapping("/me/statistics")
    public ResponseEntity<UserStatisticsDTO> getMyStatistics() {
        Long userId = authenticationUtil.getAuthenticatedUserId();
        UserStatistics stats = statisticsRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Estadísticas no encontradas"));

        return ResponseEntity.ok(mapToDTO(stats));
    }

    /**
     * Obtiene las estadísticas de un usuario por ID
     * GET /api/users/{userId}/statistics
     */
    @GetMapping("/{userId}/statistics")
    public ResponseEntity<UserStatisticsDTO> getUserStatistics(@PathVariable Long userId) {
        UserStatistics stats = statisticsRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Estadísticas no encontradas"));

        return ResponseEntity.ok(mapToDTO(stats));
    }

    /**
     * Obtiene los badges del usuario autenticado
     * GET /api/users/me/badges
     */
    @GetMapping("/me/badges")
    public ResponseEntity<List<UserBadgeResponseDTO>> getMyBadges() {
        String username = authenticationUtil.getAuthenticatedUsername();
        return ResponseEntity.ok(badgeService.getUserBadges(username));
    }

    /**
     * Obtiene los badges de un usuario por ID
     * GET /api/users/{userId}/badges
     */
    @GetMapping("/{userId}/badges")
    public ResponseEntity<List<UserBadgeResponseDTO>> getUserBadges(@PathVariable Long userId) {
        return ResponseEntity.ok(badgeService.getUserBadgesById(userId));
    }

    /**
     * Obtiene el progreso de nivel del usuario autenticado
     * GET /api/users/me/level-progress
     */
    @GetMapping("/me/level-progress")
    public ResponseEntity<ReputationService.LevelProgressDTO> getMyLevelProgress() {
        Long userId = authenticationUtil.getAuthenticatedUserId();
        return ResponseEntity.ok(reputationService.getLevelProgress(userId));
    }

    /**
     * Obtiene el progreso de nivel de un usuario por ID
     * GET /api/users/{userId}/level-progress
     */
    @GetMapping("/{userId}/level-progress")
    public ResponseEntity<ReputationService.LevelProgressDTO> getUserLevelProgress(@PathVariable Long userId) {
        return ResponseEntity.ok(reputationService.getLevelProgress(userId));
    }

    /**
     * Obtiene el perfil completo del usuario autenticado (stats + badges + nivel)
     * GET /api/users/me/profile
     */
    @GetMapping("/me/profile")
    public ResponseEntity<UserProfileDTO> getMyProfile() {
        Long userId = authenticationUtil.getAuthenticatedUserId();
        String username = authenticationUtil.getAuthenticatedUsername();

        UserStatistics stats = statisticsRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Estadísticas no encontradas"));

        List<UserBadgeResponseDTO> badges = badgeService.getUserBadges(username);
        ReputationService.LevelProgressDTO levelProgress = reputationService.getLevelProgress(userId);

        UserProfileDTO profile = UserProfileDTO.builder()
                .userId(userId)
                .username(username)
                .statistics(mapToDTO(stats))
                .badges(badges)
                .levelProgress(levelProgress)
                .build();

        return ResponseEntity.ok(profile);
    }

    /**
     * Obtiene el perfil completo de un usuario por ID
     * GET /api/users/{userId}/profile
     */
    @GetMapping("/{userId}/profile")
    public ResponseEntity<UserProfileDTO> getUserProfile(@PathVariable Long userId) {
        UserStatistics stats = statisticsRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Estadísticas no encontradas"));

        List<UserBadgeResponseDTO> badges = badgeService.getUserBadgesById(userId);
        ReputationService.LevelProgressDTO levelProgress = reputationService.getLevelProgress(userId);

        UserProfileDTO profile = UserProfileDTO.builder()
                .userId(userId)
                .username(stats.getUser().getUsername())
                .statistics(mapToDTO(stats))
                .badges(badges)
                .levelProgress(levelProgress)
                .build();

        return ResponseEntity.ok(profile);
    }

    // ==================== DTOs ====================

    private UserStatisticsDTO mapToDTO(UserStatistics stats) {
        return UserStatisticsDTO.builder()
                .alertsCreated(stats.getAlertsCreated())
                .alertsVerified(stats.getAlertsVerified())
                .commentsPosted(stats.getCommentsPosted())
                .upvotesReceived(stats.getUpvotesReceived())
                .downvotesReceived(stats.getDownvotesReceived())
                .reportsSubmitted(stats.getReportsSubmitted())
                .validReports(stats.getValidReports())
                .reputationPoints(stats.getReputationPoints())
                .level(stats.getLevel())
                .build();
    }

    @lombok.Data
    @lombok.Builder
    public static class UserStatisticsDTO {
        private Integer alertsCreated;
        private Integer alertsVerified;
        private Integer commentsPosted;
        private Integer upvotesReceived;
        private Integer downvotesReceived;
        private Integer reportsSubmitted;
        private Integer validReports;
        private Integer reputationPoints;
        private Integer level;
    }

    @lombok.Data
    @lombok.Builder
    public static class UserProfileDTO {
        private Long userId;
        private String username;
        private UserStatisticsDTO statistics;
        private List<UserBadgeResponseDTO> badges;
        private ReputationService.LevelProgressDTO levelProgress;
    }
}
