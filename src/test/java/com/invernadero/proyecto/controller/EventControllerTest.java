package com.invernadero.proyecto.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.invernadero.proyecto.Dto.Request.EventRequest;
import com.invernadero.proyecto.Dto.response.EventResponse;
import com.invernadero.proyecto.Entity.Event;
import com.invernadero.proyecto.Entity.EventType;
import com.invernadero.proyecto.Entity.Lot;
import com.invernadero.proyecto.Entity.User;
import com.invernadero.proyecto.Security.JwtAuthenticationFilter;
import com.invernadero.proyecto.Security.JwtService;
import com.invernadero.proyecto.Service.EventService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EventController.class)
@AutoConfigureMockMvc(addFilters = false)
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EventService eventService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createEvent_success() throws Exception {
        EventRequest request = EventRequest.builder()
                .lotId(1L)
                .type("SOWING")
                .userId(10L)
                .timestamp(Instant.parse("2024-01-01T00:00:00Z"))
                .description("Sowing")
                .build();

        when(eventService.registerEvent(any(EventRequest.class)))
                .thenReturn(EventResponse.builder().id(100L).lotId(1L).type("SOWING").build());

        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.type").value("SOWING"));
    }

    @Test
    void createEvent_invalid_shouldReturn400() throws Exception {
        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getEventById_success() throws Exception {
        when(eventService.getEventById(1L))
                .thenReturn(EventResponse.builder().id(1L).type("SOWING").build());

        mockMvc.perform(get("/api/events/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getAllEvents_success() throws Exception {
        when(eventService.getAllEvents()).thenReturn(List.of(
                EventResponse.builder().id(1L).type("SOWING").build(),
                EventResponse.builder().id(2L).type("HARVEST").build()
        ));

        mockMvc.perform(get("/api/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getEventsByLot_success() throws Exception {
        when(eventService.getEventsByLot(1L)).thenReturn(List.of(
                EventResponse.builder().id(1L).type("SOWING").build()
        ));

        mockMvc.perform(get("/api/events/lot/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getEventHistory_success() throws Exception {
        when(eventService.getEventsByLot(1L)).thenReturn(List.of(
                EventResponse.builder().id(1L).type("SOWING").build()
        ));

        mockMvc.perform(get("/api/events/lot/1/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void filterEvents_success() throws Exception {
        Event event = buildEvent(1L, "SOWING");
        when(eventService.filterEvents(eq(1L), eq("SOWING"), any(), any()))
                .thenReturn(List.of(event));

        mockMvc.perform(get("/api/events/filter")
                        .param("lotId", "1")
                        .param("type", "SOWING")
                        .param("startDate", "2024-01-01T00:00:00Z")
                        .param("endDate", "2024-02-01T00:00:00Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].type").value("SOWING"));
    }

    private Event buildEvent(Long id, String typeName) {
        Lot lot = Lot.builder().id(1L).name("Lot A").build();
        EventType type = EventType.builder().id(2L).name(typeName).category("CYCLE").build();
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
