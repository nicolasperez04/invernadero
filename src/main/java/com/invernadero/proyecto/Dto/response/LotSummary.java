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
public class LotSummary {

    private Long lotId;
    private String lotName;
    private String status;
    private String inactivityStatus;
    private Long totalEvents;
    private Long durationDays;
    private Double eventFrequency;

    // Campos nuevos desde getLotProgressDetails
    private String sowingDate;          // "null" si no hay
    private int totalDays;
    private int daysElapsed;
    private int daysRemaining;
    private String estimatedHarvestDate; // "null" si no hay
    // Utilidad
    private Instant lastEventDate;
    private String lastEventType;



}
