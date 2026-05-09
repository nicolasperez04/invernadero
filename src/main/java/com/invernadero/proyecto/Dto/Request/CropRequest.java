package com.invernadero.proyecto.Dto.Request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CropRequest {

    @NotBlank(message = "{validation.crop.name.required}")
    private String name;
    private String description;
    private Integer inactivityDaysThreshold;
    private Integer estimatedGrowthDays;

}
