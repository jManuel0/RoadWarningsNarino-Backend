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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AlertService {

    private final AlertRepository alertRepository;
    private final UserRepository userRepository;

    private static final String ALERT_NOT_FOUND = "Alerta no encontrada";
    private static final String USER_NOT_FOUND = "Usuario no encontrado";

    private final ObjectMapper objectMapper = new ObjectMapper();

    public AlertaResponseDTO createAlert(AlertaRequestDTO request, String username) {
        log.info("Creando alerta: {} por usuario: {}", request.getTitle(), username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException(USER_NOT_FOUND));

        // Determinar coordenadas
        Double lat = request.getLatitude();
        Double lon = request.getLongitude();

        if ((lat == null || lon == null) &&
                request.getLocation() != null &&
                !request.getLocation().isBlank()) {

            double[] coords = geocodeAddress(request.getLocation());
            if (coords != null) {
                lat = coords[0];
                lon = coords[1];
                log.info("Geocodificada ubicación '{}' -> {}, {}", request.getLocation(), lat, lon);
            } else {
                log.warn("No se pudo geocodificar '{}', se usará 0,0", request.getLocation());
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
                .user(user)
                .build();

        alert = alertRepository.save(alert);
        log.info("Alerta creada con ID: {}", alert.getId());

        return mapToResponseDTO(alert);
    }

    public List<AlertaResponseDTO> getAllAlerts() {
        return alertRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    public List<AlertaResponseDTO> getActiveAlerts() {
        return alertRepository.findByStatus(AlertStatus.ACTIVE).stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    public AlertaResponseDTO getAlertById(Long id) {
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(ALERT_NOT_FOUND));
        return mapToResponseDTO(alert);
    }

    public List<AlertaResponseDTO> getNearbyAlerts(Double latitude, Double longitude, Double radius) {
        return alertRepository.findAll().stream()
                .filter(alert -> calculateDistance(
                        latitude, longitude,
                        alert.getLatitude(), alert.getLongitude()
                ) <= radius)
                .map(this::mapToResponseDTO)
                .toList();
    }

    public AlertaResponseDTO updateAlert(Long id, AlertaRequestDTO request, String username) {
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(ALERT_NOT_FOUND));

        if (!alert.getUser().getUsername().equals(username)) {
            throw new UnauthorizedException("No tienes permiso para actualizar esta alerta");
        }

        alert.setType(request.getType());
        alert.setTitle(request.getTitle());
        alert.setDescription(request.getDescription());
        alert.setLocation(request.getLocation());
        alert.setSeverity(request.getSeverity());

        // Actualizar coordenadas
        Double lat = request.getLatitude();
        Double lon = request.getLongitude();

        if ((lat == null || lon == null) &&
                request.getLocation() != null &&
                !request.getLocation().isBlank()) {

            double[] coords = geocodeAddress(request.getLocation());
            if (coords != null) {
                lat = coords[0];
                lon = coords[1];
                log.info("Geocodificada ubicación (update) '{}' -> {}, {}", request.getLocation(), lat, lon);
            } else {
                log.warn("No se pudo geocodificar en update '{}', se mantienen coords anteriores", request.getLocation());
                lat = alert.getLatitude();
                lon = alert.getLongitude();
            }
        }

        if (lat != null) alert.setLatitude(lat);
        if (lon != null) alert.setLongitude(lon);

        alert = alertRepository.save(alert);
        return mapToResponseDTO(alert);
    }

    public void deleteAlert(Long id, String username) {
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(ALERT_NOT_FOUND));

        if (!alert.getUser().getUsername().equals(username)) {
            throw new UnauthorizedException("No tienes permiso para eliminar esta alerta");
        }

        alertRepository.delete(alert);
    }

    public AlertaResponseDTO updateAlertStatus(Long id, AlertStatus status) {
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(ALERT_NOT_FOUND));

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
     * Devuelve [lat, lon] o null si falla.
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
            headers.add("User-Agent", "roadwarnings-narino/1.0"); // requerido por Nominatim
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
