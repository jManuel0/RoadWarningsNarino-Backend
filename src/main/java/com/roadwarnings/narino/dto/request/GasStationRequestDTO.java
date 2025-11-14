package com.roadwarnings.narino.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GasStationRequestDTO {

    @NotBlank(message = "El nombre de la estación es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String name;

    @Size(max = 50, message = "La marca no puede exceder 50 caracteres")
    private String brand;

    @NotNull(message = "La latitud es obligatoria")
    @DecimalMin(value = "-90.0", message = "La latitud debe estar entre -90 y 90")
    @DecimalMax(value = "90.0", message = "La latitud debe estar entre -90 y 90")
    private Double latitude;

    @NotNull(message = "La longitud es obligatoria")
    @DecimalMin(value = "-180.0", message = "La longitud debe estar entre -180 y 180")
    @DecimalMax(value = "180.0", message = "La longitud debe estar entre -180 y 180")
    private Double longitude;

    @NotBlank(message = "La dirección es obligatoria")
    @Size(max = 200, message = "La dirección no puede exceder 200 caracteres")
    private String address;

    @NotBlank(message = "El municipio es obligatorio")
    @Size(max = 100, message = "El municipio no puede exceder 100 caracteres")
    private String municipality;

    @Pattern(regexp = "^[0-9]{10}$|^[0-9]{7}$", message = "El teléfono debe tener 7 o 10 dígitos")
    private String phoneNumber;

    private Boolean hasGasoline;

    private Boolean hasDiesel;

    @DecimalMin(value = "0.0", inclusive = false, message = "El precio de la gasolina debe ser mayor a 0")
    private BigDecimal gasolinePrice;

    @DecimalMin(value = "0.0", inclusive = false, message = "El precio del diesel debe ser mayor a 0")
    private BigDecimal dieselPrice;

    private Boolean isOpen24Hours;

    @Pattern(regexp = "^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$", message = "El horario de apertura debe tener formato HH:mm")
    private String openingTime;

    @Pattern(regexp = "^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$", message = "El horario de cierre debe tener formato HH:mm")
    private String closingTime;

    private Boolean isAvailable;
}
