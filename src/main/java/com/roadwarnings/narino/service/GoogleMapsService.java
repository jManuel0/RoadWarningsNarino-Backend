package com.roadwarnings.narino.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.roadwarnings.narino.dto.request.RouteCalculationRequestDTO;
import com.roadwarnings.narino.dto.response.PlaceDetailDTO;
import com.roadwarnings.narino.dto.response.PlaceSearchResultDTO;
import com.roadwarnings.narino.dto.response.RouteCalculationResponseDTO;
import com.roadwarnings.narino.entity.Alert;
import com.roadwarnings.narino.enums.AlertStatus;
import com.roadwarnings.narino.repository.AlertRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleMapsService {

    private final AlertRepository alertRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${google.maps.api.key:}")
    private String googleMapsApiKey;

    private static final String PLACES_API_URL = "https://maps.googleapis.com/maps/api/place/textsearch/json";
    private static final String PLACE_DETAILS_API_URL = "https://maps.googleapis.com/maps/api/place/details/json";
    private static final String DIRECTIONS_API_URL = "https://maps.googleapis.com/maps/api/directions/json";

    /**
     * Buscar lugares usando Google Places API
     */
    public List<PlaceSearchResultDTO> searchPlaces(String query, Double lat, Double lng, Integer radius) {
        if (googleMapsApiKey == null || googleMapsApiKey.isBlank()) {
            log.warn("Google Maps API key no configurada");
            return new ArrayList<>();
        }

        try {
            String url = UriComponentsBuilder.fromHttpUrl(PLACES_API_URL)
                    .queryParam("query", query)
                    .queryParam("location", lat + "," + lng)
                    .queryParam("radius", radius != null ? radius : 5000)
                    .queryParam("key", googleMapsApiKey)
                    .toUriString();

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                log.error("Error en Google Places API: {}", response.getStatusCode());
                return new ArrayList<>();
            }

            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode results = root.get("results");

            List<PlaceSearchResultDTO> places = new ArrayList<>();

            if (results != null && results.isArray()) {
                for (JsonNode place : results) {
                    JsonNode location = place.path("geometry").path("location");
                    double placeLat = location.path("lat").asDouble();
                    double placeLng = location.path("lng").asDouble();

                    PlaceSearchResultDTO dto = PlaceSearchResultDTO.builder()
                            .id(place.path("place_id").asText())
                            .name(place.path("name").asText())
                            .address(place.path("formatted_address").asText())
                            .lat(placeLat)
                            .lng(placeLng)
                            .type(place.path("types").get(0).asText(""))
                            .rating(place.has("rating") ? place.get("rating").asDouble() : null)
                            .totalReviews(place.has("user_ratings_total") ? place.get("user_ratings_total").asInt() : null)
                            .distance(calculateDistance(lat, lng, placeLat, placeLng))
                            .build();

                    places.add(dto);
                }
            }

            return places;

        } catch (Exception e) {
            log.error("Error buscando lugares: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Obtener detalles de un lugar
     */
    public PlaceDetailDTO getPlaceDetails(String placeId) {
        if (googleMapsApiKey == null || googleMapsApiKey.isBlank()) {
            throw new RuntimeException("Google Maps API key no configurada");
        }

        try {
            String url = UriComponentsBuilder.fromHttpUrl(PLACE_DETAILS_API_URL)
                    .queryParam("place_id", placeId)
                    .queryParam("fields", "name,formatted_address,geometry,rating,user_ratings_total,formatted_phone_number,website,opening_hours,photos,types")
                    .queryParam("key", googleMapsApiKey)
                    .toUriString();

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new RuntimeException("Error obteniendo detalles del lugar");
            }

            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode result = root.get("result");

            if (result == null) {
                throw new RuntimeException("Lugar no encontrado");
            }

            JsonNode location = result.path("geometry").path("location");
            List<String> photos = new ArrayList<>();

            if (result.has("photos")) {
                for (JsonNode photo : result.get("photos")) {
                    String photoReference = photo.path("photo_reference").asText();
                    photos.add("https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference=" + photoReference + "&key=" + googleMapsApiKey);
                }
            }

            return PlaceDetailDTO.builder()
                    .id(placeId)
                    .name(result.path("name").asText())
                    .address(result.path("formatted_address").asText())
                    .lat(location.path("lat").asDouble())
                    .lng(location.path("lng").asDouble())
                    .rating(result.has("rating") ? result.get("rating").asDouble() : null)
                    .totalReviews(result.has("user_ratings_total") ? result.get("user_ratings_total").asInt() : null)
                    .phone(result.has("formatted_phone_number") ? result.get("formatted_phone_number").asText() : null)
                    .website(result.has("website") ? result.get("website").asText() : null)
                    .hours(result.has("opening_hours") && result.get("opening_hours").has("weekday_text") 
                            ? result.get("opening_hours").get("weekday_text").toString() : null)
                    .photos(photos)
                    .category(result.has("types") ? result.get("types").get(0).asText() : null)
                    .build();

        } catch (Exception e) {
            log.error("Error obteniendo detalles del lugar: {}", e.getMessage());
            throw new RuntimeException("Error obteniendo detalles del lugar: " + e.getMessage());
        }
    }

    /**
     * Calcular rutas usando Google Directions API
     */
    public RouteCalculationResponseDTO calculateRoutes(RouteCalculationRequestDTO request) {
        if (googleMapsApiKey == null || googleMapsApiKey.isBlank()) {
            throw new RuntimeException("Google Maps API key no configurada");
        }

        try {
            String origin = request.getOrigin().getLat() + "," + request.getOrigin().getLng();
            String destination = request.getDestination().getLat() + "," + request.getDestination().getLng();

            String url = UriComponentsBuilder.fromHttpUrl(DIRECTIONS_API_URL)
                    .queryParam("origin", origin)
                    .queryParam("destination", destination)
                    .queryParam("alternatives", request.getAlternatives())
                    .queryParam("key", googleMapsApiKey)
                    .toUriString();

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new RuntimeException("Error calculando rutas");
            }

            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode routes = root.get("routes");

            if (routes == null || routes.isEmpty()) {
                throw new RuntimeException("No se encontraron rutas");
            }

            List<RouteCalculationResponseDTO.RouteOption> routeOptions = new ArrayList<>();

            for (int i = 0; i < routes.size(); i++) {
                JsonNode route = routes.get(i);
                JsonNode leg = route.get("legs").get(0);

                double distanceMeters = leg.path("distance").path("value").asDouble();
                double distanceKm = distanceMeters / 1000.0;
                int durationSeconds = leg.path("duration").path("value").asInt();
                int durationMinutes = durationSeconds / 60;

                String polyline = route.path("overview_polyline").path("points").asText();

                // Contar alertas en la ruta
                int alertCount = countAlertsInRoute(polyline);

                // Calcular nivel de tráfico basado en duración
                String traffic = calculateTrafficLevel(durationMinutes, distanceKm);

                // Extraer pasos
                List<RouteCalculationResponseDTO.RouteStep> steps = new ArrayList<>();
                JsonNode stepsNode = leg.get("steps");

                if (stepsNode != null && stepsNode.isArray()) {
                    for (JsonNode step : stepsNode) {
                        JsonNode startLocation = step.path("start_location");
                        double stepDistanceMeters = step.path("distance").path("value").asDouble();
                        int stepDurationSeconds = step.path("duration").path("value").asInt();

                        RouteCalculationResponseDTO.RouteStep routeStep = RouteCalculationResponseDTO.RouteStep.builder()
                                .instruction(step.path("html_instructions").asText().replaceAll("<[^>]*>", ""))
                                .distance(stepDistanceMeters / 1000.0)
                                .duration(stepDurationSeconds / 60)
                                .lat(startLocation.path("lat").asDouble())
                                .lng(startLocation.path("lng").asDouble())
                                .build();

                        steps.add(routeStep);
                    }
                }

                RouteCalculationResponseDTO.RouteOption routeOption = RouteCalculationResponseDTO.RouteOption.builder()
                        .id("route-" + (i + 1))
                        .name(route.path("summary").asText("Ruta " + (i + 1)))
                        .distance(distanceKm)
                        .duration(durationMinutes)
                        .traffic(traffic)
                        .alerts(alertCount)
                        .polyline(polyline)
                        .steps(steps)
                        .build();

                routeOptions.add(routeOption);
            }

            return RouteCalculationResponseDTO.builder()
                    .routes(routeOptions)
                    .build();

        } catch (Exception e) {
            log.error("Error calculando rutas: {}", e.getMessage());
            throw new RuntimeException("Error calculando rutas: " + e.getMessage());
        }
    }

    // ==== Helpers ====

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

    private int countAlertsInRoute(String polyline) {
        // Obtener alertas activas
        List<Alert> activeAlerts = alertRepository.findByStatus(AlertStatus.ACTIVE);

        // TODO: Implementar decodificación de polyline y verificar proximidad
        // Por ahora, retornamos un conteo simple basado en distancia
        return (int) activeAlerts.stream()
                .filter(alert -> alert.getLatitude() != null && alert.getLongitude() != null)
                .count();
    }

    private String calculateTrafficLevel(int durationMinutes, double distanceKm) {
        // Calcular velocidad promedio
        double avgSpeed = (distanceKm / durationMinutes) * 60; // km/h

        if (avgSpeed > 50) {
            return "low";
        } else if (avgSpeed > 30) {
            return "medium";
        } else {
            return "high";
        }
    }
}
