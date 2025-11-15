package com.roadwarnings.narino.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.roadwarnings.narino.dto.request.AlertaRequestDTO;
import com.roadwarnings.narino.dto.request.AlertFilterDTO;
import com.roadwarnings.narino.dto.response.AlertaResponseDTO;
import com.roadwarnings.narino.entity.Alert;
import com.roadwarnings.narino.entity.FavoriteRoute;
import com.roadwarnings.narino.entity.User;
import com.roadwarnings.narino.enums.AlertStatus;
import com.roadwarnings.narino.exception.UnauthorizedException;
import com.roadwarnings.narino.repository.AlertRepository;
import com.roadwarnings.narino.repository.UserRepository;
import com.roadwarnings.narino.repository.RouteRepository;
import com.roadwarnings.narino.repository.FavoriteRouteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
    private final RouteRepository routeRepository;
    private final FavoriteRouteRepository favoriteRouteRepository;
    private final ReputationService reputationService;
    private final SmartNotificationService smartNotificationService;
    private final PushNotificationService pushNotificationService;

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

            // Otorgar puntos de reputación
            reputationService.onAlertCreated(user.getId());
        }

        // Enviar notificaciones inteligentes a usuarios con rutas favoritas
        smartNotificationService.onNewAlert(alert);

        // Actualizar contador de alertas en rutas cercanas
        updateNearbyRoutesAlertCount(alert.getLatitude(), alert.getLongitude());

        // Notificar a usuarios con rutas favoritas cercanas
        notifyUsersWithNearbyFavoriteRoutes(alert);

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

            // Otorgar puntos de reputación
            reputationService.onAlertUpvoted(alert.getUser().getId());
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

            // Restar puntos de reputación
            reputationService.onAlertDownvoted(alert.getUser().getId());
        }

        // Broadcast actualización de votos a través de WebSocket
        webSocketService.broadcastAlertVoteUpdate(id, alert.getUpvotes(), alert.getDownvotes());

        return mapToResponseDTO(alert);
    }

    /**
     * Filtra alertas según criterios avanzados
     */
    public Page<AlertaResponseDTO> filterAlerts(AlertFilterDTO filter, Pageable pageable) {
        List<Alert> alerts = alertRepository.findAll();

        // Aplicar filtros
        List<Alert> filteredAlerts = alerts.stream()
                .filter(alert -> matchesFilter(alert, filter))
                .collect(Collectors.toList());

        // Paginación manual
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filteredAlerts.size());

        List<AlertaResponseDTO> pageContent = filteredAlerts.subList(start, end).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());

        return new PageImpl<>(pageContent, pageable, filteredAlerts.size());
    }

    /**
     * Obtiene alertas de un usuario específico
     */
    public Page<AlertaResponseDTO> getAlertsByUser(Long userId, Pageable pageable) {
        List<Alert> alerts = alertRepository.findAll().stream()
                .filter(alert -> alert.getUser() != null && alert.getUser().getId().equals(userId))
                .collect(Collectors.toList());

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), alerts.size());

        List<AlertaResponseDTO> pageContent = alerts.subList(start, end).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());

        return new PageImpl<>(pageContent, pageable, alerts.size());
    }

    /**
     * Obtiene alertas del usuario autenticado
     */
    public Page<AlertaResponseDTO> getMyAlerts(String username, Pageable pageable) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException(USER_NOT_FOUND));

        return getAlertsByUser(user.getId(), pageable);
    }

    /**
     * Expira manualmente una alerta
     */
    public AlertaResponseDTO expireAlert(Long id, String username) {
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(ALERT_NOT_FOUND));

        validateOwnership(alert, username);

        alert.setStatus(AlertStatus.EXPIRED);
        alert.setExpiresAt(LocalDateTime.now());
        alert = alertRepository.save(alert);

        // Broadcast cambio de estado a través de WebSocket
        AlertaResponseDTO response = mapToResponseDTO(alert);
        webSocketService.broadcastAlertStatusChange(response);

        log.info("Alerta {} expirada manualmente por {}", id, username);

        return response;
    }

    /**
     * Verifica si una alerta cumple con los criterios de filtro
     */
    private boolean matchesFilter(Alert alert, AlertFilterDTO filter) {
        // Filtro por tipo
        if (filter.getType() != null && !alert.getType().equals(filter.getType())) {
            return false;
        }

        // Filtro por severidad
        if (filter.getSeverity() != null && !alert.getSeverity().equals(filter.getSeverity())) {
            return false;
        }

        // Filtro por estado
        if (filter.getStatus() != null && !alert.getStatus().equals(filter.getStatus())) {
            return false;
        }

        // Filtro por ubicación (radio)
        if (filter.getLatitude() != null && filter.getLongitude() != null && filter.getRadiusKm() != null) {
            double distance = calculateDistance(
                    filter.getLatitude(), filter.getLongitude(),
                    alert.getLatitude(), alert.getLongitude()
            );
            if (distance > filter.getRadiusKm()) {
                return false;
            }
        }

        // Filtro por fecha de creación (desde)
        if (filter.getCreatedAfter() != null && alert.getCreatedAt().isBefore(filter.getCreatedAfter())) {
            return false;
        }

        // Filtro por fecha de creación (hasta)
        if (filter.getCreatedBefore() != null && alert.getCreatedAt().isAfter(filter.getCreatedBefore())) {
            return false;
        }

        // Filtro por upvotes mínimos
        if (filter.getMinUpvotes() != null && alert.getUpvotes() < filter.getMinUpvotes()) {
            return false;
        }

        // Filtro por usuario
        if (filter.getUserId() != null && (alert.getUser() == null || !alert.getUser().getId().equals(filter.getUserId()))) {
                return false;
            }
        

        return true;
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

    /**
     * Notifica a usuarios que tienen rutas favoritas cerca de una alerta
     */
    private void notifyUsersWithNearbyFavoriteRoutes(Alert alert) {
        if (alert.getLatitude() == null || alert.getLongitude() == null) {
            return;
        }

        double radiusKm = 10.0;

        // Obtener todas las rutas favoritas con notificaciones habilitadas
        favoriteRouteRepository.findAll().stream()
            .filter(FavoriteRoute::getNotificationsEnabled)
            .forEach(favRoute -> {
                // Calcular distancia desde la alerta al punto medio de la ruta
                double routeMidLat = (favRoute.getRoute().getOriginLatitude() +
                                     favRoute.getRoute().getDestinationLatitude()) / 2;
                double routeMidLon = (favRoute.getRoute().getOriginLongitude() +
                                     favRoute.getRoute().getDestinationLongitude()) / 2;

                double distance = calculateDistance(
                    alert.getLatitude(), alert.getLongitude(),
                    routeMidLat, routeMidLon
                );

                if (distance <= radiusKm) {
                    // Enviar notificación push al usuario
                    String title = "⚠️ Nueva alerta en tu ruta";
                    String message = String.format("%s cerca de %s",
                        alert.getTitle(),
                        favRoute.getRoute().getName());

                    java.util.Map<String, String> data = new java.util.HashMap<>();
                    data.put("alertId", alert.getId().toString());
                    data.put("routeId", favRoute.getRoute().getId().toString());
                    data.put("type", "ALERT_NEARBY_ROUTE");

                    pushNotificationService.sendNotificationToUser(
                        favRoute.getUser().getId(),
                        title,
                        message,
                        data
                    );

                    log.info("Notificación enviada a usuario {} por alerta en ruta {}",
                        favRoute.getUser().getUsername(),
                        favRoute.getRoute().getName());
                }
            });
    }

    /**
     * Actualiza el contador de alertas activas en rutas cercanas a una ubicación
     */
    private void updateNearbyRoutesAlertCount(Double latitude, Double longitude) {
        if (latitude == null || longitude == null) {
            return;
        }

        // Buscar rutas dentro de un radio de 10km
        double radiusKm = 10.0;

        routeRepository.findAll().forEach(route -> {
            // Calcular distancia desde la alerta a la ruta (usando punto medio de origen-destino)
            double routeMidLat = (route.getOriginLatitude() + route.getDestinationLatitude()) / 2;
            double routeMidLon = (route.getOriginLongitude() + route.getDestinationLongitude()) / 2;

            double distance = calculateDistance(latitude, longitude, routeMidLat, routeMidLon);

            if (distance <= radiusKm) {
                // Contar alertas activas cerca de esta ruta
                long activeAlertsCount = alertRepository.findByStatus(AlertStatus.ACTIVE).stream()
                    .filter(alert -> {
                        double distToRoute = calculateDistance(
                            alert.getLatitude(), alert.getLongitude(),
                            routeMidLat, routeMidLon
                        );
                        return distToRoute <= radiusKm;
                    })
                    .count();

                route.setActiveAlertsCount((int) activeAlertsCount);
                routeRepository.save(route);

                log.debug("Ruta {} actualizada con {} alertas activas", route.getId(), activeAlertsCount);
            }
        });
    }
}
