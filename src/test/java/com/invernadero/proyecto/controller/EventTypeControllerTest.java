package com.invernadero.proyecto.controller;

import com.invernadero.proyecto.Entity.EventType;
import com.invernadero.proyecto.Security.JwtAuthenticationFilter;
import com.invernadero.proyecto.Security.JwtService;
import com.invernadero.proyecto.Service.EventTypeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EventTypeController.class)
@AutoConfigureMockMvc(addFilters = false)
class EventTypeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EventTypeService eventTypeService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void getAllEventTypes_success() throws Exception {
        when(eventTypeService.getAllEventTypes()).thenReturn(List.of(
                EventType.builder().id(1L).name("SOWING").category("CYCLE").build(),
                EventType.builder().id(2L).name("HARVEST").category("CYCLE").build()
        ));

        mockMvc.perform(get("/api/event-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getEventTypeById_success() throws Exception {
        when(eventTypeService.getEventTypeById(1L))
                .thenReturn(EventType.builder().id(1L).name("SOWING").category("CYCLE").build());

        mockMvc.perform(get("/api/event-types/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("SOWING"));
    }

    @Test
    void getEventTypeByName_success() throws Exception {
        when(eventTypeService.getEventTypeByName("SOWING"))
                .thenReturn(EventType.builder().id(1L).name("SOWING").category("CYCLE").build());

        mockMvc.perform(get("/api/event-types/name/SOWING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("SOWING"));
    }
}

