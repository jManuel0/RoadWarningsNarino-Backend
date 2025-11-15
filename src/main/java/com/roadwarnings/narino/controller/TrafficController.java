package com.roadwarnings.narino.controller;

import com.roadwarnings.narino.service.TrafficService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador para información de tráfico
 */
@RestController
@RequestMapping("/api/traffic")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TrafficController {

    private final TrafficService trafficService;

    /**
     * Obtiene las condiciones de tráfico para una ubicación
     * GET /api/traffic/conditions?lat={lat}&lon={lon}
     */
    @GetMapping("/conditions")
    public ResponseEntity<TrafficService.TrafficConditionDTO> getTrafficConditions(
            @RequestParam Double lat,
            @RequestParam Double lon) {

        TrafficService.TrafficConditionDTO conditions =
                trafficService.getTrafficConditions(lat, lon);

        if (conditions == null) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(conditions);
    }

    /**
     * Obtiene las condiciones de tráfico para una ruta
     * GET /api/traffic/route/{routeId}
     */
    @GetMapping("/route/{routeId}")
    public ResponseEntity<TrafficService.RouteTrafficDTO> getRouteTraffic(
            @PathVariable Long routeId) {

        TrafficService.RouteTrafficDTO routeTraffic =
                trafficService.getRouteTraffic(routeId);

        if (routeTraffic == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(routeTraffic);
    }
}
