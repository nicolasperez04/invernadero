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
public class LotResponse {

    private Long id;
    private String name;

    private Long cropId;
    private String cropName;

    private Instant startDate;
    private Instant endDate;

}
