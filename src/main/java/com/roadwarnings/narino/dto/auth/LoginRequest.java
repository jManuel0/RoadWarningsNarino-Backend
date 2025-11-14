package com.roadwarnings.narino.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "El username o email es obligatorio")
    private String username; // o email

    @NotBlank(message = "La contraseña es obligatoria")
    private String password;
}
