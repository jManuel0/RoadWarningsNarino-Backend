package com.roadwarnings.narino.controller;

import com.roadwarnings.narino.dto.request.FavoriteRouteRequestDTO;
import com.roadwarnings.narino.dto.response.FavoriteAlertResponseDTO;
import com.roadwarnings.narino.dto.response.FavoriteRouteResponseDTO;
import com.roadwarnings.narino.service.FavoriteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class FavoriteController {

    private final FavoriteService favoriteService;

    // ==================== FAVORITE ROUTES ====================

    @PostMapping("/routes")
    public ResponseEntity<FavoriteRouteResponseDTO> addFavoriteRoute(
            @Valid @RequestBody FavoriteRouteRequestDTO request) {

        String username = getAuthenticatedUsername();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(favoriteService.addFavoriteRoute(username, request));
    }

    @GetMapping("/routes")
    public ResponseEntity<List<FavoriteRouteResponseDTO>> getMyFavoriteRoutes() {
        String username = getAuthenticatedUsername();
        return ResponseEntity.ok(favoriteService.getUserFavoriteRoutes(username));
    }

    @GetMapping("/routes/paginated")
    public ResponseEntity<Page<FavoriteRouteResponseDTO>> getMyFavoriteRoutesPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        String username = getAuthenticatedUsername();
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(favoriteService.getUserFavoriteRoutesPaginated(username, pageable));
    }

    @DeleteMapping("/routes/{routeId}")
    public ResponseEntity<Void> removeFavoriteRoute(@PathVariable Long routeId) {
        String username = getAuthenticatedUsername();
        favoriteService.removeFavoriteRoute(username, routeId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/routes/{routeId}/last-used")
    public ResponseEntity<FavoriteRouteResponseDTO> updateLastUsed(@PathVariable Long routeId) {
        String username = getAuthenticatedUsername();
        return ResponseEntity.ok(favoriteService.updateFavoriteRouteLastUsed(username, routeId));
    }

    @GetMapping("/routes/{routeId}/is-favorite")
    public ResponseEntity<Boolean> isFavoriteRoute(@PathVariable Long routeId) {
        String username = getAuthenticatedUsername();
        return ResponseEntity.ok(favoriteService.isFavoriteRoute(username, routeId));
    }

    // ==================== FAVORITE ALERTS ====================

    @PostMapping("/alerts/{alertId}")
    public ResponseEntity<FavoriteAlertResponseDTO> addFavoriteAlert(@PathVariable Long alertId) {
        String username = getAuthenticatedUsername();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(favoriteService.addFavoriteAlert(username, alertId));
    }

    @GetMapping("/alerts")
    public ResponseEntity<List<FavoriteAlertResponseDTO>> getMyFavoriteAlerts() {
        String username = getAuthenticatedUsername();
        return ResponseEntity.ok(favoriteService.getUserFavoriteAlerts(username));
    }

    @GetMapping("/alerts/paginated")
    public ResponseEntity<Page<FavoriteAlertResponseDTO>> getMyFavoriteAlertsPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        String username = getAuthenticatedUsername();
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(favoriteService.getUserFavoriteAlertsPaginated(username, pageable));
    }

    @DeleteMapping("/alerts/{alertId}")
    public ResponseEntity<Void> removeFavoriteAlert(@PathVariable Long alertId) {
        String username = getAuthenticatedUsername();
        favoriteService.removeFavoriteAlert(username, alertId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/alerts/{alertId}/is-favorite")
    public ResponseEntity<Boolean> isFavoriteAlert(@PathVariable Long alertId) {
        String username = getAuthenticatedUsername();
        return ResponseEntity.ok(favoriteService.isFavoriteAlert(username, alertId));
    }

    private String getAuthenticatedUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }
}
