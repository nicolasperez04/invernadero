package com.invernadero.proyecto.Dto.Request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Datos para registrar un evento en un lote de cultivo")
public class EventRequest {

    @NotNull(message = "{validation.event.lotId.required}")
    @Schema(description = "ID del lote", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long lotId;

    @NotNull(message = "{validation.event.type.required}")
    @Schema(description = "Tipo de evento (SOWING, HARVEST, WATERING...)", example = "SOWING", requiredMode = Schema.RequiredMode.REQUIRED)
    private String type;

    @NotNull(message = "{validation.event.userId.required}")
    @Schema(description = "ID del usuario que registra", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long userId;

    @NotNull(message = "{validation.event.timestamp.required}")
    @Schema(description = "Fecha y hora del evento", example = "2026-01-15T10:30:00Z", requiredMode = Schema.RequiredMode.REQUIRED)
    private Instant timestamp;

    @NotNull(message = "{validation.event.description.required}")
    @Schema(description = "Descripción o notas del evento", example = "Siembra de semillas a 2cm de profundidad")
    private String description;
}
