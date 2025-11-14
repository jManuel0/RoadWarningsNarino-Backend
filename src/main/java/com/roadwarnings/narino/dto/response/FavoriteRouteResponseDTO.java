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
public class FavoriteRouteResponseDTO {

    private Long id;
    private Long userId;
    private Long routeId;
    private String routeName;
    private String customName;
    private Boolean notificationsEnabled;
    private LocalDateTime savedAt;
    private LocalDateTime lastUsed;

    // Información adicional de la ruta
    private String originName;
    private String destinationName;
    private Double distanceKm;
    private Integer activeAlertsCount;
}
