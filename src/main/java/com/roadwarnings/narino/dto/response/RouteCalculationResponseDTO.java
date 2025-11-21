package com.roadwarnings.narino.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteCalculationResponseDTO {

    private List<RouteOption> routes;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RouteOption {
        private String id;
        private String name;
        private Double distance; // en km
        private Integer duration; // en minutos
        private String traffic; // "low", "medium", "high"
        private Integer alerts;
        private String polyline;
        private List<RouteStep> steps;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RouteStep {
        private String instruction;
        private Double distance; // en km
        private Integer duration; // en minutos
        private Double lat;
        private Double lng;
    }
}
