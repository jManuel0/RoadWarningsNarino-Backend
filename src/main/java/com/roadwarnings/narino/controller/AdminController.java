package com.roadwarnings.narino.controller;

import com.roadwarnings.narino.dto.response.UserResponseDTO;
import com.roadwarnings.narino.entity.User;
import com.roadwarnings.narino.enums.UserRole;
import com.roadwarnings.narino.repository.UserRepository;
import lombok.RequiredArgsConstructor;
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
public class AdminController {

    private final UserRepository userRepository;

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
                .build();

        return ResponseEntity.ok(stats);
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
    }
}
