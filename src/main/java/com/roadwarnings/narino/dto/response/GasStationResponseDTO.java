package com.roadwarnings.narino.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GasStationResponseDTO {

    private Long id;
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
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
