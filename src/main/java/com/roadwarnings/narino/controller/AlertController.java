package com.roadwarnings.narino.controller;

import com.roadwarnings.narino.dto.request.AlertaRequestDTO;
import com.roadwarnings.narino.dto.response.AlertaResponseDTO;
import com.roadwarnings.narino.enums.AlertStatus;
import com.roadwarnings.narino.service.AlertService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication; // ✅ IMPORT CORRECTO
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alert") // ✅ AHORA COINCIDE CON EL FRONTEND
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173") // o "*" si prefieres
public class AlertController {

    private final AlertService alertService;

    // CREATE
    @PostMapping
    public ResponseEntity<AlertaResponseDTO> createAlert(
            @Valid @RequestBody AlertaRequestDTO request,
            Authentication authentication
    ) {
        // Si no tienes Spring Security configurado, evitamos NPE
        String username = (authentication != null)
                ? authentication.getName()
                : "system";

        AlertaResponseDTO response = alertService.createAlert(request, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // READ - todas
    @GetMapping
    public ResponseEntity<List<AlertaResponseDTO>> getAllAlerts() {
        return ResponseEntity.ok(alertService.getAllAlerts());
    }

    // READ - activas
    @GetMapping("/active")
    public ResponseEntity<List<AlertaResponseDTO>> getActiveAlerts() {
        return ResponseEntity.ok(alertService.getActiveAlerts());
    }

    // READ - por id
    @GetMapping("/{id}")
    public ResponseEntity<AlertaResponseDTO> getAlertById(@PathVariable Long id) {
        return ResponseEntity.ok(alertService.getAlertById(id));
    }

    // READ - cercanas
    @GetMapping("/nearby")
    public ResponseEntity<List<AlertaResponseDTO>> getNearbyAlerts(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "10.0") Double radius
    ) {
        return ResponseEntity.ok(
                alertService.getNearbyAlerts(latitude, longitude, radius)
        );
    }

    // UPDATE - completa
    @PutMapping("/{id}")
    public ResponseEntity<AlertaResponseDTO> updateAlert(
            @PathVariable Long id,
            @Valid @RequestBody AlertaRequestDTO request,
            Authentication authentication
    ) {
        String username = (authentication != null)
                ? authentication.getName()
                : "system";

        AlertaResponseDTO response = alertService.updateAlert(id, request, username);
        return ResponseEntity.ok(response);
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAlert(
            @PathVariable Long id,
            Authentication authentication
    ) {
        String username = (authentication != null)
                ? authentication.getName()
                : "system";

        alertService.deleteAlert(id, username);
        return ResponseEntity.noContent().build();
    }

    // PATCH - solo estado
    @PatchMapping("/{id}/status")
    public ResponseEntity<AlertaResponseDTO> updateAlertStatus(
            @PathVariable Long id,
            @RequestParam AlertStatus status
    ) {
        AlertaResponseDTO response = alertService.updateAlertStatus(id, status);
        return ResponseEntity.ok(response);
    }
}
