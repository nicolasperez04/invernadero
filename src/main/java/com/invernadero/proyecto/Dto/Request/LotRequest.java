package com.invernadero.proyecto.Dto.Request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
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
@Schema(description = "Datos para crear o actualizar un lote de cultivo")
public class LotRequest {

    @NotBlank(message = "{validation.lot.name.required}")
    @Schema(description = "Nombre del lote", example = "Lote Norte A", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @NotNull(message = "{validation.lot.cropId.required}")
    @Schema(description = "ID del cultivo asociado", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long cropId;

    @NotNull(message = "{validation.lot.startDate.required}")
    @Schema(description = "Fecha de inicio del lote", example = "2026-01-15T10:30:00Z", requiredMode = Schema.RequiredMode.REQUIRED)
    private Instant startDate;

    @Schema(description = "Fecha de fin o cosecha estimada", example = "2026-04-15T10:30:00Z")
    private Instant endDate;
}
