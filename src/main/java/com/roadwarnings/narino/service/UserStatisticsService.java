package com.roadwarnings.narino.service;

import com.roadwarnings.narino.dto.response.UserStatisticsResponseDTO;
import com.roadwarnings.narino.entity.User;
import com.roadwarnings.narino.entity.UserStatistics;
import com.roadwarnings.narino.exception.ResourceNotFoundException;
import com.roadwarnings.narino.repository.UserBadgeRepository;
import com.roadwarnings.narino.repository.UserRepository;
import com.roadwarnings.narino.repository.UserStatisticsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserStatisticsService {

    private final UserStatisticsRepository statisticsRepository;
    private final UserRepository userRepository;
    private final UserBadgeRepository badgeRepository;

    private static final String USER_NOT_FOUND = "Usuario no encontrado";
    private static final int POINTS_PER_ALERT = 10;
    private static final int POINTS_PER_UPVOTE = 5;
    private static final int POINTS_PER_COMMENT = 3;
    private static final int POINTS_PER_VERIFIED_ALERT = 20;
    private static final int POINTS_PER_VALID_REPORT = 15;

    public UserStatisticsResponseDTO getUserStatistics(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));

        UserStatistics stats = getOrCreateStatistics(user);
        return mapToResponseDTO(stats);
    }

    public UserStatisticsResponseDTO getUserStatisticsById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));

        UserStatistics stats = getOrCreateStatistics(user);
        return mapToResponseDTO(stats);
    }

    public List<UserStatisticsResponseDTO> getTopUsersByReputation(int limit) {
        return statisticsRepository.findTopByReputationPoints(PageRequest.of(0, limit)).stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    public List<UserStatisticsResponseDTO> getTopUsersByAlerts(int limit) {
        return statisticsRepository.findTopByAlertsCreated(PageRequest.of(0, limit)).stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    public List<UserStatisticsResponseDTO> getTopUsersByUpvotes(int limit) {
        return statisticsRepository.findTopByUpvotesReceived(PageRequest.of(0, limit)).stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    public void incrementAlertCreated(Long userId) {
        UserStatistics stats = getOrCreateStatisticsByUserId(userId);
        stats.setAlertsCreated(stats.getAlertsCreated() + 1);
        stats.setLastAlertAt(LocalDateTime.now());
        addReputationPoints(stats, POINTS_PER_ALERT);
        calculateLevel(stats);
        statisticsRepository.save(stats);
        log.info("Estadísticas actualizadas: +1 alerta para usuario {}", userId);
    }

    public void incrementCommentPosted(Long userId) {
        UserStatistics stats = getOrCreateStatisticsByUserId(userId);
        stats.setCommentsPosted(stats.getCommentsPosted() + 1);
        stats.setLastCommentAt(LocalDateTime.now());
        addReputationPoints(stats, POINTS_PER_COMMENT);
        calculateLevel(stats);
        statisticsRepository.save(stats);
        log.info("Estadísticas actualizadas: +1 comentario para usuario {}", userId);
    }

    public void incrementUpvoteReceived(Long userId) {
        UserStatistics stats = getOrCreateStatisticsByUserId(userId);
        stats.setUpvotesReceived(stats.getUpvotesReceived() + 1);
        addReputationPoints(stats, POINTS_PER_UPVOTE);
        calculateLevel(stats);
        statisticsRepository.save(stats);
    }

    public void decrementUpvoteReceived(Long userId) {
        UserStatistics stats = getOrCreateStatisticsByUserId(userId);
        if (stats.getUpvotesReceived() > 0) {
            stats.setUpvotesReceived(stats.getUpvotesReceived() - 1);
            removeReputationPoints(stats, POINTS_PER_UPVOTE);
            calculateLevel(stats);
            statisticsRepository.save(stats);
        }
    }

    public void incrementDownvoteReceived(Long userId) {
        UserStatistics stats = getOrCreateStatisticsByUserId(userId);
        stats.setDownvotesReceived(stats.getDownvotesReceived() + 1);
        removeReputationPoints(stats, 2); // Penalización pequeña
        calculateLevel(stats);
        statisticsRepository.save(stats);
    }

    public void decrementDownvoteReceived(Long userId) {
        UserStatistics stats = getOrCreateStatisticsByUserId(userId);
        if (stats.getDownvotesReceived() > 0) {
            stats.setDownvotesReceived(stats.getDownvotesReceived() - 1);
            addReputationPoints(stats, 2);
            calculateLevel(stats);
            statisticsRepository.save(stats);
        }
    }

    public void incrementAlertVerified(Long userId) {
        UserStatistics stats = getOrCreateStatisticsByUserId(userId);
        stats.setAlertsVerified(stats.getAlertsVerified() + 1);
        addReputationPoints(stats, POINTS_PER_VERIFIED_ALERT);
        calculateLevel(stats);
        statisticsRepository.save(stats);
        log.info("Estadísticas actualizadas: +1 alerta verificada para usuario {}", userId);
    }

    public void incrementReportSubmitted(Long userId) {
        UserStatistics stats = getOrCreateStatisticsByUserId(userId);
        stats.setReportsSubmitted(stats.getReportsSubmitted() + 1);
        statisticsRepository.save(stats);
    }

    public void incrementValidReport(Long userId) {
        UserStatistics stats = getOrCreateStatisticsByUserId(userId);
        stats.setValidReports(stats.getValidReports() + 1);
        addReputationPoints(stats, POINTS_PER_VALID_REPORT);
        calculateLevel(stats);
        statisticsRepository.save(stats);
        log.info("Estadísticas actualizadas: +1 reporte válido para usuario {}", userId);
    }

    private UserStatistics getOrCreateStatistics(User user) {
        return statisticsRepository.findByUserId(user.getId())
                .orElseGet(() -> createStatistics(user));
    }

    private UserStatistics getOrCreateStatisticsByUserId(Long userId) {
        return statisticsRepository.findByUserId(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));
                    return createStatistics(user);
                });
    }

    private UserStatistics createStatistics(User user) {
        UserStatistics stats = UserStatistics.builder()
                .user(user)
                .build();
        return statisticsRepository.save(stats);
    }

    private void addReputationPoints(UserStatistics stats, int points) {
        stats.setReputationPoints(stats.getReputationPoints() + points);
    }

    private void removeReputationPoints(UserStatistics stats, int points) {
        int newPoints = Math.max(0, stats.getReputationPoints() - points);
        stats.setReputationPoints(newPoints);
    }

    private void calculateLevel(UserStatistics stats) {
        int reputation = stats.getReputationPoints();
        int level = 1;

        if (reputation >= 10000) level = 10;
        else if (reputation >= 5000) level = 9;
        else if (reputation >= 2500) level = 8;
        else if (reputation >= 1000) level = 7;
        else if (reputation >= 500) level = 6;
        else if (reputation >= 250) level = 5;
        else if (reputation >= 100) level = 4;
        else if (reputation >= 50) level = 3;
        else if (reputation >= 20) level = 2;

        if (stats.getLevel() != level) {
            log.info("Usuario {} subió a nivel {}", stats.getUser().getId(), level);
        }

        stats.setLevel(level);
    }

    private UserStatisticsResponseDTO mapToResponseDTO(UserStatistics stats) {
        Long badgeCount = badgeRepository.countByUserId(stats.getUser().getId());

        return UserStatisticsResponseDTO.builder()
                .id(stats.getId())
                .userId(stats.getUser().getId())
                .username(stats.getUser().getUsername())
                .alertsCreated(stats.getAlertsCreated())
                .alertsVerified(stats.getAlertsVerified())
                .commentsPosted(stats.getCommentsPosted())
                .upvotesReceived(stats.getUpvotesReceived())
                .downvotesReceived(stats.getDownvotesReceived())
                .reportsSubmitted(stats.getReportsSubmitted())
                .validReports(stats.getValidReports())
                .reputationPoints(stats.getReputationPoints())
                .level(stats.getLevel())
                .lastAlertAt(stats.getLastAlertAt())
                .lastCommentAt(stats.getLastCommentAt())
                .badgeCount(badgeCount.intValue())
                .build();
    }
}
