package com.roadwarnings.narino.service;

import com.roadwarnings.narino.dto.request.FavoriteRouteRequestDTO;
import com.roadwarnings.narino.dto.response.FavoriteAlertResponseDTO;
import com.roadwarnings.narino.dto.response.FavoriteRouteResponseDTO;
import com.roadwarnings.narino.entity.*;
import com.roadwarnings.narino.exception.ResourceNotFoundException;
import com.roadwarnings.narino.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class FavoriteService {

    private final FavoriteRouteRepository favoriteRouteRepository;
    private final FavoriteAlertRepository favoriteAlertRepository;
    private final UserRepository userRepository;
    private final RouteRepository routeRepository;
    private final AlertRepository alertRepository;

    private static final String USER_NOT_FOUND = "Usuario no encontrado";
    private static final String ROUTE_NOT_FOUND = "Ruta no encontrada";
    private static final String ALERT_NOT_FOUND = "Alerta no encontrada";

    // ==================== FAVORITE ROUTES ====================

    public FavoriteRouteResponseDTO addFavoriteRoute(String username, FavoriteRouteRequestDTO request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));

        Route route = routeRepository.findById(request.getRouteId())
                .orElseThrow(() -> new ResourceNotFoundException(ROUTE_NOT_FOUND));

        if (favoriteRouteRepository.existsByUserIdAndRouteId(user.getId(), route.getId())) {
            throw new RuntimeException("Esta ruta ya está en favoritos");
        }

        FavoriteRoute favoriteRoute = FavoriteRoute.builder()
                .user(user)
                .route(route)
                .customName(request.getCustomName())
                .notificationsEnabled(request.getNotificationsEnabled() != null ?
                        request.getNotificationsEnabled() : true)
                .build();

        favoriteRoute = favoriteRouteRepository.save(favoriteRoute);
        log.info("Ruta {} agregada a favoritos por usuario {}", route.getId(), username);

        return mapFavoriteRouteToDTO(favoriteRoute);
    }

    public void removeFavoriteRoute(String username, Long routeId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));

        if (!favoriteRouteRepository.existsByUserIdAndRouteId(user.getId(), routeId)) {
            throw new ResourceNotFoundException("Esta ruta no está en favoritos");
        }

        favoriteRouteRepository.deleteByUserIdAndRouteId(user.getId(), routeId);
        log.info("Ruta {} removida de favoritos por usuario {}", routeId, username);
    }

    public List<FavoriteRouteResponseDTO> getUserFavoriteRoutes(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));

        return favoriteRouteRepository.findByUserId(user.getId()).stream()
                .map(this::mapFavoriteRouteToDTO)
                .toList();
    }

    public Page<FavoriteRouteResponseDTO> getUserFavoriteRoutesPaginated(String username, Pageable pageable) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));

        return favoriteRouteRepository.findByUserId(user.getId(), pageable)
                .map(this::mapFavoriteRouteToDTO);
    }

    public FavoriteRouteResponseDTO updateFavoriteRouteLastUsed(String username, Long routeId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));

        FavoriteRoute favoriteRoute = favoriteRouteRepository.findByUserIdAndRouteId(user.getId(), routeId)
                .orElseThrow(() -> new ResourceNotFoundException("Ruta favorita no encontrada"));

        favoriteRoute.setLastUsed(LocalDateTime.now());
        favoriteRoute = favoriteRouteRepository.save(favoriteRoute);

        return mapFavoriteRouteToDTO(favoriteRoute);
    }

    // ==================== FAVORITE ALERTS ====================

    public FavoriteAlertResponseDTO addFavoriteAlert(String username, Long alertId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));

        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException(ALERT_NOT_FOUND));

        if (favoriteAlertRepository.existsByUserIdAndAlertId(user.getId(), alertId)) {
            throw new RuntimeException("Esta alerta ya está en favoritos");
        }

        FavoriteAlert favoriteAlert = FavoriteAlert.builder()
                .user(user)
                .alert(alert)
                .build();

        favoriteAlert = favoriteAlertRepository.save(favoriteAlert);
        log.info("Alerta {} agregada a favoritos por usuario {}", alertId, username);

        return mapFavoriteAlertToDTO(favoriteAlert);
    }

    public void removeFavoriteAlert(String username, Long alertId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));

        if (!favoriteAlertRepository.existsByUserIdAndAlertId(user.getId(), alertId)) {
            throw new ResourceNotFoundException("Esta alerta no está en favoritos");
        }

        favoriteAlertRepository.deleteByUserIdAndAlertId(user.getId(), alertId);
        log.info("Alerta {} removida de favoritos por usuario {}", alertId, username);
    }

    public List<FavoriteAlertResponseDTO> getUserFavoriteAlerts(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));

        return favoriteAlertRepository.findByUserId(user.getId()).stream()
                .map(this::mapFavoriteAlertToDTO)
                .toList();
    }

    public Page<FavoriteAlertResponseDTO> getUserFavoriteAlertsPaginated(String username, Pageable pageable) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));

        return favoriteAlertRepository.findByUserId(user.getId(), pageable)
                .map(this::mapFavoriteAlertToDTO);
    }

    public boolean isFavoriteRoute(String username, Long routeId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));

        return favoriteRouteRepository.existsByUserIdAndRouteId(user.getId(), routeId);
    }

    public boolean isFavoriteAlert(String username, Long alertId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));

        return favoriteAlertRepository.existsByUserIdAndAlertId(user.getId(), alertId);
    }

    // ==================== MAPPERS ====================

    private FavoriteRouteResponseDTO mapFavoriteRouteToDTO(FavoriteRoute favoriteRoute) {
        Route route = favoriteRoute.getRoute();

        return FavoriteRouteResponseDTO.builder()
                .id(favoriteRoute.getId())
                .userId(favoriteRoute.getUser().getId())
                .routeId(route.getId())
                .routeName(route.getName())
                .customName(favoriteRoute.getCustomName())
                .notificationsEnabled(favoriteRoute.getNotificationsEnabled())
                .savedAt(favoriteRoute.getSavedAt())
                .lastUsed(favoriteRoute.getLastUsed())
                .originName(route.getOriginName())
                .destinationName(route.getDestinationName())
                .distanceKm(route.getDistanceKm())
                .activeAlertsCount(route.getActiveAlertsCount())
                .build();
    }

    private FavoriteAlertResponseDTO mapFavoriteAlertToDTO(FavoriteAlert favoriteAlert) {
        Alert alert = favoriteAlert.getAlert();

        return FavoriteAlertResponseDTO.builder()
                .id(favoriteAlert.getId())
                .userId(favoriteAlert.getUser().getId())
                .alertId(alert.getId())
                .alertTitle(alert.getTitle())
                .savedAt(favoriteAlert.getSavedAt())
                .alertType(alert.getType().toString())
                .alertSeverity(alert.getSeverity().toString())
                .alertStatus(alert.getStatus().toString())
                .latitude(alert.getLatitude())
                .longitude(alert.getLongitude())
                .location(alert.getLocation())
                .build();
    }
}
