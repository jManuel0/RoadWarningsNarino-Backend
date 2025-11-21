package com.roadwarnings.narino.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteCalculationRequestDTO {

    @NotNull(message = "El origen es obligatorio")
    private Coordinates origin;

    @NotNull(message = "El destino es obligatorio")
    private Coordinates destination;

    @Builder.Default
    private Boolean alternatives = true;

    @Builder.Default
    private Boolean avoidAlerts = false;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Coordinates {
        @NotNull
        private Double lat;

        @NotNull
        private Double lng;
    }
}
