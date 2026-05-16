package com.invernadero.proyecto.Dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Schema(description = "Respuesta con detalles de un evento registrado en el invernadero")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EventResponse {

    @Schema(description = "Identificador único del evento", example = "1")
    private Long id;

    @Schema(description = "Identificador del lote asociado", example = "3")
    private Long lotId;
    @Schema(description = "Nombre del lote", example = "Lote Norte A")
    private String lotName;

    @Schema(description = "Tipo de evento", example = "RIEGO")
    private String type;
    @Schema(description = "Categoría del evento", example = "MANTENIMIENTO")
    private String category;

    @Schema(description = "Identificador del usuario que registró el evento", example = "2")
    private Long userId;
    @Schema(description = "Nombre del usuario", example = "Juan Pérez")
    private String userName;

    @Schema(description = "Fecha y hora del evento (formato ISO 8601)", example = "2025-01-15T10:30:00Z")
    private Instant timestamp;
    @Schema(description = "Descripción del evento", example = "Riego automático completado")
    private String description;

    @Schema(description = "Fecha de creación del registro", example = "2025-01-15T10:30:00Z")
    private Instant createdAt;

}
