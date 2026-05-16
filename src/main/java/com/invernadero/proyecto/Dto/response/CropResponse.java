package com.invernadero.proyecto.Dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Datos de un cultivo")
public class CropResponse {

    @Schema(description = "ID del cultivo", example = "1")
    private Long id;

    @Schema(description = "Nombre del cultivo", example = "Tomate")
    private String name;

    @Schema(description = "Descripción del cultivo", example = "Variedad de tomate cherry")
    private String description;

    @Schema(description = "Días de umbral de inactividad", example = "7")
    private Long inactivityDaysThreshold;

    @Schema(description = "Días estimados para la cosecha", example = "90")
    private Long estimatedGrowthDays;

    @Schema(description = "Frecuencia de riego automático en horas", example = "48")
    private Integer irrigationFrequencyHours;

    @Schema(description = "Días recomendados entre fertilizaciones", example = "15")
    private Integer recommendedFertilizationDays;

    @Schema(description = "Días recomendados entre controles de plagas", example = "30")
    private Integer recommendedPestControlDays;

}
