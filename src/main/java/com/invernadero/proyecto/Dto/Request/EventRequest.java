package com.invernadero.proyecto.Dto.Request;

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
public class EventRequest {

    @NotNull(message = "{validation.event.lotId.required}")
    private Long lotId;

    @NotNull(message = "{validation.event.type.required}")
    private String type;       // "SIEMBRA", "COSECHA", etc. EN INGLES

    @NotNull(message = "{validation.event.userId.required}")
    private Long userId;

    @NotNull(message = "{validation.event.timestamp.required}")
    private Instant timestamp;

    @NotNull(message = "{validation.event.description.required}")
    private String description;
}
