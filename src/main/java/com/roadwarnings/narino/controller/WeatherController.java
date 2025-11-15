package com.roadwarnings.narino.controller;

import com.roadwarnings.narino.service.WeatherService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador para información meteorológica
 */
@RestController
@RequestMapping("/api/weather")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class WeatherController {

    private final WeatherService weatherService;

    /**
     * Obtiene el clima actual para una ubicación
     * GET /api/weather/current?lat={lat}&lon={lon}
     */
    @GetMapping("/current")
    public ResponseEntity<WeatherService.WeatherDataDTO> getCurrentWeather(
            @RequestParam Double lat,
            @RequestParam Double lon) {

        WeatherService.WeatherDataDTO weather = weatherService.getCurrentWeather(lat, lon);

        if (weather == null) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(weather);
    }

    /**
     * Obtiene el pronóstico del clima para los próximos días
     * GET /api/weather/forecast?lat={lat}&lon={lon}&days={days}
     */
    @GetMapping("/forecast")
    public ResponseEntity<List<WeatherService.WeatherForecastDTO>> getWeatherForecast(
            @RequestParam Double lat,
            @RequestParam Double lon,
            @RequestParam(defaultValue = "3") Integer days) {

        List<WeatherService.WeatherForecastDTO> forecast =
                weatherService.getWeatherForecast(lat, lon, days);

        return ResponseEntity.ok(forecast);
    }

    /**
     * Verifica si hay condiciones climáticas peligrosas
     * GET /api/weather/hazards?lat={lat}&lon={lon}
     */
    @GetMapping("/hazards")
    public ResponseEntity<WeatherService.WeatherAlertDTO> checkWeatherHazards(
            @RequestParam Double lat,
            @RequestParam Double lon) {

        WeatherService.WeatherAlertDTO hazards = weatherService.checkWeatherHazards(lat, lon);

        if (hazards == null) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(hazards);
    }
}
