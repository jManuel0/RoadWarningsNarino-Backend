package com.roadwarnings.narino.controller;

import com.roadwarnings.narino.dto.request.AlertaRequestDTO;
import com.roadwarnings.narino.dto.response.AlertaResponseDTO;
import com.roadwarnings.narino.enums.AlertStatus;
import com.roadwarnings.narino.service.AlertService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication; // 👈 IMPORT CORRECTO
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alert")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AlertController {

    private final AlertService alertService;

    @PostMapping
    public ResponseEntity<AlertaResponseDTO> createAlert(
            @Valid @RequestBody AlertaRequestDTO request,
            Authentication authentication
    ) {
        String username = authentication.getName();
        AlertaResponseDTO response = alertService.createAlert(request, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<AlertaResponseDTO>> getAllAlerts() {
        return ResponseEntity.ok(alertService.getAllAlerts());
    }

    @GetMapping("/active")
    public ResponseEntity<List<AlertaResponseDTO>> getActiveAlerts() {
        return ResponseEntity.ok(alertService.getActiveAlerts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AlertaResponseDTO> getAlertById(@PathVariable Long id) {
        return ResponseEntity.ok(alertService.getAlertById(id));
    }

    @GetMapping("/nearby")
    public ResponseEntity<List<AlertaResponseDTO>> getNearbyAlerts(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "10.0") Double radius
    ) {
        return ResponseEntity.ok(alertService.getNearbyAlerts(latitude, longitude, radius));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AlertaResponseDTO> updateAlert(
            @PathVariable Long id,
            @Valid @RequestBody AlertaRequestDTO request,
            Authentication authentication
    ) {
        String username = authentication.getName();
        return ResponseEntity.ok(alertService.updateAlert(id, request, username));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAlert(
            @PathVariable Long id,
            Authentication authentication
    ) {
        String username = authentication.getName();
        alertService.deleteAlert(id, username);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<AlertaResponseDTO> updateAlertStatus(
            @PathVariable Long id,
            @RequestParam AlertStatus status
    ) {
        return ResponseEntity.ok(alertService.updateAlertStatus(id, status));
    }
}
