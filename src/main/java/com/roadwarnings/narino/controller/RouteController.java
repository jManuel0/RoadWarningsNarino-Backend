package com.roadwarnings.narino.controller;

import com.roadwarnings.narino.dto.request.RouteRequestDTO;
import com.roadwarnings.narino.dto.response.AlertaResponseDTO;
import com.roadwarnings.narino.dto.response.RouteResponseDTO;
import com.roadwarnings.narino.service.RouteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/routes")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class RouteController {

    private final RouteService routeService;

    @PostMapping
    public ResponseEntity<RouteResponseDTO> createRoute(
            @Valid @RequestBody RouteRequestDTO request) {
        RouteResponseDTO response = routeService.createRoute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<RouteResponseDTO>> getAllRoutes() {
        return ResponseEntity.ok(routeService.getAllRoutes());
    }

    @GetMapping("/paginated")
    public ResponseEntity<Page<RouteResponseDTO>> getAllRoutesPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        Sort.Direction direction = sortDirection.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        return ResponseEntity.ok(routeService.getAllRoutesPaginated(pageable));
    }

    @GetMapping("/active")
    public ResponseEntity<List<RouteResponseDTO>> getActiveRoutes() {
        return ResponseEntity.ok(routeService.getActiveRoutes());
    }

    @GetMapping("/active/paginated")
    public ResponseEntity<Page<RouteResponseDTO>> getActiveRoutesPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        Sort.Direction direction = sortDirection.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        return ResponseEntity.ok(routeService.getActiveRoutesPaginated(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RouteResponseDTO> getRouteById(@PathVariable Long id) {
        return ResponseEntity.ok(routeService.getRouteById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RouteResponseDTO> updateRoute(
            @PathVariable Long id,
            @Valid @RequestBody RouteRequestDTO request) {
        return ResponseEntity.ok(routeService.updateRoute(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoute(@PathVariable Long id) {
        routeService.deleteRoute(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/alerts-count")
    public ResponseEntity<RouteResponseDTO> updateActiveAlertsCount(
            @PathVariable Long id,
            @RequestParam Integer count) {
        return ResponseEntity.ok(routeService.updateActiveAlertsCount(id, count));
    }

    @GetMapping("/nearby")
    public ResponseEntity<List<RouteResponseDTO>> getNearbyRoutes(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "10.0") Double radius) {
        return ResponseEntity.ok(routeService.getNearbyRoutes(latitude, longitude, radius));
    }

    @GetMapping("/{id}/alerts")
    public ResponseEntity<List<AlertaResponseDTO>> getAlertsForRoute(
            @PathVariable Long id,
            @RequestParam(defaultValue = "10.0") Double radius) {
        return ResponseEntity.ok(routeService.getAlertsForRoute(id, radius));
    }
}
