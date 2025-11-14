package com.roadwarnings.narino.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceTokenResponseDTO {

    private Long id;
    private Long userId;
    private String deviceType;
    private String deviceName;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime lastUsed;
}
