package com.invernadero.proyecto.Dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpcomingHarvestDTO {

    private Long lotId;
    private String lotName;
    private String estimatedHarvestDate;
    private long daysRemaining;
}
