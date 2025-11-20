package com.roadwarnings.narino.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefreshTokenRequest {
    @NotBlank(message = "Refresh token es requerido")
    private String refreshToken;
}
