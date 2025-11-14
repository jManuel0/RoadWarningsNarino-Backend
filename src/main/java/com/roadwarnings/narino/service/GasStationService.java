package com.roadwarnings.narino.service;

import com.roadwarnings.narino.dto.request.GasStationFilterDTO;
import com.roadwarnings.narino.dto.request.GasStationRequestDTO;
import com.roadwarnings.narino.dto.response.GasStationResponseDTO;
import com.roadwarnings.narino.entity.GasStation;
import com.roadwarnings.narino.exception.ResourceNotFoundException;
import com.roadwarnings.narino.repository.GasStationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class GasStationService {

    private final GasStationRepository gasStationRepository;

    private static final String GAS_STATION_NOT_FOUND = "Estación de servicio no encontrada";
    private static final double EARTH_RADIUS_KM = 6371.0;

    public GasStationResponseDTO createGasStation(GasStationRequestDTO request) {
        log.info("Creando estación de servicio: {}", request.getName());

        GasStation gasStation = GasStation.builder()
                .name(request.getName())
                .brand(request.getBrand())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .address(request.getAddress())
                .municipality(request.getMunicipality())
                .phoneNumber(request.getPhoneNumber())
                .hasGasoline(request.getHasGasoline() != null || request.getHasGasoline())
                .hasDiesel(request.getHasDiesel() != null || request.getHasDiesel())
                .gasolinePrice(request.getGasolinePrice())
                .dieselPrice(request.getDieselPrice())
                .isOpen24Hours(request.getIsOpen24Hours() != null && request.getIsOpen24Hours())
                .openingTime(request.getOpeningTime())
                .closingTime(request.getClosingTime())
                .isAvailable(request.getIsAvailable() != null || request.getIsAvailable())
                .build();

        gasStation = gasStationRepository.save(gasStation);
        return mapToResponseDTO(gasStation);
    }

    public List<GasStationResponseDTO> getAllGasStations() {
        return gasStationRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    public Page<GasStationResponseDTO> getAllGasStationsPaginated(Pageable pageable) {
        return gasStationRepository.findAll(pageable)
                .map(this::mapToResponseDTO);
    }

    public GasStationResponseDTO getGasStationById(Long id) {
        GasStation gasStation = gasStationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(GAS_STATION_NOT_FOUND));
        return mapToResponseDTO(gasStation);
    }

    public GasStationResponseDTO updateGasStation(Long id, GasStationRequestDTO request) {
        GasStation gasStation = gasStationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(GAS_STATION_NOT_FOUND));

        gasStation.setName(request.getName());
        gasStation.setBrand(request.getBrand());
        gasStation.setLatitude(request.getLatitude());
        gasStation.setLongitude(request.getLongitude());
        gasStation.setAddress(request.getAddress());
        gasStation.setMunicipality(request.getMunicipality());
        gasStation.setPhoneNumber(request.getPhoneNumber());
        gasStation.setHasGasoline(request.getHasGasoline());
        gasStation.setHasDiesel(request.getHasDiesel());
        gasStation.setGasolinePrice(request.getGasolinePrice());
        gasStation.setDieselPrice(request.getDieselPrice());
        gasStation.setIsOpen24Hours(request.getIsOpen24Hours());
        gasStation.setOpeningTime(request.getOpeningTime());
        gasStation.setClosingTime(request.getClosingTime());
        gasStation.setIsAvailable(request.getIsAvailable());

        gasStation = gasStationRepository.save(gasStation);
        return mapToResponseDTO(gasStation);
    }

    public void deleteGasStation(Long id) {
        GasStation gasStation = gasStationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(GAS_STATION_NOT_FOUND));
        gasStationRepository.delete(gasStation);
    }

    public List<GasStationResponseDTO> getNearbyGasStations(Double latitude,
                                                            Double longitude,
                                                            Double radiusKm) {
        return gasStationRepository.findAll().stream()
                .filter(gs -> gs.getLatitude() != null && gs.getLongitude() != null)
                .filter(gs -> calculateDistanceKm(
                        latitude,
                        longitude,
                        gs.getLatitude(),
                        gs.getLongitude()
                ) <= radiusKm)
                .map(this::mapToResponseDTO)
                .toList();
    }

    /**
     * Filtra gasolineras segun criterios avanzados
     */
    public Page<GasStationResponseDTO> filterGasStations(GasStationFilterDTO filter, Pageable pageable) {
        List<GasStation> gasStations = gasStationRepository.findAll();

        // Aplicar filtros
        List<GasStation> filtered = gasStations.stream()
                .filter(gs -> matchesFilter(gs, filter))
                .collect(Collectors.toList());

        // Paginacion manual
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filtered.size());

        List<GasStationResponseDTO> pageContent = filtered.subList(start, end).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());

        return new PageImpl<>(pageContent, pageable, filtered.size());
    }

    /**
     * Obtiene gasolineras abiertas en este momento
     */
    public List<GasStationResponseDTO> getOpenNow() {
        LocalTime now = LocalTime.now();

        return gasStationRepository.findAll().stream()
                .filter(gs -> gs.getIsAvailable())
                .filter(gs -> {
                    if (gs.getIsOpen24Hours()) {
                        return true;
                    }
                    if (gs.getOpeningTime() != null && gs.getClosingTime() != null) {
                        try {
                            LocalTime opening = LocalTime.parse(gs.getOpeningTime());
                            LocalTime closing = LocalTime.parse(gs.getClosingTime());
                            return now.isAfter(opening) && now.isBefore(closing);
                        } catch (Exception e) {
                            log.warn("Error parsing time for gas station {}: {}", gs.getId(), e.getMessage());
                            return false;
                        }
                    }
                    return false;
                })
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene gasolineras por tipo de combustible
     */
    public List<GasStationResponseDTO> getByFuelType(String fuelType) {
        return gasStationRepository.findAll().stream()
                .filter(gs -> {
                    if ("GASOLINE".equalsIgnoreCase(fuelType)) {
                        return gs.getHasGasoline();
                    } else if ("DIESEL".equalsIgnoreCase(fuelType)) {
                        return gs.getHasDiesel();
                    }
                    return false;
                })
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    private boolean matchesFilter(GasStation gs, GasStationFilterDTO filter) {
        // Filtro por marca
        if (filter.getBrand() != null && !filter.getBrand().isBlank()) {
            if (gs.getBrand() == null || !gs.getBrand().toLowerCase().contains(filter.getBrand().toLowerCase())) {
                return false;
            }
        }

        // Filtro por municipio
        if (filter.getMunicipality() != null && !filter.getMunicipality().isBlank()) {
            if (gs.getMunicipality() == null || !gs.getMunicipality().toLowerCase().contains(filter.getMunicipality().toLowerCase())) {
                return false;
            }
        }

        // Filtro por disponibilidad
        if (filter.getIsAvailable() != null && !gs.getIsAvailable().equals(filter.getIsAvailable())) {
            return false;
        }

        // Filtro por gasolina
        if (filter.getHasGasoline() != null && filter.getHasGasoline() && !gs.getHasGasoline()) {
            return false;
        }

        // Filtro por diesel
        if (filter.getHasDiesel() != null && filter.getHasDiesel() && !gs.getHasDiesel()) {
            return false;
        }

        // Filtro por 24 horas
        if (filter.getIsOpen24Hours() != null && !gs.getIsOpen24Hours().equals(filter.getIsOpen24Hours())) {
            return false;
        }

        // Filtro por precio maximo de gasolina
        if (filter.getMaxGasolinePrice() != null && gs.getGasolinePrice() != null) {
            if (gs.getGasolinePrice().compareTo(java.math.BigDecimal.valueOf(filter.getMaxGasolinePrice())) > 0) {
                return false;
            }
        }

        // Filtro por precio maximo de diesel
        if (filter.getMaxDieselPrice() != null && gs.getDieselPrice() != null) {
            if (gs.getDieselPrice().compareTo(java.math.BigDecimal.valueOf(filter.getMaxDieselPrice())) > 0) {
                return false;
            }
        }

        // Filtro por ubicacion (radio)
        if (filter.getLatitude() != null && filter.getLongitude() != null && filter.getRadiusKm() != null) {
            if (gs.getLatitude() == null || gs.getLongitude() == null) {
                return false;
            }
            double distance = calculateDistanceKm(
                    filter.getLatitude(), filter.getLongitude(),
                    gs.getLatitude(), gs.getLongitude()
            );
            if (distance > filter.getRadiusKm()) {
                return false;
            }
        }

        return true;
    }

    private double calculateDistanceKm(double lat1, double lon1, double lat2, double lon2) {
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }

    private GasStationResponseDTO mapToResponseDTO(GasStation gasStation) {
        return GasStationResponseDTO.builder()
                .id(gasStation.getId())
                .name(gasStation.getName())
                .brand(gasStation.getBrand())
                .latitude(gasStation.getLatitude())
                .longitude(gasStation.getLongitude())
                .address(gasStation.getAddress())
                .municipality(gasStation.getMunicipality())
                .phoneNumber(gasStation.getPhoneNumber())
                .hasGasoline(gasStation.getHasGasoline())
                .hasDiesel(gasStation.getHasDiesel())
                .gasolinePrice(gasStation.getGasolinePrice())
                .dieselPrice(gasStation.getDieselPrice())
                .isOpen24Hours(gasStation.getIsOpen24Hours())
                .openingTime(gasStation.getOpeningTime())
                .closingTime(gasStation.getClosingTime())
                .isAvailable(gasStation.getIsAvailable())
                .createdAt(gasStation.getCreatedAt())
                .updatedAt(gasStation.getUpdatedAt())
                .build();
    }
}
