package com.roadwarnings.narino.dto.request;

import com.roadwarnings.narino.enums.AlertType;
import com.roadwarnings.narino.enums.AlertSeverity;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertaRequestDTO {

    @NotNull(message = "El tipo de alerta es obligatorio")
    private AlertType type;

    @NotBlank(message = "El título es obligatorio")
    @Size(min = 3, max = 100, message = "El título debe tener entre 3 y 100 caracteres")
    private String title;

    @Size(max = 1000, message = "La descripción no puede exceder 1000 caracteres")
    private String description;

    @NotNull(message = "La latitud es obligatoria")
    @DecimalMin(value = "-90.0", message = "Latitud inválida")
    @DecimalMax(value = "90.0", message = "Latitud inválida")
    private Double latitude;

    @NotNull(message = "La longitud es obligatoria")
    @DecimalMin(value = "-180.0", message = "Longitud inválida")
    @DecimalMax(value = "180.0", message = "Longitud inválida")
    private Double longitude;

    // Dirección (texto libre)
    private String location;

    // Municipio seleccionado en el frontend
    private String municipality;

    // Severidad (CRITICA, ALTA, MEDIA, BAJA)
    private AlertSeverity severity;

    // URL opcional
    private String imageUrl;

    // Duración estimada en minutos (opcional)
    private Integer estimatedDuration;

    // Vías afectadas como texto (ej: "Ruta 25; Calle 18")
    private String affectedRoads;
}
