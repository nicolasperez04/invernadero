package com.invernadero.proyecto.Dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LotProgressDTO {

    private Long lotId;
    private String lotName;
    private double progress;
    private String estimatedHarvestDate;

    private String sowingDate;
    private int totalDays;
    private int daysElapsed;
    private int daysRemaining;


}
