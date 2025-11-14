package com.roadwarnings.narino.dto.response;

import com.roadwarnings.narino.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDTO {

    private Long id;
    private String username;
    private String email;
    private UserRole role;
    private Boolean isActive;
    private String preferredTheme;
    private LocalDateTime createdAt;
}
