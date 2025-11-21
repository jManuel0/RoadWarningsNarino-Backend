package com.roadwarnings.narino.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/db-fix")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DatabaseFixController {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Endpoint temporal para arreglar la columna user_id
     * Ejecutar una sola vez y luego eliminar este controlador
     */
    @PostMapping("/fix-user-id-nullable")
    public ResponseEntity<String> fixUserIdNullable() {
        try {
            jdbcTemplate.execute("ALTER TABLE alerts ALTER COLUMN user_id DROP NOT NULL");
            return ResponseEntity.ok("✅ Columna user_id ahora permite NULL");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("❌ Error: " + e.getMessage());
        }
    }
}
