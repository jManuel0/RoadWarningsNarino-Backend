package com.roadwarnings.narino.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteRouteRequestDTO {

    @NotNull(message = "El ID de la ruta es requerido")
    private Long routeId;

    private String customName;

    private Boolean notificationsEnabled;
}
