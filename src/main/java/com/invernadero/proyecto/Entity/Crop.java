package com.invernadero.proyecto.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Entidad que representa un cultivo en el sistema de invernaderos.
 * Cada cultivo tiene un nombre, descripción, umbral de días de inactividad
 * y una estimación de días hasta la cosecha.
 */
@Schema(description = "Cultivo registrado en el sistema de invernaderos")
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "crops")
public class Crop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Identificador único del cultivo", example = "1")
    private Long id;

    @Column(nullable = false)
    @Schema(description = "Nombre del cultivo", example = "Tomate cherry")
    private String name;

    @Schema(description = "Descripción opcional del cultivo", example = "Variedad dulce de tomate para exportación")
    private String description;

    @Column(nullable = false)
    @Schema(description = "Días máximos de inactividad antes de alertar", example = "7")
    private Integer inactivityDaysThreshold;

    @Column(nullable = false)
    @Schema(description = "Días estimados desde siembra hasta cosecha", example = "90")
    private Integer estimatedGrowthDays;

    @Column(name = "irrigation_frequency_hours")
    @Schema(description = "Cada cuántas horas se debe regar automáticamente (0 = sin riego automático)", example = "48")
    private Integer irrigationFrequencyHours;

    @Column(name = "recommended_fertilization_days")
    @Schema(description = "Días recomendados entre fertilizaciones", example = "15")
    private Integer recommendedFertilizationDays;

    @Column(name = "recommended_pest_control_days")
    @Schema(description = "Días recomendados entre controles de plagas", example = "30")
    private Integer recommendedPestControlDays;

    @OneToMany(
            mappedBy = "crop", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true
    )
    @JsonIgnore
    @Schema(description = "Lotes asociados a este cultivo")
    private List<Lot> lots;

    @OneToMany(
            mappedBy = "crop", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true
    )
    @JsonIgnore
    @Schema(description = "Tipos de evento disponibles para este cultivo")
    private List<CropEventType> cropEventTypes;

}
