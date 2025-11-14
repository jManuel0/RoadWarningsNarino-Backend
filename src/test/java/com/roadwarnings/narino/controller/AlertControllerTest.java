package com.roadwarnings.narino.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.roadwarnings.narino.dto.request.AlertaRequestDTO;
import com.roadwarnings.narino.dto.response.AlertaResponseDTO;
import com.roadwarnings.narino.enums.AlertSeverity;
import com.roadwarnings.narino.enums.AlertStatus;
import com.roadwarnings.narino.enums.AlertType;
import com.roadwarnings.narino.service.AlertService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AlertController.class)
class AlertControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AlertService alertService;

    private AlertaRequestDTO requestDTO;
    private AlertaResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        requestDTO = new AlertaRequestDTO();
        requestDTO.setType(AlertType.ACCIDENTE);
        requestDTO.setTitle("Test Alert");
        requestDTO.setDescription("Test Description");
        requestDTO.setLatitude(1.2345);
        requestDTO.setLongitude(-77.2812);
        requestDTO.setLocation("Pasto, Nariño");
        requestDTO.setSeverity(AlertSeverity.HIGH);

        responseDTO = AlertaResponseDTO.builder()
                .id(1L)
                .type(AlertType.ACCIDENTE)
                .title("Test Alert")
                .description("Test Description")
                .latitude(1.2345)
                .longitude(-77.2812)
                .location("Pasto, Nariño")
                .severity(AlertSeverity.HIGH)
                .status(AlertStatus.ACTIVE)
                .username("system")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @WithMockUser
    void createAlert_ShouldReturnCreatedAlert() throws Exception {
        // Arrange
        when(alertService.createAlert(any(AlertaRequestDTO.class), anyString()))
                .thenReturn(responseDTO);

        // Act & Assert
        mockMvc.perform(post("/alert")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test Alert"))
                .andExpect(jsonPath("$.type").value("ACCIDENTE"));
    }

    @Test
    @WithMockUser
    void getAllAlerts_ShouldReturnListOfAlerts() throws Exception {
        // Arrange
        when(alertService.getAllAlerts()).thenReturn(List.of(responseDTO));

        // Act & Assert
        mockMvc.perform(get("/alert")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Test Alert"))
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    @WithMockUser
    void getActiveAlerts_ShouldReturnOnlyActiveAlerts() throws Exception {
        // Arrange
        when(alertService.getActiveAlerts()).thenReturn(List.of(responseDTO));

        // Act & Assert
        mockMvc.perform(get("/alert/active")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));
    }

    @Test
    @WithMockUser
    void getAlertById_ShouldReturnAlert() throws Exception {
        // Arrange
        when(alertService.getAlertById(1L)).thenReturn(responseDTO);

        // Act & Assert
        mockMvc.perform(get("/alert/1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test Alert"));
    }

    @Test
    @WithMockUser
    void getNearbyAlerts_ShouldReturnAlertsWithinRadius() throws Exception {
        // Arrange
        when(alertService.getNearbyAlerts(anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(List.of(responseDTO));

        // Act & Assert
        mockMvc.perform(get("/alert/nearby")
                        .with(csrf())
                        .param("latitude", "1.2345")
                        .param("longitude", "-77.2812")
                        .param("radius", "10.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].latitude").value(1.2345))
                .andExpect(jsonPath("$[0].longitude").value(-77.2812));
    }

    @Test
    @WithMockUser
    void updateAlert_ShouldReturnUpdatedAlert() throws Exception {
        // Arrange
        when(alertService.updateAlert(anyLong(), any(AlertaRequestDTO.class), anyString()))
                .thenReturn(responseDTO);

        // Act & Assert
        mockMvc.perform(put("/alert/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser
    void deleteAlert_ShouldReturnNoContent() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/alert/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    void updateAlertStatus_ShouldReturnUpdatedAlert() throws Exception {
        // Arrange
        when(alertService.updateAlertStatus(anyLong(), any(AlertStatus.class)))
                .thenReturn(responseDTO);

        // Act & Assert
        mockMvc.perform(patch("/alert/1/status")
                        .with(csrf())
                        .param("status", "RESOLVED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }
}
