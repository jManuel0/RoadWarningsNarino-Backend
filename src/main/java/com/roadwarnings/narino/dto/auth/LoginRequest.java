package com.roadwarnings.narino.dto.auth;

import lombok.Data;

@Data
public class LoginRequest {
    private String username; // o email
    private String password;
}
