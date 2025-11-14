package com.roadwarnings.narino.controller;

import com.roadwarnings.narino.dto.response.UserBadgeResponseDTO;
import com.roadwarnings.narino.dto.response.UserStatisticsResponseDTO;
import com.roadwarnings.narino.service.BadgeService;
import com.roadwarnings.narino.service.UserStatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class StatisticsController {

    private final UserStatisticsService statisticsService;
    private final BadgeService badgeService;

    @GetMapping("/me")
    public ResponseEntity<UserStatisticsResponseDTO> getMyStatistics() {
        String username = getAuthenticatedUsername();
        return ResponseEntity.ok(statisticsService.getUserStatistics(username));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<UserStatisticsResponseDTO> getUserStatistics(@PathVariable Long userId) {
        return ResponseEntity.ok(statisticsService.getUserStatisticsById(userId));
    }

    @GetMapping("/leaderboard/reputation")
    public ResponseEntity<List<UserStatisticsResponseDTO>> getTopByReputation(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(statisticsService.getTopUsersByReputation(limit));
    }

    @GetMapping("/leaderboard/alerts")
    public ResponseEntity<List<UserStatisticsResponseDTO>> getTopByAlerts(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(statisticsService.getTopUsersByAlerts(limit));
    }

    @GetMapping("/leaderboard/upvotes")
    public ResponseEntity<List<UserStatisticsResponseDTO>> getTopByUpvotes(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(statisticsService.getTopUsersByUpvotes(limit));
    }

    // ==================== BADGES ====================

    @GetMapping("/badges/me")
    public ResponseEntity<List<UserBadgeResponseDTO>> getMyBadges() {
        String username = getAuthenticatedUsername();
        return ResponseEntity.ok(badgeService.getUserBadges(username));
    }

    @GetMapping("/badges/user/{userId}")
    public ResponseEntity<List<UserBadgeResponseDTO>> getUserBadges(@PathVariable Long userId) {
        return ResponseEntity.ok(badgeService.getUserBadgesById(userId));
    }

    private String getAuthenticatedUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }
}
