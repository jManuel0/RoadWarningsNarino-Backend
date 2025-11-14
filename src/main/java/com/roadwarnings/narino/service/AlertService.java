package com.roadwarnings.narino.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.roadwarnings.narino.dto.request.AlertaRequestDTO;
import com.roadwarnings.narino.dto.response.AlertaResponseDTO;
import com.roadwarnings.narino.entity.Alert;
import com.roadwarnings.narino.entity.User;
import com.roadwarnings.narino.enums.AlertStatus;
import com.roadwarnings.narino.exception.UnauthorizedException;
import com.roadwarnings.narino.repository.AlertRepository;
import com.roadwarnings.narino.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AlertService {

    private final AlertRepository alertRepository;
    private final UserRepository userRepository;
    private final WebSocketService webSocketService;
    private final UserStatisticsService statisticsService;
    private final BadgeService badgeService;

    private static final String ALERT_NOT_FOUND = "Alerta no encontrada";
    private static final String USER_NOT_FOUND = "Usuario no encontrado";

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Crear alerta.
     * username puede ser null/"public" → se guarda sin usuario asociado.
     */
    public AlertaResponseDTO createAlert(AlertaRequestDTO request, String username) {
        log.info("Creando alerta: {} por usuario: {}", request.getTitle(), username);

        User user = resolveUser(username);

        Double lat = request.getLatitude();
        Double lon = request.getLongitude();

        // Si llega sin lat/lon pero con dirección, intentamos geocodificar (defensivo)
        if ((lat == null || lon == null)
                && request.getLocation() != null
                && !request.getLocation().isBlank()) {

            double[] coords = geocodeAddress(request.getLocation());
            if (coords.length == 2) {
                lat = coords[0];
                lon = coords[1];
                log.info("Geocodificada ubicación '{}' -> {}, {}", request.getLocation(), lat, lon);
            } else {
                log.warn("No se pudo geocodificar '{}', usando 0,0", request.getLocation());
                lat = 0.0;
                lon = 0.0;
            }
        }

        Alert alert = Alert.builder()
                .type(request.getType())
                .title(request.getTitle())
                .description(request.getDescription())
                .latitude(lat)
                .longitude(lon)
                .location(request.getLocation())
                .severity(request.getSeverity())
                .status(AlertStatus.ACTIVE)
                .imageUrl(request.getImageUrl())
                .user(user)
                .build();

        alert = alertRepository.save(alert);
        log.info("Alerta creada con ID: {}", alert.getId());

        // Actualizar estadísticas del usuario
        if (user != null) {
            statisticsService.incrementAlertCreated(user.getId());
            badgeService.checkAndAwardBadges(user.getId());
        }

        // Broadcast a través de WebSocket
        AlertaResponseDTO response = mapToResponseDTO(alert);
        webSocketService.broadcastNewAlert(response);

        return response;
    }

    public List<AlertaResponseDTO> getAllAlerts() {
        return alertRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    public Page<AlertaResponseDTO> getAllAlertsPaginated(Pageable pageable) {
        return alertRepository.findAll(pageable)
                .map(this::mapToResponseDTO);
    }

    public List<AlertaResponseDTO> getActiveAlerts() {
        return alertRepository.findByStatus(AlertStatus.ACTIVE).stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    public Page<AlertaResponseDTO> getActiveAlertsPaginated(Pageable pageable) {
        return alertRepository.findByStatus(AlertStatus.ACTIVE, pageable)
                .map(this::mapToResponseDTO);
    }

    public AlertaResponseDTO getAlertById(Long id) {
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(ALERT_NOT_FOUND));
        return mapToResponseDTO(alert);
    }

    public List<AlertaResponseDTO> getNearbyAlerts(Double latitude, Double longitude, Double radiusKm) {
        return alertRepository.findAll().stream()
                .filter(alert -> calculateDistance(
                        latitude, longitude,
                        alert.getLatitude(), alert.getLongitude()
                ) <= radiusKm)
                .map(this::mapToResponseDTO)
                .toList();
    }

    public AlertaResponseDTO updateAlert(Long id, AlertaRequestDTO request, String username) {
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(ALERT_NOT_FOUND));

        validateOwnership(alert, username);

        alert.setType(request.getType());
        alert.setTitle(request.getTitle());
        alert.setDescription(request.getDescription());
        alert.setLocation(request.getLocation());
        alert.setSeverity(request.getSeverity());
        alert.setImageUrl(request.getImageUrl());

        Double lat = request.getLatitude();
        Double lon = request.getLongitude();

        if ((lat == null || lon == null)
                && request.getLocation() != null
                && !request.getLocation().isBlank()) {

            double[] coords = geocodeAddress(request.getLocation());
            if (coords.length == 2) {
                lat = coords[0];
                lon = coords[1];
                log.info("Geocodificada ubicación (update) '{}' -> {}, {}",
                        request.getLocation(), lat, lon);
            } else {
                log.warn("No se pudo geocodificar en update '{}', se mantienen coords anteriores",
                        request.getLocation());
                lat = alert.getLatitude();
                lon = alert.getLongitude();
            }
        }

        if (lat != null) alert.setLatitude(lat);
        if (lon != null) alert.setLongitude(lon);

        alert = alertRepository.save(alert);

        // Broadcast actualización a través de WebSocket
        AlertaResponseDTO response = mapToResponseDTO(alert);
        webSocketService.broadcastAlertUpdate(response);

        return response;
    }

    public void deleteAlert(Long id, String username) {
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(ALERT_NOT_FOUND));

        validateOwnership(alert, username);

        alertRepository.delete(alert);

        // Broadcast eliminación a través de WebSocket
        webSocketService.broadcastAlertDeletion(id);
    }

    public AlertaResponseDTO updateAlertStatus(Long id, AlertStatus status) {
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(ALERT_NOT_FOUND));

        alert.setStatus(status);
        alert = alertRepository.save(alert);

        // Broadcast cambio de estado a través de WebSocket
        AlertaResponseDTO response = mapToResponseDTO(alert);
        webSocketService.broadcastAlertStatusChange(response);

        return response;
    }

    public AlertaResponseDTO upvoteAlert(Long id) {
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(ALERT_NOT_FOUND));

        alert.setUpvotes(alert.getUpvotes() + 1);
        alert = alertRepository.save(alert);

        log.info("Alerta {} recibió upvote. Total: {}", id, alert.getUpvotes());

        // Actualizar estadísticas del creador de la alerta
        if (alert.getUser() != null) {
            statisticsService.incrementUpvoteReceived(alert.getUser().getId());
            badgeService.checkAndAwardBadges(alert.getUser().getId());
        }

        // Broadcast actualización de votos a través de WebSocket
        webSocketService.broadcastAlertVoteUpdate(id, alert.getUpvotes(), alert.getDownvotes());

        return mapToResponseDTO(alert);
    }

    public AlertaResponseDTO downvoteAlert(Long id) {
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(ALERT_NOT_FOUND));

        alert.setDownvotes(alert.getDownvotes() + 1);
        alert = alertRepository.save(alert);

        log.info("Alerta {} recibió downvote. Total: {}", id, alert.getDownvotes());

        // Actualizar estadísticas del creador de la alerta
        if (alert.getUser() != null) {
            statisticsService.incrementDownvoteReceived(alert.getUser().getId());
        }

        // Broadcast actualización de votos a través de WebSocket
        webSocketService.broadcastAlertVoteUpdate(id, alert.getUpvotes(), alert.getDownvotes());

        return mapToResponseDTO(alert);
    }

    // ==== Helpers ====

    private void validateOwnership(Alert alert, String username) {
        if (alert.getUser() != null
                && username != null
                && !username.isBlank()
                && !"public".equalsIgnoreCase(username)
                && !alert.getUser().getUsername().equals(username)) {
            throw new UnauthorizedException("No tienes permiso para modificar esta alerta");
        }
    }

    private User resolveUser(String username) {
        if (username == null || username.isBlank() || "public".equalsIgnoreCase(username)) {
            return null;
        }
        Optional<User> existing = userRepository.findByUsername(username);
        return existing.orElseThrow(() -> new RuntimeException(USER_NOT_FOUND));
    }

    private AlertaResponseDTO mapToResponseDTO(Alert alert) {
        User user = alert.getUser();

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
                .userId(user != null ? user.getId() : null)
                .username(user != null ? user.getUsername() : null)
                .build();
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int EARTH_RADIUS = 6371; // km

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }

    /**
     * Geocodifica una dirección usando Nominatim (OpenStreetMap).
     * Devuelve [lat, lon] o array vacío si falla.
     */
    private double[] geocodeAddress(String address) {
        try {
            String url = UriComponentsBuilder
                    .fromHttpUrl("https://nominatim.openstreetmap.org/search")
                    .queryParam("format", "json")
                    .queryParam("limit", 1)
                    .queryParam("q", address)
                    .toUriString();

            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "roadwarnings-narino/1.0");

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                log.warn("Nominatim respondió con código no exitoso: {}", response.getStatusCode());
                return new double[0];
            }

            JsonNode root = objectMapper.readTree(response.getBody());
            if (root.isArray() && root.size() > 0) {
                JsonNode first = root.get(0);
                double lat = first.get("lat").asDouble();
                double lon = first.get("lon").asDouble();
                return new double[]{lat, lon};
            }

            log.warn("Nominatim no encontró resultados para '{}'", address);
            return new double[0];

        } catch (Exception e) {
            log.error("Error geocodificando dirección '{}': {}", address, e.getMessage());
            return new double[0];
        }
    }
}
