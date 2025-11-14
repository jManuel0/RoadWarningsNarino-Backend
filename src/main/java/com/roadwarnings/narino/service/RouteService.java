package com.roadwarnings.narino.service;

import com.roadwarnings.narino.dto.request.RouteRequestDTO;
import com.roadwarnings.narino.dto.response.RouteResponseDTO;
import com.roadwarnings.narino.entity.Route;
import com.roadwarnings.narino.exception.ResourceNotFoundException;
import com.roadwarnings.narino.repository.RouteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class RouteService {

    private final RouteRepository routeRepository;

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
}
