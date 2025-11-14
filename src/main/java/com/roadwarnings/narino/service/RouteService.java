package com.roadwarnings.narino.service;

import com.roadwarnings.narino.dto.request.RouteRequestDTO;
import com.roadwarnings.narino.dto.response.AlertaResponseDTO;
import com.roadwarnings.narino.dto.response.RouteResponseDTO;
import com.roadwarnings.narino.entity.Alert;
import com.roadwarnings.narino.entity.Route;
import com.roadwarnings.narino.enums.AlertStatus;
import com.roadwarnings.narino.exception.ResourceNotFoundException;
import com.roadwarnings.narino.repository.AlertRepository;
import com.roadwarnings.narino.repository.RouteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class RouteService {

    private final RouteRepository routeRepository;
    private final AlertRepository alertRepository;

    private static final String ROUTE_NOT_FOUND = "Ruta no encontrada";

    public RouteResponseDTO createRoute(RouteRequestDTO request) {
        log.info("Creando ruta: {}", request.getName());

        Route route = Route.builder()
                .name(request.getName())
                .originLatitude(request.getOriginLatitude())
                .originLongitude(request.getOriginLongitude())
                .originName(request.getOriginName())
                .destinationLatitude(request.getDestinationLatitude())
                .destinationLongitude(request.getDestinationLongitude())
                .destinationName(request.getDestinationName())
                .distanceKm(request.getDistanceKm())
                .estimatedTimeMinutes(request.getEstimatedTimeMinutes())
                .polyline(request.getPolyline())
                .activeAlertsCount(0)
                .isActive(true)
                .build();

        route = routeRepository.save(route);
        log.info("Ruta creada con ID: {}", route.getId());

        return mapToResponseDTO(route);
    }

    public List<RouteResponseDTO> getAllRoutes() {
        return routeRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    public Page<RouteResponseDTO> getAllRoutesPaginated(Pageable pageable) {
        return routeRepository.findAll(pageable)
                .map(this::mapToResponseDTO);
    }

    public List<RouteResponseDTO> getActiveRoutes() {
        return routeRepository.findByIsActive(true).stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    public Page<RouteResponseDTO> getActiveRoutesPaginated(Pageable pageable) {
        return routeRepository.findByIsActive(true, pageable)
                .map(this::mapToResponseDTO);
    }

    public RouteResponseDTO getRouteById(Long id) {
        Route route = routeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ROUTE_NOT_FOUND));

        return mapToResponseDTO(route);
    }

    public RouteResponseDTO updateRoute(Long id, RouteRequestDTO request) {
        Route route = routeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ROUTE_NOT_FOUND));

        route.setName(request.getName());
        route.setOriginLatitude(request.getOriginLatitude());
        route.setOriginLongitude(request.getOriginLongitude());
        route.setOriginName(request.getOriginName());
        route.setDestinationLatitude(request.getDestinationLatitude());
        route.setDestinationLongitude(request.getDestinationLongitude());
        route.setDestinationName(request.getDestinationName());
        route.setDistanceKm(request.getDistanceKm());
        route.setEstimatedTimeMinutes(request.getEstimatedTimeMinutes());
        route.setPolyline(request.getPolyline());

        route = routeRepository.save(route);
        log.info("Ruta {} actualizada", id);

        return mapToResponseDTO(route);
    }

    public void deleteRoute(Long id) {
        Route route = routeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ROUTE_NOT_FOUND));

        route.setIsActive(false);
        routeRepository.save(route);
        log.info("Ruta {} desactivada", id);
    }

    public RouteResponseDTO updateActiveAlertsCount(Long id, Integer count) {
        Route route = routeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ROUTE_NOT_FOUND));

        route.setActiveAlertsCount(count);
        route = routeRepository.save(route);

        log.info("Ruta {}: alertas activas actualizadas a {}", id, count);
        return mapToResponseDTO(route);
    }

    /**
     * Obtiene rutas cercanas a una ubicacion
     */
    public List<RouteResponseDTO> getNearbyRoutes(Double latitude, Double longitude, Double radiusKm) {
        return routeRepository.findByIsActive(true).stream()
                .filter(route -> {
                    double routeMidLat = (route.getOriginLatitude() + route.getDestinationLatitude()) / 2;
                    double routeMidLon = (route.getOriginLongitude() + route.getDestinationLongitude()) / 2;
                    double distance = calculateDistance(latitude, longitude, routeMidLat, routeMidLon);
                    return distance <= radiusKm;
                })
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene alertas activas para una ruta especifica
     */
    public List<AlertaResponseDTO> getAlertsForRoute(Long routeId, Double radiusKm) {
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new ResourceNotFoundException(ROUTE_NOT_FOUND));

        double routeMidLat = (route.getOriginLatitude() + route.getDestinationLatitude()) / 2;
        double routeMidLon = (route.getOriginLongitude() + route.getDestinationLongitude()) / 2;

        return alertRepository.findByStatus(AlertStatus.ACTIVE).stream()
                .filter(alert -> {
                    double distance = calculateDistance(
                            alert.getLatitude(), alert.getLongitude(),
                            routeMidLat, routeMidLon
                    );
                    return distance <= radiusKm;
                })
                .map(this::mapAlertToResponseDTO)
                .collect(Collectors.toList());
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int EARTH_RADIUS = 6371; // km

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }

    private RouteResponseDTO mapToResponseDTO(Route route) {
        return RouteResponseDTO.builder()
                .id(route.getId())
                .name(route.getName())
                .originLatitude(route.getOriginLatitude())
                .originLongitude(route.getOriginLongitude())
                .originName(route.getOriginName())
                .destinationLatitude(route.getDestinationLatitude())
                .destinationLongitude(route.getDestinationLongitude())
                .destinationName(route.getDestinationName())
                .distanceKm(route.getDistanceKm())
                .estimatedTimeMinutes(route.getEstimatedTimeMinutes())
                .polyline(route.getPolyline())
                .activeAlertsCount(route.getActiveAlertsCount())
                .isActive(route.getIsActive())
                .createdAt(route.getCreatedAt())
                .updatedAt(route.getUpdatedAt())
                .build();
    }

    private AlertaResponseDTO mapAlertToResponseDTO(Alert alert) {
        return AlertaResponseDTO.builder()
                .id(alert.getId())
                .type(alert.getType())
                .title(alert.getTitle())
                .description(alert.getDescription())
                .latitude(alert.getLatitude())
                .longitude(alert.getLongitude())
                .location(alert.getLocation())
                .severity(alert.getSeverity())
                .status(alert.getStatus())
                .imageUrl(alert.getImageUrl())
                .upvotes(alert.getUpvotes())
                .downvotes(alert.getDownvotes())
                .createdAt(alert.getCreatedAt())
                .updatedAt(alert.getUpdatedAt())
                .expiresAt(alert.getExpiresAt())
                .userId(alert.getUser() != null ? alert.getUser().getId() : null)
                .username(alert.getUser() != null ? alert.getUser().getUsername() : null)
                .build();
    }
}
