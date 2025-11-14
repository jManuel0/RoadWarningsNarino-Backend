package com.roadwarnings.narino.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceTokenRequestDTO {

    @NotBlank(message = "El token es requerido")
    private String token;

    @NotBlank(message = "El tipo de dispositivo es requerido")
    private String deviceType; // iOS, Android, Web

    private String deviceName;
}
