package com.invernadero.proyecto.mapper;

import com.invernadero.proyecto.Dto.response.LotResponse;
import com.invernadero.proyecto.Entity.Lot;

public class LotMapper {

    public static LotResponse toDTO(Lot lot) {
        return LotResponse.builder()
                .id(lot.getId())
                .name(lot.getName())
                .cropId(lot.getCrop().getId())
                .cropName(lot.getCrop().getName())
                .startDate(lot.getStartDate())
                .endDate(lot.getEndDate())
                .build();
    }

}
