package com.roadwarnings.narino.dto.request;

import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GasStationRequestDTO {

    private String name;
    private String brand;
    private Double latitude;
    private Double longitude;
    private String address;
    private String municipality;
    private String phoneNumber;
    private Boolean hasGasoline;
    private Boolean hasDiesel;
    private BigDecimal gasolinePrice;
    private BigDecimal dieselPrice;
    private Boolean isOpen24Hours;
    private String openingTime;
    private String closingTime;
    private Boolean isAvailable;
}
