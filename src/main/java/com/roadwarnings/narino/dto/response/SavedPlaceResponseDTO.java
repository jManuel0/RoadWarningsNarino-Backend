package com.roadwarnings.narino.dto.response;

import com.roadwarnings.narino.enums.SavedPlaceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SavedPlaceResponseDTO {

    private Long id;
    private Long userId;
    private String name;
    private String address;
    private Double lat;
    private Double lng;
    private SavedPlaceType type;
    private LocalDateTime createdAt;
}
