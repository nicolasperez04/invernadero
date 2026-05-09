package com.invernadero.proyecto.service;

import com.invernadero.proyecto.Entity.EventType;
import com.invernadero.proyecto.Repository.EventTypeRepository;
import com.invernadero.proyecto.Service.EventTypeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventTypeServiceTest {

    @Mock
    private EventTypeRepository eventTypeRepository;

    @InjectMocks
    private EventTypeService eventTypeService;

    @Test
    void getAllEventTypes_success() {
        when(eventTypeRepository.findAll()).thenReturn(List.of(
                EventType.builder().id(1L).name("SOWING").category("CYCLE").build(),
                EventType.builder().id(2L).name("HARVEST").category("CYCLE").build()
        ));

        List<EventType> response = eventTypeService.getAllEventTypes();

        assertEquals(2, response.size());
    }

    @Test
    void getEventTypeById_success() {
        EventType eventType = EventType.builder().id(1L).name("SOWING").category("CYCLE").build();
        when(eventTypeRepository.findById(1L)).thenReturn(Optional.of(eventType));

        EventType response = eventTypeService.getEventTypeById(1L);

        assertEquals("SOWING", response.getName());
    }

    @Test
    void getEventTypeById_notFound() {
        when(eventTypeRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> eventTypeService.getEventTypeById(1L));

        assertEquals("Event type not found", ex.getMessage());
    }

    @Test
    void getEventTypeByName_success() {
        EventType eventType = EventType.builder().id(2L).name("HARVEST").category("CYCLE").build();
        when(eventTypeRepository.findByName("HARVEST")).thenReturn(Optional.of(eventType));

        EventType response = eventTypeService.getEventTypeByName("HARVEST");

        assertEquals(2L, response.getId());
    }

    @Test
    void getEventTypeByName_notFound() {
        when(eventTypeRepository.findByName("UNKNOWN")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> eventTypeService.getEventTypeByName("UNKNOWN"));

        assertEquals("Event type not found", ex.getMessage());
    }
}

