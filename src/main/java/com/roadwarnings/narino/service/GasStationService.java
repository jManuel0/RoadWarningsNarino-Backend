package com.roadwarnings.narino.service;

import com.roadwarnings.narino.dto.request.GasStationRequestDTO;
import com.roadwarnings.narino.dto.response.GasStationResponseDTO;
import com.roadwarnings.narino.entity.GasStation;
import com.roadwarnings.narino.exception.ResourceNotFoundException;
import com.roadwarnings.narino.repository.GasStationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class GasStationService {

    private final GasStationRepository gasStationRepository;

    private static final String GAS_STATION_NOT_FOUND = "Estación de servicio no encontrada";

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
