package com.roadwarnings.narino.dto.response;

import com.roadwarnings.narino.enums.BadgeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserBadgeResponseDTO {

    private Long id;
    private Long userId;
    private String username;
    private BadgeType badgeType;
    private String badgeName;
    private String badgeDescription;
    private LocalDateTime earnedAt;
}
