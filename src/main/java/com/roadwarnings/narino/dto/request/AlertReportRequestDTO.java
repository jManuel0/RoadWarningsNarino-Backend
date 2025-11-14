package com.roadwarnings.narino.dto.request;

import com.roadwarnings.narino.enums.ReportReason;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertReportRequestDTO {

    @NotNull(message = "El ID de la alerta es requerido")
    private Long alertId;

    @NotNull(message = "El motivo del reporte es requerido")
    private ReportReason reason;

    private String description;
}
