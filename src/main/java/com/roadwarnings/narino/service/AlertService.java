package com.roadwarnings.narino.service;

import com.roadwarnings.narino.dto.request.AlertaRequestDTO;
import com.roadwarnings.narino.dto.response.AlertaResponseDTO;
import com.roadwarnings.narino.entity.Alert;
import com.roadwarnings.narino.entity.User;
import com.roadwarnings.narino.enums.AlertStatus;
import com.roadwarnings.narino.repository.AlertRepository;
import com.roadwarnings.narino.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AlertService {

    private final AlertRepository alertRepository;
    private final UserRepository userRepository;
    
    private static final String ALERT_NOT_FOUND = "Alerta no encontrada";
    private static final String USER_NOT_FOUND = "Usuario no encontrado";

    public AlertaResponseDTO createAlert(AlertaRequestDTO request, String username) {
        log.info("Creando alerta: {} por usuario: {}", request.getTitle(), username);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow();

        Alert alert = Alert.builder()
                .type(request.getType())
                .title(request.getTitle())
                .description(request.getDescription())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .location(request.getLocation())
                .severity(request.getSeverity())
                .user(user)
                .build();

        alert = alertRepository.save(alert);
        log.info("Alerta creada con ID: {}", alert.getId());
        
        return mapToResponseDTO(alert);
    }

    public List<AlertaResponseDTO> getAllAlerts() {
        return alertRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    public List<AlertaResponseDTO> getActiveAlerts() {
        return alertRepository.findByStatus(AlertStatus.ACTIVE).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    public AlertaResponseDTO getAlertById(Long id) {
        Alert alert = alertRepository.findById(id)
                .orElseThrow();
        return mapToResponseDTO(alert);
    }

    public List<AlertaResponseDTO> getNearbyAlerts(Double latitude, Double longitude, Double radius) {
        return alertRepository.findAll().stream()
                .filter(alert -> calculateDistance(latitude, longitude, alert.getLatitude(), alert.getLongitude()) <= radius)
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    public AlertaResponseDTO updateAlert(Long id, AlertaRequestDTO request, String username) {
        Alert alert = alertRepository.findById(id)
                .orElseThrow();

        if (!alert.getUser().getUsername().equals(username)) {
            throw new UnauthorizedException("No tienes permiso para actualizar esta alerta");
        }

        alert.setType(request.getType());
        alert.setTitle(request.getTitle());
        alert.setDescription(request.getDescription());
        alert.setLatitude(request.getLatitude());
        alert.setLongitude(request.getLongitude());
        alert.setLocation(request.getLocation());
        alert.setSeverity(request.getSeverity());

        alert = alertRepository.save(alert);
        return mapToResponseDTO(alert);
    }

    public void deleteAlert(Long id, String username) {
        Alert alert = alertRepository.findById(id)
                .orElseThrow();

        if (!alert.getUser().getUsername().equals(username)) {
            throw new UnauthorizedException("No tienes permiso para eliminar esta alerta");
        }

        alertRepository.delete(alert);
    }

    public AlertaResponseDTO updateAlertStatus(Long id, AlertStatus status) {
        Alert alert = alertRepository.findById(id)
                .orElseThrow();

        alert.setStatus(status);
        alert = alertRepository.save(alert);
        
        return mapToResponseDTO(alert);
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
                .imageUrl(alert.getImageUrl())
                .upvotes(alert.getUpvotes())
                .downvotes(alert.getDownvotes())
                .createdAt(alert.getCreatedAt())
                .updatedAt(alert.getUpdatedAt())
                .expiresAt(alert.getExpiresAt())
                .userId(alert.getUser().getId())
                .username(alert.getUser().getUsername())
                .build();
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int EARTH_RADIUS = 6371; // Radio de la Tierra en km

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return EARTH_RADIUS * c;
    }
}