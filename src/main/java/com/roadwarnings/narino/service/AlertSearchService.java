package com.roadwarnings.narino.service;

import com.roadwarnings.narino.dto.request.AlertSearchDTO;
import com.roadwarnings.narino.dto.response.AlertaResponseDTO;
import com.roadwarnings.narino.entity.Alert;
import com.roadwarnings.narino.repository.AlertRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para búsqueda avanzada de alertas
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AlertSearchService {

    private final AlertRepository alertRepository;

    /**
     * Búsqueda avanzada con múltiples filtros
     */
    public Page<AlertaResponseDTO> searchAlerts(AlertSearchDTO searchDTO) {
        log.info("Búsqueda avanzada con filtros: {}", searchDTO);

        // Obtener todas las alertas (en producción, usar Specifications de JPA)
        List<Alert> allAlerts = alertRepository.findAll();

        // Aplicar filtros
        List<Alert> filteredAlerts = allAlerts.stream()
                .filter(alert -> matchesKeyword(alert, searchDTO.getKeyword()))
                .filter(alert -> matchesTypes(alert, searchDTO.getTypes()))
                .filter(alert -> matchesSeverities(alert, searchDTO.getSeverities()))
                .filter(alert -> matchesStatuses(alert, searchDTO.getStatuses()))
                .filter(alert -> matchesLocation(alert, searchDTO))
                .filter(alert -> matchesTimeRange(alert, searchDTO))
                .filter(alert -> matchesEngagement(alert, searchDTO))
                .filter(alert -> matchesUser(alert, searchDTO))
                .collect(Collectors.toList());

        // Ordenar
        filteredAlerts = sortAlerts(filteredAlerts, searchDTO);

        // Paginar
        int page = searchDTO.getPage() != null ? searchDTO.getPage() : 0;
        int size = searchDTO.getSize() != null ? searchDTO.getSize() : 20;

        int start = page * size;
        int end = Math.min(start + size, filteredAlerts.size());

        List<Alert> paginatedAlerts = start < filteredAlerts.size() ?
                filteredAlerts.subList(start, end) : List.of();

        // Convertir a DTO
        List<AlertaResponseDTO> alertDTOs = paginatedAlerts.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());

        Pageable pageable = PageRequest.of(page, size);
        return new PageImpl<>(alertDTOs, pageable, filteredAlerts.size());
    }

    // ==================== MÉTODOS DE FILTRADO ====================

    private boolean matchesKeyword(Alert alert, String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return true;
        }

        String lowerKeyword = keyword.toLowerCase();
        return (alert.getTitle() != null && alert.getTitle().toLowerCase().contains(lowerKeyword)) ||
               (alert.getDescription() != null && alert.getDescription().toLowerCase().contains(lowerKeyword));
    }

    private boolean matchesTypes(Alert alert, List types) {
        return types == null || types.isEmpty() || types.contains(alert.getType());
    }

    private boolean matchesSeverities(Alert alert, List severities) {
        return severities == null || severities.isEmpty() || severities.contains(alert.getSeverity());
    }

    private boolean matchesStatuses(Alert alert, List statuses) {
        return statuses == null || statuses.isEmpty() || statuses.contains(alert.getStatus());
    }

    private boolean matchesLocation(Alert alert, AlertSearchDTO searchDTO) {
        // Búsqueda por keyword en ubicación
        if (searchDTO.getLocationKeyword() != null && !searchDTO.getLocationKeyword().isBlank()) {
            String locationLower = searchDTO.getLocationKeyword().toLowerCase();
            if (alert.getLocation() == null ||
                !alert.getLocation().toLowerCase().contains(locationLower)) {
                return false;
            }
        }

        // Búsqueda por proximidad geográfica
        if (searchDTO.getLatitude() != null &&
            searchDTO.getLongitude() != null &&
            searchDTO.getRadiusKm() != null) {

            double distance = calculateDistance(
                    searchDTO.getLatitude(), searchDTO.getLongitude(),
                    alert.getLatitude(), alert.getLongitude()
            );

            return distance <= searchDTO.getRadiusKm();
        }

        return true;
    }

    private boolean matchesTimeRange(Alert alert, AlertSearchDTO searchDTO) {
        if (searchDTO.getCreatedAfter() != null &&
            alert.getCreatedAt().isBefore(searchDTO.getCreatedAfter())) {
            return false;
        }

        if (searchDTO.getCreatedBefore() != null &&
            alert.getCreatedAt().isAfter(searchDTO.getCreatedBefore())) {
            return false;
        }

        return true;
    }

    private boolean matchesEngagement(Alert alert, AlertSearchDTO searchDTO) {
        if (searchDTO.getMinUpvotes() != null &&
            alert.getUpvotes() < searchDTO.getMinUpvotes()) {
            return false;
        }

        if (searchDTO.getMaxDownvotes() != null &&
            alert.getDownvotes() > searchDTO.getMaxDownvotes()) {
            return false;
        }

        return true;
    }

    private boolean matchesUser(Alert alert, AlertSearchDTO searchDTO) {
        if (searchDTO.getUserId() != null) {
            return alert.getUser() != null && alert.getUser().getId().equals(searchDTO.getUserId());
        }

        if (searchDTO.getUsername() != null && !searchDTO.getUsername().isBlank()) {
            return alert.getUser() != null &&
                   alert.getUser().getUsername().equalsIgnoreCase(searchDTO.getUsername());
        }

        return true;
    }

    // ==================== ORDENAMIENTO ====================

    private List<Alert> sortAlerts(List<Alert> alerts, AlertSearchDTO searchDTO) {
        String sortBy = searchDTO.getSortBy() != null ? searchDTO.getSortBy() : "createdAt";
        boolean ascending = "asc".equalsIgnoreCase(searchDTO.getSortDirection());

        return alerts.stream().sorted((a1, a2) -> {
            int comparison = switch (sortBy.toLowerCase()) {
                case "upvotes" -> Integer.compare(a1.getUpvotes(), a2.getUpvotes());
                case "severity" -> a1.getSeverity().compareTo(a2.getSeverity());
                case "createdat", "date" -> a1.getCreatedAt().compareTo(a2.getCreatedAt());
                default -> a1.getCreatedAt().compareTo(a2.getCreatedAt());
            };

            return ascending ? comparison : -comparison;
        }).collect(Collectors.toList());
    }

    // ==================== UTILIDADES ====================

    private double calculateDistance(Double lat1, Double lon1, Double lat2, Double lon2) {
        if (lat1 == null || lon1 == null || lat2 == null || lon2 == null) {
            return Double.MAX_VALUE;
        }

        final int R = 6371; // Radio de la Tierra en km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    private AlertaResponseDTO mapToResponseDTO(Alert alert) {
        return AlertaResponseDTO.builder()
                .id(alert.getId())
                .type(alert.getType())
                .title(alert.getTitle())
                .description(alert.getDescription())
                .latitude(alert.getLatitude())
                .longitude(alert.getLongitude())
                .location(alert.getLocation())
                .severity(alert.getSeverity())
                .status(alert.getStatus())
                .imageUrl(alert.getImageUrl())
                .upvotes(alert.getUpvotes())
                .downvotes(alert.getDownvotes())
                .userId(alert.getUser() != null ? alert.getUser().getId() : null)
                .username(alert.getUser() != null ? alert.getUser().getUsername() : null)
                .createdAt(alert.getCreatedAt())
                .build();
    }
}
