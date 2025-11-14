package com.roadwarnings.narino.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GasStationFilterDTO {

    private String brand;
    private String municipality;
    private Boolean isAvailable;
    private Boolean hasGasoline;
    private Boolean hasDiesel;
    private Boolean isOpen24Hours;
    private Double maxGasolinePrice;
    private Double maxDieselPrice;
    private Double latitude;
    private Double longitude;
    private Double radiusKm;
}
