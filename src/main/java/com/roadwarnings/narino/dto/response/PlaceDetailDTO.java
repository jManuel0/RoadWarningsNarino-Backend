package com.roadwarnings.narino.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaceDetailDTO {

    private String id;
    private String name;
    private String address;
    private Double lat;
    private Double lng;
    private Double rating;
    private Integer totalReviews;
    private String phone;
    private String website;
    private String hours;
    private List<String> photos;
    private String category;
}
