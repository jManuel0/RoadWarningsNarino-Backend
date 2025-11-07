package com.roadwarnings.narino.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/public")
@CrossOrigin(origins = "*")
public class PublicController {

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "RoadWarnings Nariño API");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/info")
    public ResponseEntity<Map<String, String>> getInfo() {
        Map<String, String> info = new HashMap<>();
        info.put("name", "RoadWarnings Nariño");
        info.put("version", "1.0.0");
        info.put("description", "Sistema de alertas viales para Nariño");
        return ResponseEntity.ok(info);
    }
}
