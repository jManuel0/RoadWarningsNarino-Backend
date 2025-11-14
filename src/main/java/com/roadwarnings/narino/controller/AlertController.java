package com.roadwarnings.narino.controller;

import com.roadwarnings.narino.dto.request.AlertaRequestDTO;
import com.roadwarnings.narino.dto.request.AlertFilterDTO;
import com.roadwarnings.narino.dto.response.AlertaResponseDTO;
import com.roadwarnings.narino.enums.AlertStatus;
import com.roadwarnings.narino.service.AlertService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/alert")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AlertController {

    private final AlertService alertService;

    @PostMapping
    public ResponseEntity<AlertaResponseDTO> createAlert(
            @Valid @RequestBody AlertaRequestDTO request) {

        String username = getAuthenticatedUsername();
        AlertaResponseDTO response = alertService.createAlert(request, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 🔹 Obtener todas las alertas
    @GetMapping
    public ResponseEntity<List<AlertaResponseDTO>> getAllAlerts() {
        return ResponseEntity.ok(alertService.getAllAlerts());
    }

    // 🔹 Obtener todas las alertas paginadas
    @GetMapping("/paginated")
    public ResponseEntity<Page<AlertaResponseDTO>> getAllAlertsPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        Sort.Direction direction = sortDirection.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        return ResponseEntity.ok(alertService.getAllAlertsPaginated(pageable));
    }

    // 🔹 Solo activas
    @GetMapping("/active")
    public ResponseEntity<List<AlertaResponseDTO>> getActiveAlerts() {
        return ResponseEntity.ok(alertService.getActiveAlerts());
    }

    // 🔹 Solo activas paginadas
    @GetMapping("/active/paginated")
    public ResponseEntity<Page<AlertaResponseDTO>> getActiveAlertsPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        Sort.Direction direction = sortDirection.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        return ResponseEntity.ok(alertService.getActiveAlertsPaginated(pageable));
    }

    // 🔹 Por id
    @GetMapping("/{id}")
    public ResponseEntity<AlertaResponseDTO> getAlertById(@PathVariable Long id) {
        return ResponseEntity.ok(alertService.getAlertById(id));
    }

    // 🔹 Cercanas
    @GetMapping("/nearby")
    public ResponseEntity<List<AlertaResponseDTO>> getNearbyAlerts(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "10.0") Double radius) {
        return ResponseEntity.ok(alertService.getNearbyAlerts(latitude, longitude, radius));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AlertaResponseDTO> updateAlert(
            @PathVariable Long id,
            @Valid @RequestBody AlertaRequestDTO request) {

        String username = getAuthenticatedUsername();
        return ResponseEntity.ok(alertService.updateAlert(id, request, username));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAlert(@PathVariable Long id) {
        String username = getAuthenticatedUsername();
        alertService.deleteAlert(id, username);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<AlertaResponseDTO> updateAlertStatus(
            @PathVariable Long id,
            @RequestParam AlertStatus status) {
        return ResponseEntity.ok(alertService.updateAlertStatus(id, status));
    }

    @PostMapping("/{id}/upvote")
    public ResponseEntity<AlertaResponseDTO> upvoteAlert(@PathVariable Long id) {
        return ResponseEntity.ok(alertService.upvoteAlert(id));
    }

    @PostMapping("/{id}/downvote")
    public ResponseEntity<AlertaResponseDTO> downvoteAlert(@PathVariable Long id) {
        return ResponseEntity.ok(alertService.downvoteAlert(id));
    }

    @GetMapping("/my-alerts")
    public ResponseEntity<Page<AlertaResponseDTO>> getMyAlerts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        String username = getAuthenticatedUsername();
        Sort.Direction direction = sortDirection.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        return ResponseEntity.ok(alertService.getMyAlerts(username, pageable));
    }

    @PostMapping("/filter")
    public ResponseEntity<Page<AlertaResponseDTO>> filterAlerts(
            @Valid @RequestBody AlertFilterDTO filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        Sort.Direction direction = sortDirection.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        return ResponseEntity.ok(alertService.filterAlerts(filter, pageable));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<AlertaResponseDTO>> getAlertsByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        Sort.Direction direction = sortDirection.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        return ResponseEntity.ok(alertService.getAlertsByUser(userId, pageable));
    }

    @PatchMapping("/{id}/expire")
    public ResponseEntity<AlertaResponseDTO> expireAlert(@PathVariable Long id) {
        String username = getAuthenticatedUsername();
        return ResponseEntity.ok(alertService.expireAlert(id, username));
    }

    /**
     * Obtiene el username del usuario autenticado desde el SecurityContext.
     * Si no hay usuario autenticado, retorna null.
     */
    private String getAuthenticatedUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        // Si el principal es "anonymousUser", retornar null
        if ("anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }

        return authentication.getName();
    }
}

