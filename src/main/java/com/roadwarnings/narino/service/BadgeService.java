package com.roadwarnings.narino.service;

import com.roadwarnings.narino.dto.response.UserBadgeResponseDTO;
import com.roadwarnings.narino.entity.User;
import com.roadwarnings.narino.entity.UserBadge;
import com.roadwarnings.narino.entity.UserStatistics;
import com.roadwarnings.narino.enums.BadgeType;
import com.roadwarnings.narino.enums.NotificationType;
import com.roadwarnings.narino.exception.ResourceNotFoundException;
import com.roadwarnings.narino.repository.UserBadgeRepository;
import com.roadwarnings.narino.repository.UserRepository;
import com.roadwarnings.narino.repository.UserStatisticsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class BadgeService {

    private final UserBadgeRepository badgeRepository;
    private final UserRepository userRepository;
    private final UserStatisticsRepository statisticsRepository;
    private final NotificationService notificationService;

    private static final String USER_NOT_FOUND = "Usuario no encontrado";

    private static final Map<BadgeType, String> BADGE_NAMES = new HashMap<>();
    private static final Map<BadgeType, String> BADGE_DESCRIPTIONS = new HashMap<>();

    static {
        BADGE_NAMES.put(BadgeType.FIRST_ALERT, "Primera Alerta");
        BADGE_NAMES.put(BadgeType.ALERTS_10, "Reportero Activo");
        BADGE_NAMES.put(BadgeType.ALERTS_50, "Reportero Experto");
        BADGE_NAMES.put(BadgeType.ALERTS_100, "Maestro Reportero");
        BADGE_NAMES.put(BadgeType.HELPFUL_REPORTER, "Reportes Útiles");
        BADGE_NAMES.put(BadgeType.TRUSTED_USER, "Usuario Confiable");
        BADGE_NAMES.put(BadgeType.EARLY_ADOPTER, "Adoptador Temprano");
        BADGE_NAMES.put(BadgeType.COMMUNITY_HERO, "Héroe de la Comunidad");
        BADGE_NAMES.put(BadgeType.VERIFIED_ALERTS, "Alertas Verificadas");
        BADGE_NAMES.put(BadgeType.ACTIVE_COMMENTER, "Comentarista Activo");
        BADGE_NAMES.put(BadgeType.ROUTE_EXPERT, "Experto en Rutas");

        BADGE_DESCRIPTIONS.put(BadgeType.FIRST_ALERT, "Creaste tu primera alerta");
        BADGE_DESCRIPTIONS.put(BadgeType.ALERTS_10, "Creaste 10 alertas");
        BADGE_DESCRIPTIONS.put(BadgeType.ALERTS_50, "Creaste 50 alertas");
        BADGE_DESCRIPTIONS.put(BadgeType.ALERTS_100, "Creaste 100 alertas");
        BADGE_DESCRIPTIONS.put(BadgeType.HELPFUL_REPORTER, "Tus reportes son muy útiles");
        BADGE_DESCRIPTIONS.put(BadgeType.TRUSTED_USER, "Eres un usuario de confianza");
        BADGE_DESCRIPTIONS.put(BadgeType.EARLY_ADOPTER, "Uno de los primeros usuarios");
        BADGE_DESCRIPTIONS.put(BadgeType.COMMUNITY_HERO, "Contribuyes enormemente a la comunidad");
        BADGE_DESCRIPTIONS.put(BadgeType.VERIFIED_ALERTS, "Tus alertas son verificadas frecuentemente");
        BADGE_DESCRIPTIONS.put(BadgeType.ACTIVE_COMMENTER, "Comentas activamente en las alertas");
        BADGE_DESCRIPTIONS.put(BadgeType.ROUTE_EXPERT, "Conoces las rutas mejor que nadie");
    }

    public List<UserBadgeResponseDTO> getUserBadges(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));

        return badgeRepository.findByUserId(user.getId()).stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    public List<UserBadgeResponseDTO> getUserBadgesById(Long userId) {
        return badgeRepository.findByUserId(userId).stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    public void checkAndAwardBadges(Long userId) {
        UserStatistics stats = statisticsRepository.findByUserId(userId)
                .orElse(null);

        if (stats == null) {
            return;
        }

        // Verificar badge de primera alerta
        if (stats.getAlertsCreated() >= 1) {
            awardBadgeIfNotExists(userId, BadgeType.FIRST_ALERT);
        }

        // Verificar badges por cantidad de alertas
        if (stats.getAlertsCreated() >= 10) {
            awardBadgeIfNotExists(userId, BadgeType.ALERTS_10);
        }
        if (stats.getAlertsCreated() >= 50) {
            awardBadgeIfNotExists(userId, BadgeType.ALERTS_50);
        }
        if (stats.getAlertsCreated() >= 100) {
            awardBadgeIfNotExists(userId, BadgeType.ALERTS_100);
        }

        // Badge de reportes útiles
        if (stats.getValidReports() >= 5) {
            awardBadgeIfNotExists(userId, BadgeType.HELPFUL_REPORTER);
        }

        // Badge de usuario confiable (alta reputación)
        if (stats.getReputationPoints() >= 500) {
            awardBadgeIfNotExists(userId, BadgeType.TRUSTED_USER);
        }

        // Badge de héroe de la comunidad
        if (stats.getReputationPoints() >= 2000) {
            awardBadgeIfNotExists(userId, BadgeType.COMMUNITY_HERO);
        }

        // Badge de alertas verificadas
        if (stats.getAlertsVerified() >= 10) {
            awardBadgeIfNotExists(userId, BadgeType.VERIFIED_ALERTS);
        }

        // Badge de comentarista activo
        if (stats.getCommentsPosted() >= 50) {
            awardBadgeIfNotExists(userId, BadgeType.ACTIVE_COMMENTER);
        }

        // Badge de experto en rutas (muchas alertas con upvotes)
        if (stats.getUpvotesReceived() >= 100) {
            awardBadgeIfNotExists(userId, BadgeType.ROUTE_EXPERT);
        }
    }

    public void awardBadgeIfNotExists(Long userId, BadgeType badgeType) {
        if (!badgeRepository.existsByUserIdAndBadgeType(userId, badgeType)) {
            awardBadge(userId, badgeType);
        }
    }

    public UserBadgeResponseDTO awardBadge(Long userId, BadgeType badgeType) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));

        // Verificar si ya tiene el badge
        if (badgeRepository.existsByUserIdAndBadgeType(userId, badgeType)) {
            log.info("Usuario {} ya tiene el badge {}", userId, badgeType);
            return null;
        }

        UserBadge badge = UserBadge.builder()
                .user(user)
                .badgeType(badgeType)
                .build();

        badge = badgeRepository.save(badge);
        log.info("Badge {} otorgado a usuario {}", badgeType, userId);

        // Enviar notificación al usuario
        String badgeName = BADGE_NAMES.getOrDefault(badgeType, badgeType.toString());
        String badgeDescription = BADGE_DESCRIPTIONS.getOrDefault(badgeType, "Nuevo logro desbloqueado");

        notificationService.createNotification(
                userId,
                NotificationType.BADGE_EARNED,
                "¡Nuevo logro desbloqueado!",
                "Has ganado el logro: " + badgeName + " - " + badgeDescription,
                badge.getId()
        );

        return mapToResponseDTO(badge);
    }

    public boolean hasBadge(Long userId, BadgeType badgeType) {
        return badgeRepository.existsByUserIdAndBadgeType(userId, badgeType);
    }

    private UserBadgeResponseDTO mapToResponseDTO(UserBadge badge) {
        String badgeName = BADGE_NAMES.getOrDefault(badge.getBadgeType(), badge.getBadgeType().toString());
        String badgeDescription = BADGE_DESCRIPTIONS.getOrDefault(badge.getBadgeType(), "");

        return UserBadgeResponseDTO.builder()
                .id(badge.getId())
                .userId(badge.getUser().getId())
                .username(badge.getUser().getUsername())
                .badgeType(badge.getBadgeType())
                .badgeName(badgeName)
                .badgeDescription(badgeDescription)
                .earnedAt(badge.getEarnedAt())
                .build();
    }
}
