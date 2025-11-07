package com.roadwarnings.narino.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/")
public class RootController {
    
    @GetMapping
    public ResponseEntity<Map<String, Object>> root() {
        Map<String, Object> response = new HashMap<>();
        response.put("application", "RoadWarnings Nariño");
        response.put("version", "1.0.0");
        response.put("status", "Running");
        response.put("endpoints", Map.of(
            "health", "/api/public/health",
            "alertas", "/api/alertas",
            "swagger", "/api/swagger-ui.html",
            "h2-console", "/api/h2-console"
        ));
        return ResponseEntity.ok(response);
    }
}