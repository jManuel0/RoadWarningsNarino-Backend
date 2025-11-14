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
public class UserStatisticsResponseDTO {

    private Long id;
    private Long userId;
    private String username;
    private Integer alertsCreated;
    private Integer alertsVerified;
    private Integer commentsPosted;
    private Integer upvotesReceived;
    private Integer downvotesReceived;
    private Integer reportsSubmitted;
    private Integer validReports;
    private Integer reputationPoints;
    private Integer level;
    private LocalDateTime lastAlertAt;
    private LocalDateTime lastCommentAt;
    private Integer badgeCount;
}
