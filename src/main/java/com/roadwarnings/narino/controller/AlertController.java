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
@RequestMapping("/api/alert") // 👈 IMPORTANTE: coincide con el frontend
@RequiredArgsConstructor
@CrossOrigin(
        origins = {
                "http://localhost:5173",          // frontend local
                "https://TU-FRONTEND.onrender.com" // 👈 cambia esto por tu URL real en Render
        }
)
public class AlertController {

    private final AlertService alertService;

    @PostMapping
    public ResponseEntity<AlertaResponseDTO> createAlert(
            @Valid @RequestBody AlertaRequestDTO request,
            Authentication authentication
    ) {
        // Si aún no tienes login real, evitamos NPE:
        String username = (authentication != null)
                ? authentication.getName()
                : "public"; // o algún usuario por defecto que exista en tu BD

        AlertaResponseDTO response = alertService.createAlert(request, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<AlertaResponseDTO>> getAllAlerts() {
        List<AlertaResponseDTO> alerts = alertService.getAllAlerts();
        return ResponseEntity.ok(alerts);
    }

    @GetMapping("/active")
    public ResponseEntity<List<AlertaResponseDTO>> getActiveAlerts() {
        List<AlertaResponseDTO> alerts = alertService.getActiveAlerts();
        return ResponseEntity.ok(alerts);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AlertaResponseDTO> getAlertById(@PathVariable Long id) {
        AlertaResponseDTO alert = alertService.getAlertById(id);
        return ResponseEntity.ok(alert);
    }

    @GetMapping("/nearby")
    public ResponseEntity<List<AlertaResponseDTO>> getNearbyAlerts(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "10.0") Double radius
    ) {
        List<AlertaResponseDTO> alerts =
                alertService.getNearbyAlerts(latitude, longitude, radius);
        return ResponseEntity.ok(alerts);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AlertaResponseDTO> updateAlert(
            @PathVariable Long id,
            @Valid @RequestBody AlertaRequestDTO request,
            Authentication authentication
    ) {
        String username = (authentication != null)
                ? authentication.getName()
                : "public";
        AlertaResponseDTO response =
                alertService.updateAlert(id, request, username);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAlert(
            @PathVariable Long id,
            Authentication authentication
    ) {
        String username = (authentication != null)
                ? authentication.getName()
                : "public";
        alertService.deleteAlert(id, username);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<AlertaResponseDTO> updateAlertStatus(
            @PathVariable Long id,
            @RequestParam AlertStatus status
    ) {
        AlertaResponseDTO response = alertService.updateAlertStatus(id, status);
        return ResponseEntity.ok(response);
    }
}
