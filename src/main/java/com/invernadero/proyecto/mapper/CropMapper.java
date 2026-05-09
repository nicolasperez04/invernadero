package com.invernadero.proyecto.mapper;

import com.invernadero.proyecto.Dto.response.CropResponse;
import com.invernadero.proyecto.Entity.Crop;

public class CropMapper {

    public static CropResponse toDTO(Crop crop) {

        return CropResponse.builder()
                .id(crop.getId())
                .name(crop.getName())
                .description(crop.getDescription())
                .estimatedGrowthDays(Long.valueOf(crop.getEstimatedGrowthDays()))
                .inactivityDaysThreshold(Long.valueOf(crop.getInactivityDaysThreshold()))
                .build();
    }

}
