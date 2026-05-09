package com.invernadero.proyecto.service;

import com.invernadero.proyecto.Dto.Request.EventRequest;
import com.invernadero.proyecto.Dto.response.EventResponse;
import com.invernadero.proyecto.Entity.Crop;
import com.invernadero.proyecto.Entity.Event;
import com.invernadero.proyecto.Entity.EventType;
import com.invernadero.proyecto.Entity.Lot;
import com.invernadero.proyecto.Entity.User;
import com.invernadero.proyecto.Repository.EventRepository;
import com.invernadero.proyecto.Repository.EventTypeRepository;
import com.invernadero.proyecto.Repository.LotRepository;
import com.invernadero.proyecto.Repository.UserRepository;
import com.invernadero.proyecto.Service.EventService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private LotRepository lotRepository;

    @Mock
    private EventTypeRepository eventTypeRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private EventService eventService;

    @Test
    void registerEvent_success() {
        EventRequest request = EventRequest.builder()
                .lotId(1L)
                .type("SOWING")
                .userId(10L)
                .timestamp(Instant.parse("2024-01-01T00:00:00Z"))
                .description("Sowing done")
                .build();

        Crop crop = Crop.builder().id(1L).name("Tomato").estimatedGrowthDays(60).inactivityDaysThreshold(7).build();
        Lot lot = Lot.builder().id(1L).name("Lot A").crop(crop).build();
        EventType type = EventType.builder().id(5L).name("SOWING").category("CYCLE").build();
        User user = User.builder().id(10L).name("Ana").build();

        Event saved = Event.builder()
                .id(100L)
                .lot(lot)
                .type(type)
                .user(user)
                .timestamp(request.getTimestamp())
                .description(request.getDescription())
                .createdAt(Instant.parse("2024-01-01T01:00:00Z"))
                .build();

        when(lotRepository.findById(1L)).thenReturn(Optional.of(lot));
        when(eventTypeRepository.findByName("SOWING")).thenReturn(Optional.of(type));
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(eventRepository.existsByLotIdAndTypeName(1L, "SOWING")).thenReturn(false);
        when(eventRepository.existsByLotIdAndTypeName(1L, "HARVEST")).thenReturn(false);
        when(eventRepository.save(any(Event.class))).thenReturn(saved);

        EventResponse response = eventService.registerEvent(request);

        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals("SOWING", response.getType());
        assertEquals(1L, response.getLotId());

        verify(eventRepository).save(any(Event.class));
    }

    @Test
    void registerEvent_lotNotFound() {
        EventRequest request = EventRequest.builder()
                .lotId(1L)
                .type("SOWING")
                .userId(10L)
                .timestamp(Instant.parse("2024-01-01T00:00:00Z"))
                .description("Sowing done")
                .build();

        when(lotRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> eventService.registerEvent(request));

        assertEquals("Lot not found", ex.getMessage());
        verifyNoInteractions(eventTypeRepository, userRepository, eventRepository);
    }

    @Test
    void registerEvent_eventTypeNotFound() {
        EventRequest request = EventRequest.builder()
                .lotId(1L)
                .type("SOWING")
                .userId(10L)
                .timestamp(Instant.parse("2024-01-01T00:00:00Z"))
                .description("Sowing done")
                .build();

        when(lotRepository.findById(1L)).thenReturn(Optional.of(Lot.builder().id(1L).name("Lot A").build()));
        when(eventTypeRepository.findByName("SOWING")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> eventService.registerEvent(request));

        assertEquals("Event type not found", ex.getMessage());
        verifyNoInteractions(userRepository, eventRepository);
    }

    @Test
    void registerEvent_userNotFound() {
        EventRequest request = EventRequest.builder()
                .lotId(1L)
                .type("SOWING")
                .userId(10L)
                .timestamp(Instant.parse("2024-01-01T00:00:00Z"))
                .description("Sowing done")
                .build();

        when(lotRepository.findById(1L)).thenReturn(Optional.of(Lot.builder().id(1L).name("Lot A").build()));
        when(eventTypeRepository.findByName("SOWING")).thenReturn(Optional.of(EventType.builder().id(5L).name("SOWING").category("CYCLE").build()));
        when(userRepository.findById(10L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> eventService.registerEvent(request));

        assertEquals("User not found", ex.getMessage());
        verifyNoInteractions(eventRepository);
    }

    @Test
    void registerEvent_harvestBeforeSowing_shouldFail() {
        EventRequest request = EventRequest.builder()
                .lotId(1L)
                .type("HARVEST")
                .userId(10L)
                .timestamp(Instant.parse("2024-01-02T00:00:00Z"))
                .description("Harvest")
                .build();

        when(lotRepository.findById(1L)).thenReturn(Optional.of(Lot.builder().id(1L).name("Lot A").build()));
        when(eventTypeRepository.findByName("HARVEST")).thenReturn(Optional.of(EventType.builder().id(6L).name("HARVEST").category("CYCLE").build()));
        when(userRepository.findById(10L)).thenReturn(Optional.of(User.builder().id(10L).name("Ana").build()));
        when(eventRepository.existsByLotIdAndTypeName(1L, "SOWING")).thenReturn(false);
        when(eventRepository.existsByLotIdAndTypeName(1L, "HARVEST")).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> eventService.registerEvent(request));

        assertEquals("Cannot register harvest before sowing", ex.getMessage());
        verify(eventRepository, never()).save(any());
    }

    @Test
    void registerEvent_sowingAlreadyExists_shouldFail() {
        EventRequest request = EventRequest.builder()
                .lotId(1L)
                .type("SOWING")
                .userId(10L)
                .timestamp(Instant.parse("2024-01-02T00:00:00Z"))
                .description("Sowing again")
                .build();

        when(lotRepository.findById(1L)).thenReturn(Optional.of(Lot.builder().id(1L).name("Lot A").build()));
        when(eventTypeRepository.findByName("SOWING")).thenReturn(Optional.of(EventType.builder().id(5L).name("SOWING").category("CYCLE").build()));
        when(userRepository.findById(10L)).thenReturn(Optional.of(User.builder().id(10L).name("Ana").build()));
        when(eventRepository.existsByLotIdAndTypeName(1L, "SOWING")).thenReturn(true);
        when(eventRepository.existsByLotIdAndTypeName(1L, "HARVEST")).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> eventService.registerEvent(request));

        assertEquals("Sowing already exists for this lot", ex.getMessage());
        verify(eventRepository, never()).save(any());
    }

    @Test
    void registerEvent_lotFinished_shouldFail() {
        EventRequest request = EventRequest.builder()
                .lotId(1L)
                .type("FERTILIZATION")
                .userId(10L)
                .timestamp(Instant.parse("2024-01-02T00:00:00Z"))
                .description("Fertilization")
                .build();

        when(lotRepository.findById(1L)).thenReturn(Optional.of(Lot.builder().id(1L).name("Lot A").build()));
        when(eventTypeRepository.findByName("FERTILIZATION")).thenReturn(Optional.of(EventType.builder().id(7L).name("FERTILIZATION").category("CARE").build()));
        when(userRepository.findById(10L)).thenReturn(Optional.of(User.builder().id(10L).name("Ana").build()));
        when(eventRepository.existsByLotIdAndTypeName(1L, "SOWING")).thenReturn(true);
        when(eventRepository.existsByLotIdAndTypeName(1L, "HARVEST")).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> eventService.registerEvent(request));

        assertEquals("This lot is already finished", ex.getMessage());
        verify(eventRepository, never()).save(any());
    }

    @Test
    void getEventById_success() {
        Event event = buildEvent(200L, "SOWING");

        when(eventRepository.findById(200L)).thenReturn(Optional.of(event));

        EventResponse response = eventService.getEventById(200L);

        assertEquals(200L, response.getId());
        assertEquals("SOWING", response.getType());
    }

    @Test
    void getEventById_notFound() {
        when(eventRepository.findById(200L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> eventService.getEventById(200L));

        assertEquals("Event not found", ex.getMessage());
    }

    @Test
    void getAllEvents_success() {
        when(eventRepository.findAll()).thenReturn(List.of(
                buildEvent(1L, "SOWING"),
                buildEvent(2L, "HARVEST")
        ));

        List<EventResponse> response = eventService.getAllEvents();

        assertEquals(2, response.size());
    }

    @Test
    void getEventsByLot_success() {
        when(eventRepository.findByLotIdOrderByTimestampAsc(1L)).thenReturn(List.of(
                buildEvent(1L, "SOWING"),
                buildEvent(2L, "HARVEST")
        ));

        List<EventResponse> response = eventService.getEventsByLot(1L);

        assertEquals(2, response.size());
    }

    @Test
    void getEventHistoryByLot_success() {
        List<Event> events = List.of(buildEvent(1L, "SOWING"));
        when(eventRepository.findByLotIdOrderByTimestampAsc(1L)).thenReturn(events);

        List<Event> response = eventService.getEventHistoryByLot(1L);

        assertEquals(events, response);
    }

    @Test
    void filterEvents_success() {
        List<Event> events = List.of(buildEvent(1L, "SOWING"));
        when(eventRepository.filterEvents(1L, "SOWING", null, null)).thenReturn(events);

        List<Event> response = eventService.filterEvents(1L, "SOWING", null, null);

        assertEquals(events, response);
    }

    private Event buildEvent(Long id, String typeName) {
        Lot lot = Lot.builder().id(1L).name("Lot A").build();
        EventType type = EventType.builder().id(5L).name(typeName).category("CYCLE").build();
        User user = User.builder().id(10L).name("Ana").build();

        return Event.builder()
                .id(id)
                .lot(lot)
                .type(type)
                .user(user)
                .timestamp(Instant.parse("2024-01-01T00:00:00Z"))
                .description("Desc")
                .createdAt(Instant.parse("2024-01-01T01:00:00Z"))
                .build();
    }
}

