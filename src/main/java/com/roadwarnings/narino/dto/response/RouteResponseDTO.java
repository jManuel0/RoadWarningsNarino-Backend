package com.roadwarnings.narino.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteResponseDTO {

    private Long id;
    private String name;
    private Double originLatitude;
    private Double originLongitude;
    private String originName;
    private Double destinationLatitude;
    private Double destinationLongitude;
    private String destinationName;
    private Double distanceKm;
    private Integer estimatedTimeMinutes;
    private String polyline;
    private Integer activeAlertsCount;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
