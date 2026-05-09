package com.invernadero.proyecto.mapper;

import com.invernadero.proyecto.Dto.response.EventResponse;
import com.invernadero.proyecto.Entity.Event;

public class EventMapper {

    public static EventResponse toDTO(Event event) {

        return EventResponse.builder()
                .id(event.getId())
                .lotId(event.getLot().getId())
                .lotName(event.getLot().getName())
                .type(event.getType().getName())
                .category(event.getType().getCategory())
                .userId(event.getUser().getId())
                .userName(event.getUser().getName())
                .timestamp(event.getTimestamp())
                .description(event.getDescription())
                .createdAt(event.getCreatedAt())
                .build();
    }

}
