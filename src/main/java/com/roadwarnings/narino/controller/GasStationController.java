package com.roadwarnings.narino.controller;

import com.roadwarnings.narino.dto.request.GasStationRequestDTO;
import com.roadwarnings.narino.dto.response.GasStationResponseDTO;
import com.roadwarnings.narino.service.GasStationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/gas-stations")
@RequiredArgsConstructor
public class GasStationController {

    private final GasStationService gasStationService;

    @PostMapping
    public ResponseEntity<GasStationResponseDTO> create(@RequestBody GasStationRequestDTO request) {
        GasStationResponseDTO response = gasStationService.createGasStation(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<GasStationResponseDTO>> getAll() {
        return ResponseEntity.ok(gasStationService.getAllGasStations());
    }

    @GetMapping("/{id}")
    public ResponseEntity<GasStationResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(gasStationService.getGasStationById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GasStationResponseDTO> update(
            @PathVariable Long id,
            @RequestBody GasStationRequestDTO request
    ) {
        return ResponseEntity.ok(gasStationService.updateGasStation(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        gasStationService.deleteGasStation(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/nearby")
    public ResponseEntity<List<GasStationResponseDTO>> getNearby(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam Double radiusKm
    ) {
        return ResponseEntity.ok(
                gasStationService.getNearbyGasStations(latitude, longitude, radiusKm)
        );
    }
}

