package com.roadwarnings.narino.dto.response;

import com.roadwarnings.narino.enums.AlertType;
import com.roadwarnings.narino.enums.AlertStatus;
import com.roadwarnings.narino.enums.AlertSeverity;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertaResponseDTO {

    private Long id;
    private AlertType type;
    private String title;
    private String description;

    private Double latitude;
    private Double longitude;
    private String location;

    private String municipality;       // 👈 nuevo campo
    private AlertSeverity severity;
    private AlertStatus status;

    private String username;
    private Long userId;

    private String imageUrl;
    private Integer upvotes;
    private Integer downvotes;

    private Integer estimatedDuration; // 👈 nuevo campo (en minutos)

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime expiresAt;
}
