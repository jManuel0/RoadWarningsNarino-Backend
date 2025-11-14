package com.roadwarnings.narino.service;

import com.roadwarnings.narino.dto.request.AlertaRequestDTO;
import com.roadwarnings.narino.dto.response.AlertaResponseDTO;
import com.roadwarnings.narino.entity.Alert;
import com.roadwarnings.narino.entity.User;
import com.roadwarnings.narino.enums.AlertSeverity;
import com.roadwarnings.narino.enums.AlertStatus;
import com.roadwarnings.narino.enums.AlertType;
import com.roadwarnings.narino.enums.UserRole;
import com.roadwarnings.narino.exception.ResourceNotFoundException;
import com.roadwarnings.narino.exception.UnauthorizedException;
import com.roadwarnings.narino.repository.AlertRepository;
import com.roadwarnings.narino.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlertServiceTest {

    @Mock
    private AlertRepository alertRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AlertService alertService;

    private User testUser;
    private Alert testAlert;
    private AlertaRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .role(UserRole.USER)
                .build();

        testAlert = Alert.builder()
                .id(1L)
                .type(AlertType.ACCIDENTE)
                .title("Test Alert")
                .description("Test Description")
                .latitude(1.2345)
                .longitude(-77.2812)
                .location("Pasto, Nariño")
                .severity(AlertSeverity.HIGH)
                .status(AlertStatus.ACTIVE)
                .user(testUser)
                .createdAt(LocalDateTime.now())
                .build();

        requestDTO = new AlertaRequestDTO();
        requestDTO.setType(AlertType.ACCIDENTE);
        requestDTO.setTitle("New Alert");
        requestDTO.setDescription("New Description");
        requestDTO.setLatitude(1.2345);
        requestDTO.setLongitude(-77.2812);
        requestDTO.setLocation("Pasto, Nariño");
        requestDTO.setSeverity(AlertSeverity.HIGH);
    }

    @Test
    void getAllAlerts_ShouldReturnAllAlerts() {
        // Arrange
        when(alertRepository.findAll()).thenReturn(List.of(testAlert));

        // Act
        List<AlertaResponseDTO> result = alertService.getAllAlerts();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Alert", result.get(0).getTitle());
        verify(alertRepository).findAll();
    }

    @Test
    void getAlertById_WhenAlertExists_ShouldReturnAlert() {
        // Arrange
        when(alertRepository.findById(1L)).thenReturn(Optional.of(testAlert));

        // Act
        AlertaResponseDTO result = alertService.getAlertById(1L);

        // Assert
        assertNotNull(result);
        assertEquals("Test Alert", result.getTitle());
        assertEquals(AlertType.ACCIDENTE, result.getType());
        verify(alertRepository).findById(1L);
    }

    @Test
    void getAlertById_WhenAlertNotExists_ShouldThrowException() {
        // Arrange
        when(alertRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            alertService.getAlertById(999L);
        });
        verify(alertRepository).findById(999L);
    }

    @Test
    void updateAlert_WhenUserIsOwner_ShouldUpdateSuccessfully() {
        // Arrange
        when(alertRepository.findById(1L)).thenReturn(Optional.of(testAlert));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(alertRepository.save(any(Alert.class))).thenReturn(testAlert);

        requestDTO.setTitle("Updated Title");

        // Act
        AlertaResponseDTO result = alertService.updateAlert(1L, requestDTO, "testuser");

        // Assert
        assertNotNull(result);
        verify(alertRepository).findById(1L);
        verify(alertRepository).save(any(Alert.class));
    }

    @Test
    void updateAlert_WhenUserIsNotOwner_ShouldThrowUnauthorizedException() {
        // Arrange
        User otherUser = User.builder()
                .id(2L)
                .username("otheruser")
                .email("other@example.com")
                .role(UserRole.USER)
                .build();

        when(alertRepository.findById(1L)).thenReturn(Optional.of(testAlert));
        when(userRepository.findByUsername("otheruser")).thenReturn(Optional.of(otherUser));

        // Act & Assert
        assertThrows(UnauthorizedException.class, () -> {
            alertService.updateAlert(1L, requestDTO, "otheruser");
        });
        verify(alertRepository).findById(1L);
        verify(alertRepository, never()).save(any(Alert.class));
    }

    @Test
    void deleteAlert_WhenUserIsOwner_ShouldDeleteSuccessfully() {
        // Arrange
        when(alertRepository.findById(1L)).thenReturn(Optional.of(testAlert));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Act
        alertService.deleteAlert(1L, "testuser");

        // Assert
        verify(alertRepository).findById(1L);
        verify(alertRepository).delete(testAlert);
    }

    @Test
    void deleteAlert_WhenUserIsNotOwner_ShouldThrowUnauthorizedException() {
        // Arrange
        User otherUser = User.builder()
                .id(2L)
                .username("otheruser")
                .email("other@example.com")
                .role(UserRole.USER)
                .build();

        when(alertRepository.findById(1L)).thenReturn(Optional.of(testAlert));
        when(userRepository.findByUsername("otheruser")).thenReturn(Optional.of(otherUser));

        // Act & Assert
        assertThrows(UnauthorizedException.class, () -> {
            alertService.deleteAlert(1L, "otheruser");
        });
        verify(alertRepository).findById(1L);
        verify(alertRepository, never()).delete(any(Alert.class));
    }

    @Test
    void getActiveAlerts_ShouldReturnOnlyActiveAlerts() {
        // Arrange
        when(alertRepository.findByStatus(AlertStatus.ACTIVE))
                .thenReturn(List.of(testAlert));

        // Act
        List<AlertaResponseDTO> result = alertService.getActiveAlerts();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(alertRepository).findByStatus(AlertStatus.ACTIVE);
    }

    @Test
    void updateAlertStatus_ShouldUpdateStatus() {
        // Arrange
        when(alertRepository.findById(1L)).thenReturn(Optional.of(testAlert));
        when(alertRepository.save(any(Alert.class))).thenReturn(testAlert);

        // Act
        AlertaResponseDTO result = alertService.updateAlertStatus(1L, AlertStatus.RESOLVED);

        // Assert
        assertNotNull(result);
        verify(alertRepository).findById(1L);
        verify(alertRepository).save(any(Alert.class));
    }
}
