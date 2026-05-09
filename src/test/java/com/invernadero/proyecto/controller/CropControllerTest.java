package com.invernadero.proyecto.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.invernadero.proyecto.Dto.Request.CropRequest;
import com.invernadero.proyecto.Dto.response.CropResponse;
import com.invernadero.proyecto.Security.JwtAuthenticationFilter;
import com.invernadero.proyecto.Security.JwtService;
import com.invernadero.proyecto.Service.CropService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CropController.class)
@AutoConfigureMockMvc(addFilters = false)
class CropControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CropService cropService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void create_success() throws Exception {
        CropRequest request = CropRequest.builder()
                .name("Tomato")
                .description("Red")
                .build();

        when(cropService.createCrop(any(CropRequest.class)))
                .thenReturn(CropResponse.builder().id(1L).name("Tomato").description("Red").build());

        mockMvc.perform(post("/api/crops")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Tomato"));
    }

    @Test
    void create_invalid_shouldReturn400() throws Exception {
        mockMvc.perform(post("/api/crops")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getById_success() throws Exception {
        when(cropService.getCropById(1L))
                .thenReturn(CropResponse.builder().id(1L).name("Tomato").build());

        mockMvc.perform(get("/api/crops/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Tomato"));
    }

    @Test
    void getAll_success() throws Exception {
        when(cropService.getAllCrops()).thenReturn(List.of(
                CropResponse.builder().id(1L).name("Tomato").build(),
                CropResponse.builder().id(2L).name("Potato").build()
        ));

        mockMvc.perform(get("/api/crops"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void update_success() throws Exception {
        CropRequest request = CropRequest.builder().name("New").description("New desc").build();

        when(cropService.updateCrop(1L, request))
                .thenReturn(CropResponse.builder().id(1L).name("New").description("New desc").build());

        mockMvc.perform(put("/api/crops/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New"));
    }

    @Test
    void delete_success() throws Exception {
        doNothing().when(cropService).deleteCrop(1L);

        mockMvc.perform(delete("/api/crops/1"))
                .andExpect(status().isOk());
    }
}

