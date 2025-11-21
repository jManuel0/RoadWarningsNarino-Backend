package com.roadwarnings.narino.controller;

import com.roadwarnings.narino.dto.response.PlaceDetailDTO;
import com.roadwarnings.narino.dto.response.PlaceSearchResultDTO;
import com.roadwarnings.narino.service.GoogleMapsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/places")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PlacesController {

    private final GoogleMapsService googleMapsService;

    /**
     * GET /api/places/search
     * Buscar lugares
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, List<PlaceSearchResultDTO>>> searchPlaces(
            @RequestParam String query,
            @RequestParam Double lat,
            @RequestParam Double lng,
            @RequestParam(required = false) Integer radius) {

        List<PlaceSearchResultDTO> places = googleMapsService.searchPlaces(query, lat, lng, radius);
        return ResponseEntity.ok(Map.of("places", places));
    }

    /**
     * GET /api/places/:placeId
     * Obtener detalles de un lugar
     */
    @GetMapping("/{placeId}")
    public ResponseEntity<PlaceDetailDTO> getPlaceDetails(@PathVariable String placeId) {
        PlaceDetailDTO placeDetail = googleMapsService.getPlaceDetails(placeId);
        return ResponseEntity.ok(placeDetail);
    }
}
