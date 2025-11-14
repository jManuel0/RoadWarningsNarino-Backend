package com.roadwarnings.narino.dto.response;

import com.roadwarnings.narino.enums.ReportReason;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertReportResponseDTO {

    private Long id;
    private Long alertId;
    private String alertTitle;
    private Long reporterId;
    private String reporterUsername;
    private ReportReason reason;
    private String description;
    private Boolean reviewed;
    private Long reviewedById;
    private String reviewedByUsername;
    private LocalDateTime reviewedAt;
    private String reviewNotes;
    private LocalDateTime createdAt;
}
