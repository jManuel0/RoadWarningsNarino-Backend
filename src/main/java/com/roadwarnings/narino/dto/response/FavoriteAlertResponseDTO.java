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
public class FavoriteAlertResponseDTO {

    private Long id;
    private Long userId;
    private Long alertId;
    private String alertTitle;
    private LocalDateTime savedAt;

    // Información adicional de la alerta
    private String alertType;
    private String alertSeverity;
    private String alertStatus;
    private Double latitude;
    private Double longitude;
    private String location;
}
