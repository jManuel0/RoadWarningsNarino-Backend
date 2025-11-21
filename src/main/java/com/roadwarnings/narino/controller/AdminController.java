package com.roadwarnings.narino.controller;

import com.roadwarnings.narino.dto.response.UserResponseDTO;
import com.roadwarnings.narino.entity.User;
import com.roadwarnings.narino.enums.UserRole;
import com.roadwarnings.narino.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controlador para operaciones administrativas
 * Solo accesible por usuarios con rol ADMIN
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN')")
@Slf4j
public class AdminController {

    private final UserRepository userRepository;
    private final AlertRepository alertRepository;
    private final GasStationRepository gasStationRepository;
    private final RouteRepository routeRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    /**
     * Obtiene todos los usuarios del sistema (paginado)
     */
    @GetMapping("/users")
    public ResponseEntity<Page<UserResponseDTO>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<User> users = userRepository.findAll(pageable);

        Page<UserResponseDTO> userDTOs = users.map(this::convertToDTO);
        return ResponseEntity.ok(userDTOs);
    }

    /**
     * Obtiene un usuario por su ID
     */
    @GetMapping("/users/{userId}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + userId));

        return ResponseEntity.ok(convertToDTO(user));
    }

    /**
     * Cambia el rol de un usuario
     */
    @PatchMapping("/users/{userId}/role")
    public ResponseEntity<UserResponseDTO> changeUserRole(
            @PathVariable Long userId,
            @RequestParam UserRole role) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + userId));

        user.setRole(role);
        User updatedUser = userRepository.save(user);

        return ResponseEntity.ok(convertToDTO(updatedUser));
    }

    /**
     * Activa o desactiva un usuario
     */
    @PatchMapping("/users/{userId}/status")
    public ResponseEntity<UserResponseDTO> toggleUserStatus(
            @PathVariable Long userId,
            @RequestParam boolean active) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + userId));

        user.setIsActive(active);
        User updatedUser = userRepository.save(user);

        return ResponseEntity.ok(convertToDTO(updatedUser));
    }

    /**
     * Obtiene usuarios por rol
     */
    @GetMapping("/users/by-role/{role}")
    public ResponseEntity<List<UserResponseDTO>> getUsersByRole(@PathVariable UserRole role) {
        List<User> users = userRepository.findAll().stream()
                .filter(user -> user.getRole() == role)
                .collect(Collectors.toList());

        List<UserResponseDTO> userDTOs = users.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(userDTOs);
    }

    /**
     * Elimina un usuario (solo ADMIN)
     */
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("Usuario no encontrado con ID: " + userId);
        }

        userRepository.deleteById(userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Obtiene estadísticas generales del sistema
     */
    @GetMapping("/stats")
    public ResponseEntity<AdminStatsDTO> getSystemStats() {
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.findAll().stream()
                .filter(User::getIsActive)
                .count();
        long adminCount = userRepository.findAll().stream()
                .filter(user -> user.getRole() == UserRole.ADMIN)
                .count();
        long moderatorCount = userRepository.findAll().stream()
                .filter(user -> user.getRole() == UserRole.MODERATOR)
                .count();
        long regularUsers = userRepository.findAll().stream()
                .filter(user -> user.getRole() == UserRole.USER)
                .count();

        AdminStatsDTO stats = AdminStatsDTO.builder()
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .inactiveUsers(totalUsers - activeUsers)
                .adminCount(adminCount)
                .moderatorCount(moderatorCount)
                .regularUserCount(regularUsers)
                .totalAlerts(alertRepository.count())
                .totalGasStations(gasStationRepository.count())
                .totalRoutes(routeRepository.count())
                .build();

        return ResponseEntity.ok(stats);
    }

    /**
     * Limpia los datos de prueba de la base de datos
     * DELETE /api/admin/clear-test-data
     */
    @DeleteMapping("/clear-test-data")
    public ResponseEntity<java.util.Map<String, Object>> clearTestData() {
        log.warn("⚠️ Limpiando datos de prueba de la base de datos...");

        java.util.Map<String, Object> result = new java.util.HashMap<>();

        try {
            // Contar datos antes de eliminar
            long alertsCount = alertRepository.count();
            long gasStationsCount = gasStationRepository.count();
            long routesCount = routeRepository.count();

            // Eliminar todas las alertas
            alertRepository.deleteAll();
            log.info("🗑️ Eliminadas {} alertas", alertsCount);

            // Eliminar usuarios de prueba específicos
            userRepository.findByUsername("admin").ifPresent(userRepository::delete);
            userRepository.findByUsername("moderador").ifPresent(userRepository::delete);
            userRepository.findByUsername("juan_pasto").ifPresent(userRepository::delete);
            log.info("🗑️ Eliminados usuarios de prueba");

            // Eliminar estaciones de gasolina
            gasStationRepository.deleteAll();
            log.info("🗑️ Eliminadas {} estaciones de gasolina", gasStationsCount);

            // Eliminar rutas
            routeRepository.deleteAll();
            log.info("🗑️ Eliminadas {} rutas", routesCount);

            // Limpiar refresh tokens huérfanos
            refreshTokenRepository.deleteAll();
            log.info("🗑️ Limpiados refresh tokens");

            result.put("success", true);
            result.put("message", "Datos de prueba eliminados exitosamente");
            result.put("deleted", java.util.Map.of(
                "alerts", alertsCount,
                "users", "admin, moderador, juan_pasto",
                "gasStations", gasStationsCount,
                "routes", routesCount
            ));
            result.put("remaining", java.util.Map.of(
                "alerts", alertRepository.count(),
                "users", userRepository.count(),
                "gasStations", gasStationRepository.count(),
                "routes", routeRepository.count()
            ));

            log.info("✅ Limpieza completada exitosamente");
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("❌ Error al limpiar datos: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }

    /**
     * Elimina TODOS los datos de la base de datos (usar con precaución)
     * DELETE /api/admin/clear-all
     */
    @DeleteMapping("/clear-all")
    public ResponseEntity<java.util.Map<String, Object>> clearAllData() {
        log.warn("⚠️⚠️⚠️ LIMPIANDO TODA LA BASE DE DATOS...");

        java.util.Map<String, Object> result = new java.util.HashMap<>();

        try {
            long alertsCount = alertRepository.count();
            long usersCount = userRepository.count();
            long gasStationsCount = gasStationRepository.count();
            long routesCount = routeRepository.count();

            alertRepository.deleteAll();
            userRepository.deleteAll();
            gasStationRepository.deleteAll();
            routeRepository.deleteAll();
            refreshTokenRepository.deleteAll();

            result.put("success", true);
            result.put("message", "TODA la base de datos ha sido limpiada");
            result.put("deleted", java.util.Map.of(
                "alerts", alertsCount,
                "users", usersCount,
                "gasStations", gasStationsCount,
                "routes", routesCount
            ));

            log.info("✅ Base de datos completamente limpiada");
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("❌ Error al limpiar base de datos: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }

    // ==================== DTOs ====================

    private UserResponseDTO convertToDTO(User user) {
        return UserResponseDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .isActive(user.getIsActive())
                .preferredTheme(user.getPreferredTheme())
                .createdAt(user.getCreatedAt())
                .build();
    }

    @lombok.Data
    @lombok.Builder
    public static class AdminStatsDTO {
        private Long totalUsers;
        private Long activeUsers;
        private Long inactiveUsers;
        private Long adminCount;
        private Long moderatorCount;
        private Long regularUserCount;
        private Long totalAlerts;
        private Long totalGasStations;
        private Long totalRoutes;
    }
}
