package com.invernadero.proyecto.Dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EventResponse {

    private Long id;

    private Long lotId;
    private String lotName;

    private String type;
    private String category;

    private Long userId;
    private String userName;

    private Instant timestamp;
    private String description;

    private Instant createdAt;

}
