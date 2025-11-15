package com.roadwarnings.narino.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio para integración con OpenWeatherMap API
 * Proporciona información meteorológica para alertas y rutas
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WeatherService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${weather.api.key:#{null}}")
    private String apiKey;

    @Value("${weather.api.url:https://api.openweathermap.org/data/2.5}")
    private String apiUrl;

    private static final int CACHE_TTL_MINUTES = 30; // Cachear por 30 minutos

    /**
     * Obtiene el clima actual para una ubicación
     */
    @Cacheable(value = "weather", key = "#latitude + '_' + #longitude", unless = "#result == null")
    public WeatherDataDTO getCurrentWeather(Double latitude, Double longitude) {
        if (apiKey == null || apiKey.isEmpty()) {
            log.warn("API Key de clima no configurada");
            return null;
        }

        try {
            String url = UriComponentsBuilder.fromHttpUrl(apiUrl + "/weather")
                    .queryParam("lat", latitude)
                    .queryParam("lon", longitude)
                    .queryParam("appid", apiKey)
                    .queryParam("units", "metric")
                    .queryParam("lang", "es")
                    .toUriString();

            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return parseWeatherResponse(response.getBody());
            }

            log.error("Error al obtener clima: {}", response.getStatusCode());
            return null;

        } catch (Exception e) {
            log.error("Error al consultar API de clima: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Obtiene pronóstico del clima para los próximos días
     */
    @Cacheable(value = "weather-forecast", key = "#latitude + '_' + #longitude", unless = "#result == null")
    public List<WeatherForecastDTO> getWeatherForecast(Double latitude, Double longitude, Integer days) {
        if (apiKey == null || apiKey.isEmpty()) {
            log.warn("API Key de clima no configurada");
            return new ArrayList<>();
        }

        try {
            String url = UriComponentsBuilder.fromHttpUrl(apiUrl + "/forecast")
                    .queryParam("lat", latitude)
                    .queryParam("lon", longitude)
                    .queryParam("appid", apiKey)
                    .queryParam("units", "metric")
                    .queryParam("lang", "es")
                    .queryParam("cnt", days != null ? days * 8 : 24) // 8 intervalos por día (cada 3 horas)
                    .toUriString();

            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return parseForecastResponse(response.getBody());
            }

            log.error("Error al obtener pronóstico: {}", response.getStatusCode());
            return new ArrayList<>();

        } catch (Exception e) {
            log.error("Error al consultar pronóstico: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Verifica si las condiciones climáticas son peligrosas para conducir
     */
    public WeatherAlertDTO checkWeatherHazards(Double latitude, Double longitude) {
        WeatherDataDTO weather = getCurrentWeather(latitude, longitude);

        if (weather == null) {
            return null;
        }

        List<String> hazards = new ArrayList<>();
        String severity = "LOW";

        // Lluvia intensa
        if (weather.getRainfall1h() != null && weather.getRainfall1h() > 10) {
            hazards.add("Lluvia intensa - Reducir velocidad");
            severity = "HIGH";
        } else if (weather.getRainfall1h() != null && weather.getRainfall1h() > 5) {
            hazards.add("Lluvia moderada - Conducir con precaución");
            severity = "MEDIUM";
        }

        // Niebla o baja visibilidad
        if (weather.getVisibility() != null && weather.getVisibility() < 1000) {
            hazards.add("Visibilidad reducida - Usar luces");
            severity = "HIGH";
        }

        // Vientos fuertes
        if (weather.getWindSpeed() != null && weather.getWindSpeed() > 50) {
            hazards.add("Vientos fuertes - Precaución con vehículos altos");
            severity = "HIGH";
        } else if (weather.getWindSpeed() != null && weather.getWindSpeed() > 30) {
            hazards.add("Vientos moderados");
            if ("LOW".equals(severity)) severity = "MEDIUM";
        }

        // Tormenta eléctrica
        if (weather.getCondition() != null &&
            (weather.getCondition().toLowerCase().contains("tormenta") ||
             weather.getCondition().toLowerCase().contains("thunderstorm"))) {
            hazards.add("Tormenta eléctrica - Evitar viajar si es posible");
            severity = "HIGH";
        }

        // Nieve o granizo
        if (weather.getSnowfall1h() != null && weather.getSnowfall1h() > 0) {
            hazards.add("Nevadas - Condiciones peligrosas");
            severity = "HIGH";
        }

        return WeatherAlertDTO.builder()
                .hasHazards(!hazards.isEmpty())
                .severity(severity)
                .hazards(hazards)
                .temperature(weather.getTemperature())
                .condition(weather.getCondition())
                .build();
    }

    private WeatherDataDTO parseWeatherResponse(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode main = root.path("main");
            JsonNode weather = root.path("weather").get(0);
            JsonNode wind = root.path("wind");
            JsonNode rain = root.path("rain");
            JsonNode snow = root.path("snow");

            return WeatherDataDTO.builder()
                    .temperature(main.path("temp").asDouble())
                    .feelsLike(main.path("feels_like").asDouble())
                    .humidity(main.path("humidity").asInt())
                    .pressure(main.path("pressure").asInt())
                    .condition(weather.path("description").asText())
                    .icon(weather.path("icon").asText())
                    .windSpeed(wind.path("speed").asDouble())
                    .windDirection(wind.path("deg").asInt())
                    .visibility(root.path("visibility").asInt())
                    .rainfall1h(rain.has("1h") ? rain.path("1h").asDouble() : null)
                    .snowfall1h(snow.has("1h") ? snow.path("1h").asDouble() : null)
                    .timestamp(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("Error parseando respuesta de clima: {}", e.getMessage());
            return null;
        }
    }

    private List<WeatherForecastDTO> parseForecastResponse(String json) {
        List<WeatherForecastDTO> forecasts = new ArrayList<>();

        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode list = root.path("list");

            for (JsonNode item : list) {
                JsonNode main = item.path("main");
                JsonNode weather = item.path("weather").get(0);
                JsonNode rain = item.path("rain");

                WeatherForecastDTO forecast = WeatherForecastDTO.builder()
                        .timestamp(item.path("dt_txt").asText())
                        .temperature(main.path("temp").asDouble())
                        .condition(weather.path("description").asText())
                        .icon(weather.path("icon").asText())
                        .rainfall3h(rain.has("3h") ? rain.path("3h").asDouble() : 0.0)
                        .build();

                forecasts.add(forecast);
            }

        } catch (Exception e) {
            log.error("Error parseando pronóstico: {}", e.getMessage());
        }

        return forecasts;
    }

    // ==================== DTOs ====================

    @lombok.Data
    @lombok.Builder
    public static class WeatherDataDTO {
        private Double temperature;
        private Double feelsLike;
        private Integer humidity;
        private Integer pressure;
        private String condition;
        private String icon;
        private Double windSpeed;
        private Integer windDirection;
        private Integer visibility;
        private Double rainfall1h;
        private Double snowfall1h;
        private LocalDateTime timestamp;
    }

    @lombok.Data
    @lombok.Builder
    public static class WeatherForecastDTO {
        private String timestamp;
        private Double temperature;
        private String condition;
        private String icon;
        private Double rainfall3h;
    }

    @lombok.Data
    @lombok.Builder
    public static class WeatherAlertDTO {
        private Boolean hasHazards;
        private String severity; // LOW, MEDIUM, HIGH
        private List<String> hazards;
        private Double temperature;
        private String condition;
    }
}
