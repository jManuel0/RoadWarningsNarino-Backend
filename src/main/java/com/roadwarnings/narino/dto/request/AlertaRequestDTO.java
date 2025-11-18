package com.roadwarnings.narino.dto.request;

import com.roadwarnings.narino.enums.AlertType;
import com.roadwarnings.narino.enums.AlertSeverity;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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

    // Coordenadas (pueden venir del mapa o calcularse desde location)
    private Double latitude;

    private Double longitude;

    // Dirección textual (Ej: "Pasto, Calle 18 con Cra 25")
    private String location;

    // Municipio (Pasto, Ipiales, Tumaco, etc.)
    private String municipality;

    // Severidad (CRITICA, ALTA, MEDIA, BAJA)
    private AlertSeverity severity;

    // URL opcional de imagen
    private String imageUrl;

    // Duración estimada en minutos (opcional)
    private Integer estimatedDuration;

    // Lista de vías afectadas (opcional)
    private List<String> affectedRoads;
}
