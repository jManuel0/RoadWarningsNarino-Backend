package com.roadwarnings.narino.controller;

import com.roadwarnings.narino.dto.request.RouteCalculationRequestDTO;
import com.roadwarnings.narino.dto.response.RouteCalculationResponseDTO;
import com.roadwarnings.narino.service.GoogleMapsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/routes")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class RoutesController {

    private final GoogleMapsService googleMapsService;

    /**
     * POST /api/routes/calculate
     * Calcular rutas entre dos puntos
     */
    @PostMapping("/calculate")
    public ResponseEntity<RouteCalculationResponseDTO> calculateRoutes(
            @Valid @RequestBody RouteCalculationRequestDTO request) {

        RouteCalculationResponseDTO response = googleMapsService.calculateRoutes(request);
        return ResponseEntity.ok(response);
    }
}
