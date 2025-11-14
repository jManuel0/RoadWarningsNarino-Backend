package com.roadwarnings.narino.service;

import com.roadwarnings.narino.dto.request.GasStationRequestDTO;
import com.roadwarnings.narino.dto.response.GasStationResponseDTO;
import com.roadwarnings.narino.entity.GasStation;
import com.roadwarnings.narino.exception.ResourceNotFoundException;
import com.roadwarnings.narino.repository.GasStationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GasStationServiceTest {

    @Mock
    private GasStationRepository gasStationRepository;

    @InjectMocks
    private GasStationService gasStationService;

    private GasStation testGasStation;
    private GasStationRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        testGasStation = GasStation.builder()
                .id(1L)
                .name("Estación Terpel")
                .brand("Terpel")
                .latitude(1.2345)
                .longitude(-77.2812)
                .address("Calle 18 # 25-40")
                .municipality("Pasto")
                .phoneNumber("3001234567")
                .hasGasoline(true)
                .hasDiesel(true)
                .gasolinePrice(BigDecimal.valueOf(10500.0))
                .dieselPrice(BigDecimal.valueOf(9800.0))
                .isOpen24Hours(true)
                .isAvailable(true)
                .createdAt(LocalDateTime.now())
                .build();

        requestDTO = new GasStationRequestDTO();
        requestDTO.setName("Estación Terpel");
        requestDTO.setBrand("Terpel");
        requestDTO.setLatitude(1.2345);
        requestDTO.setLongitude(-77.2812);
        requestDTO.setAddress("Calle 18 # 25-40");
        requestDTO.setMunicipality("Pasto");
        requestDTO.setPhoneNumber("3001234567");
        requestDTO.setHasGasoline(true);
        requestDTO.setHasDiesel(true);
        requestDTO.setGasolinePrice(BigDecimal.valueOf(10500.0));
        requestDTO.setDieselPrice(BigDecimal.valueOf(9800.0));
        requestDTO.setIsOpen24Hours(true);
        requestDTO.setIsAvailable(true);
    }

    @Test
    void createGasStation_ShouldCreateSuccessfully() {
        // Arrange
        when(gasStationRepository.save(any(GasStation.class))).thenReturn(testGasStation);

        // Act
        GasStationResponseDTO result = gasStationService.createGasStation(requestDTO);

        // Assert
        assertNotNull(result);
        assertEquals("Estación Terpel", result.getName());
        assertEquals("Terpel", result.getBrand());
        verify(gasStationRepository).save(any(GasStation.class));
    }

    @Test
    void getAllGasStations_ShouldReturnAllGasStations() {
        // Arrange
        when(gasStationRepository.findAll()).thenReturn(List.of(testGasStation));

        // Act
        List<GasStationResponseDTO> result = gasStationService.getAllGasStations();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Estación Terpel", result.get(0).getName());
        verify(gasStationRepository).findAll();
    }

    @Test
    void getGasStationById_WhenExists_ShouldReturnGasStation() {
        // Arrange
        when(gasStationRepository.findById(1L)).thenReturn(Optional.of(testGasStation));

        // Act
        GasStationResponseDTO result = gasStationService.getGasStationById(1L);

        // Assert
        assertNotNull(result);
        assertEquals("Estación Terpel", result.getName());
        assertEquals(1.2345, result.getLatitude());
        verify(gasStationRepository).findById(1L);
    }

    @Test
    void getGasStationById_WhenNotExists_ShouldThrowException() {
        // Arrange
        when(gasStationRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            gasStationService.getGasStationById(999L);
        });
        verify(gasStationRepository).findById(999L);
    }

    @Test
    void updateGasStation_WhenExists_ShouldUpdateSuccessfully() {
        // Arrange
        when(gasStationRepository.findById(1L)).thenReturn(Optional.of(testGasStation));
        when(gasStationRepository.save(any(GasStation.class))).thenReturn(testGasStation);

        requestDTO.setName("Estación Terpel Updated");
        requestDTO.setGasolinePrice(BigDecimal.valueOf(11000.0));

        // Act
        GasStationResponseDTO result = gasStationService.updateGasStation(1L, requestDTO);

        // Assert
        assertNotNull(result);
        verify(gasStationRepository).findById(1L);
        verify(gasStationRepository).save(any(GasStation.class));
    }

    @Test
    void updateGasStation_WhenNotExists_ShouldThrowException() {
        // Arrange
        when(gasStationRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            gasStationService.updateGasStation(999L, requestDTO);
        });
        verify(gasStationRepository).findById(999L);
        verify(gasStationRepository, never()).save(any(GasStation.class));
    }

    @Test
    void deleteGasStation_WhenExists_ShouldDeleteSuccessfully() {
        // Arrange
        when(gasStationRepository.findById(1L)).thenReturn(Optional.of(testGasStation));

        // Act
        gasStationService.deleteGasStation(1L);

        // Assert
        verify(gasStationRepository).findById(1L);
        verify(gasStationRepository).delete(testGasStation);
    }

    @Test
    void deleteGasStation_WhenNotExists_ShouldThrowException() {
        // Arrange
        when(gasStationRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            gasStationService.deleteGasStation(999L);
        });
        verify(gasStationRepository).findById(999L);
        verify(gasStationRepository, never()).delete(any(GasStation.class));
    }

    @Test
    void getNearbyGasStations_ShouldReturnStationsWithinRadius() {
        // Arrange
        GasStation nearbyStation = GasStation.builder()
                .id(2L)
                .name("Estación Cerca")
                .latitude(1.2350) // Muy cerca
                .longitude(-77.2815)
                .build();

        GasStation farStation = GasStation.builder()
                .id(3L)
                .name("Estación Lejos")
                .latitude(5.0) // Muy lejos
                .longitude(-80.0)
                .build();

        when(gasStationRepository.findAll()).thenReturn(List.of(testGasStation, nearbyStation, farStation));

        // Act
        List<GasStationResponseDTO> result = gasStationService.getNearbyGasStations(1.2345, -77.2812, 5.0);

        // Assert
        assertNotNull(result);
        assertTrue(result.size() >= 2); // Al menos las 2 estaciones cercanas
        verify(gasStationRepository).findAll();
    }

    @Test
    void getNearbyGasStations_ShouldFilterNullCoordinates() {
        // Arrange
        GasStation stationWithoutCoords = GasStation.builder()
                .id(4L)
                .name("Estación Sin Coordenadas")
                .latitude(null)
                .longitude(null)
                .build();

        when(gasStationRepository.findAll()).thenReturn(List.of(testGasStation, stationWithoutCoords));

        // Act
        List<GasStationResponseDTO> result = gasStationService.getNearbyGasStations(1.2345, -77.2812, 10.0);

        // Assert
        assertNotNull(result);
        // Solo debe retornar la estación con coordenadas válidas
        assertTrue(result.stream().noneMatch(gs -> gs.getLatitude() == null));
        verify(gasStationRepository).findAll();
    }
}
