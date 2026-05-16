package com.invernadero.proyecto.Dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Datos de un lote de cultivo")
public class LotResponse {

    @Schema(description = "ID del lote", example = "1")
    private Long id;

    @Schema(description = "Nombre del lote", example = "Lote Norte A")
    private String name;

    @Schema(description = "ID del cultivo asociado", example = "1")
    private Long cropId;

    @Schema(description = "Nombre del cultivo asociado", example = "Tomate")
    private String cropName;

    @Schema(description = "Fecha de inicio", example = "2026-01-15T10:30:00Z")
    private Instant startDate;

    @Schema(description = "Fecha de fin o cosecha", example = "2026-04-15T10:30:00Z")
    private Instant endDate;

    @Schema(description = "Estado del lote: CREATED, IN_PRODUCTION, FINISHED", example = "IN_PRODUCTION")
    private String status;

}
