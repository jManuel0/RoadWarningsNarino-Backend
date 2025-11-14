package com.roadwarnings.narino.dto.request;

import com.roadwarnings.narino.enums.AlertSeverity;
import com.roadwarnings.narino.enums.AlertStatus;
import com.roadwarnings.narino.enums.AlertType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertFilterDTO {

    private AlertType type;
    private AlertSeverity severity;
    private AlertStatus status;
    private Double latitude;
    private Double longitude;
    private Double radiusKm;
    private LocalDateTime fromDate;
    private LocalDateTime toDate;
    private LocalDateTime createdAfter;
    private LocalDateTime createdBefore;
    private String location;
    private String username;
    private Long userId;
    private Integer minUpvotes;
    private Integer maxDownvotes;
}
