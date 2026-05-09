package com.invernadero.proyecto.Dto.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LotRequest {

    @NotBlank(message = "{validation.lot.name.required}")
    private String name;

    @NotNull(message = "{validation.lot.cropId.required}")
    private Long cropId;

    @NotNull(message = "{validation.lot.startDate.required}")
    private Instant startDate;

    private Instant endDate;
}
