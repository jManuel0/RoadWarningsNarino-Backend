package com.roadwarnings.narino.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaceSearchResultDTO {

    private String id;
    private String name;
    private String address;
    private Double lat;
    private Double lng;
    private String type;
    private Double rating;
    private Integer totalReviews;
    private Double distance; // en km
}
