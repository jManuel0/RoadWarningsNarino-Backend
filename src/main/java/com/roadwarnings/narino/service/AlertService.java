package com.roadwarnings.narino.service;

import com.roadwarnings.narino.dto.request.AlertaRequestDTO;
import com.roadwarnings.narino.dto.response.AlertaResponseDTO;
import com.roadwarnings.narino.entity.Alert;
import com.roadwarnings.narino.entity.User;
import com.roadwarnings.narino.enums.AlertStatus;
import com.roadwarnings.narino.enums.AlertSeverity;
import com.roadwarnings.narino.repository.AlertRepository;
import com.roadwarnings.narino.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AlertService {

    private final AlertRepository alertRepository;
    private final UserRepository userRepository;

    public AlertaResponseDTO createAlert(AlertaRequestDTO request, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Alert alert = Alert.builder()
                .type(request.getType())
                .title(request.getTitle())
                .description(request.getDescription())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .location(request.getLocation())
                .severity(request.getSeverity() != null ? request.getSeverity() : AlertSeverity.MEDIUM)
                .status(AlertStatus.ACTIVE)
                .user(user)
                .imageUrl(request.getImageUrl())
                .upvotes(0)
                .downvotes(0)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();

        Alert savedAlert = alertRepository.save(alert);
        return mapToResponseDTO(savedAlert);
    }

    @Transactional(readOnly = true)
    public List<AlertaResponseDTO> getAllAlerts() {
        return alertRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AlertaResponseDTO> getActiveAlerts() {
        return alertRepository.findByStatus(AlertStatus.ACTIVE).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AlertaResponseDTO getAlertById(Long id) {
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Alerta no encontrada"));
        return mapToResponseDTO(alert);
    }

    @Transactional(readOnly = true)
    public List<AlertaResponseDTO> getNearbyAlerts(Double latitude, Double longitude, Double radiusKm) {
        return alertRepository.findNearbyAlerts(latitude, longitude, radiusKm).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    public AlertaResponseDTO updateAlert(Long id, AlertaRequestDTO request, String username) {
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Alerta no encontrada"));

        if (!alert.getUser().getUsername().equals(username)) {
            throw new RuntimeException("No tienes permisos para actualizar esta alerta");
        }

        if (request.getTitle() != null) alert.setTitle(request.getTitle());
        if (request.getDescription() != null) alert.setDescription(request.getDescription());
        if (request.getSeverity() != null) alert.setSeverity(request.getSeverity());
        if (request.getImageUrl() != null) alert.setImageUrl(request.getImageUrl());

        Alert updatedAlert = alertRepository.save(alert);
        return mapToResponseDTO(updatedAlert);
    }

    public void deleteAlert(Long id, String username) {
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Alerta no encontrada"));

        if (!alert.getUser().getUsername().equals(username)) {
            throw new RuntimeException("No tienes permisos para eliminar esta alerta");
        }

        alertRepository.delete(alert);
    }

    public AlertaResponseDTO updateAlertStatus(Long id, AlertStatus status) {
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Alerta no encontrada"));
        
        alert.setStatus(status);
        Alert updatedAlert = alertRepository.save(alert);
        return mapToResponseDTO(updatedAlert);
    }

    private AlertaResponseDTO mapToResponseDTO(Alert alert) {
        return AlertaResponseDTO.builder()
                .id(alert.getId())
                .type(alert.getType())
                .title(alert.getTitle())
                .description(alert.getDescription())
                .latitude(alert.getLatitude())
                .longitude(alert.getLongitude())
                .location(alert.getLocation())
                .severity(alert.getSeverity())
                .status(alert.getStatus())
                .username(alert.getUser().getUsername())
                .userId(alert.getUser().getId())
                .imageUrl(alert.getImageUrl())
                .upvotes(alert.getUpvotes())
                .downvotes(alert.getDownvotes())
                .createdAt(alert.getCreatedAt())
                .updatedAt(alert.getUpdatedAt())
                .expiresAt(alert.getExpiresAt())
                .build();
    }
}
