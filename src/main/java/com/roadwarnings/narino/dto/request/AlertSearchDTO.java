package com.roadwarnings.narino.dto.request;

import com.roadwarnings.narino.enums.AlertSeverity;
import com.roadwarnings.narino.enums.AlertStatus;
import com.roadwarnings.narino.enums.AlertType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para búsqueda avanzada de alertas con múltiples filtros
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertSearchDTO {

    // Búsqueda por texto
    private String keyword; // Busca en título y descripción

    // Filtros de tipo y severidad
    private List<AlertType> types;
    private List<AlertSeverity> severities;
    private List<AlertStatus> statuses;

    // Filtros geográficos
    private Double latitude;
    private Double longitude;
    private Double radiusKm;
    private String locationKeyword; // Busca en el campo location

    // Filtros temporales
    private LocalDateTime createdAfter;
    private LocalDateTime createdBefore;

    // Filtros de engagement
    private Integer minUpvotes;
    private Integer maxDownvotes;

    // Filtro por usuario
    private Long userId;
    private String username;

    // Ordenamiento
    private String sortBy; // createdAt, upvotes, severity, distance
    private String sortDirection; // asc, desc

    // Paginación
    private Integer page;
    private Integer size;
}
