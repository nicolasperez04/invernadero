package com.invernadero.proyecto.Dto.Request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Datos para crear o actualizar un cultivo")
public class CropRequest {

    @NotBlank(message = "{validation.crop.name.required}")
    @Schema(description = "Nombre del cultivo (único)", example = "Tomate", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "Descripción del cultivo", example = "Variedad de tomate cherry para invernadero")
    private String description;

    @NotNull(message = "{validation.crop.inactivityDaysThreshold.required}")
    @Schema(description = "Días máximos de inactividad antes de generar alerta", example = "7", minimum = "1")
    private Integer inactivityDaysThreshold;

    @NotNull(message = "{validation.crop.estimatedGrowthDays.required}")
    @Schema(description = "Días estimados para la cosecha desde la siembra", example = "90", minimum = "1")
    private Integer estimatedGrowthDays;

    @Schema(description = "Frecuencia de riego automático en horas", example = "48")
    private Integer irrigationFrequencyHours;

    @Schema(description = "Días recomendados entre fertilizaciones", example = "15")
    private Integer recommendedFertilizationDays;

    @Schema(description = "Días recomendados entre controles de plagas", example = "30")
    private Integer recommendedPestControlDays;

}
