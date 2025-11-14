package com.roadwarnings.narino.dto.websocket;

import com.roadwarnings.narino.enums.AlertType;
import com.roadwarnings.narino.enums.AlertStatus;
import com.roadwarnings.narino.enums.AlertSeverity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertWebSocketMessage {

    private String action; // CREATED, UPDATED, DELETED, STATUS_CHANGED
    private Long alertId;
    private AlertType type;
    private String title;
    private String description;
    private Double latitude;
    private Double longitude;
    private String location;
    private AlertSeverity severity;
    private AlertStatus status;
    private String username;
    private Integer upvotes;
    private Integer downvotes;
    private LocalDateTime timestamp;
}
