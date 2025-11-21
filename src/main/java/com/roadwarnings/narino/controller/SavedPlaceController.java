package com.roadwarnings.narino.controller;

import com.roadwarnings.narino.dto.request.SavedPlaceRequestDTO;
import com.roadwarnings.narino.dto.response.SavedPlaceResponseDTO;
import com.roadwarnings.narino.service.SavedPlaceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users/{userId}/saved-places")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SavedPlaceController {

    private final SavedPlaceService savedPlaceService;

    /**
     * GET /api/users/:userId/saved-places
     * Obtener todos los lugares guardados del usuario
     */
    @GetMapping
    public ResponseEntity<Map<String, List<SavedPlaceResponseDTO>>> getUserSavedPlaces(@PathVariable Long userId) {
        String username = getAuthenticatedUsername();
        List<SavedPlaceResponseDTO> places = savedPlaceService.getUserSavedPlaces(userId, username);
        return ResponseEntity.ok(Map.of("places", places));
    }

    /**
     * POST /api/users/:userId/saved-places
     * Crear un lugar guardado
     */
    @PostMapping
    public ResponseEntity<SavedPlaceResponseDTO> createSavedPlace(
            @PathVariable Long userId,
            @Valid @RequestBody SavedPlaceRequestDTO request) {

        String username = getAuthenticatedUsername();
        SavedPlaceResponseDTO response = savedPlaceService.createSavedPlace(userId, request, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /api/users/:userId/saved-places/:placeId
     * Obtener un lugar guardado por ID
     */
    @GetMapping("/{placeId}")
    public ResponseEntity<SavedPlaceResponseDTO> getSavedPlaceById(
            @PathVariable Long userId,
            @PathVariable Long placeId) {

        String username = getAuthenticatedUsername();
        SavedPlaceResponseDTO response = savedPlaceService.getSavedPlaceById(userId, placeId, username);
        return ResponseEntity.ok(response);
    }

    /**
     * PUT /api/users/:userId/saved-places/:placeId
     * Actualizar un lugar guardado
     */
    @PutMapping("/{placeId}")
    public ResponseEntity<SavedPlaceResponseDTO> updateSavedPlace(
            @PathVariable Long userId,
            @PathVariable Long placeId,
            @Valid @RequestBody SavedPlaceRequestDTO request) {

        String username = getAuthenticatedUsername();
        SavedPlaceResponseDTO response = savedPlaceService.updateSavedPlace(userId, placeId, request, username);
        return ResponseEntity.ok(response);
    }

    /**
     * DELETE /api/users/:userId/saved-places/:placeId
     * Eliminar un lugar guardado
     */
    @DeleteMapping("/{placeId}")
    public ResponseEntity<Void> deleteSavedPlace(
            @PathVariable Long userId,
            @PathVariable Long placeId) {

        String username = getAuthenticatedUsername();
        savedPlaceService.deleteSavedPlace(userId, placeId, username);
        return ResponseEntity.noContent().build();
    }

    private String getAuthenticatedUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Usuario no autenticado");
        }

        if ("anonymousUser".equals(authentication.getPrincipal())) {
            throw new RuntimeException("Usuario no autenticado");
        }

        return authentication.getName();
    }
}
