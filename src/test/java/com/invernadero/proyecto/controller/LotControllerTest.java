package com.invernadero.proyecto.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.invernadero.proyecto.Dto.Request.LotRequest;
import com.invernadero.proyecto.Dto.response.LotResponse;
import com.invernadero.proyecto.Dto.response.LotSummary;
import com.invernadero.proyecto.Security.JwtAuthenticationFilter;
import com.invernadero.proyecto.Security.JwtService;
import com.invernadero.proyecto.Service.LotService;
import com.invernadero.proyecto.Service.PdfReportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.hamcrest.Matchers;

@WebMvcTest(LotController.class)
@AutoConfigureMockMvc(addFilters = false)
class LotControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LotService lotService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private PdfReportService pdfReportService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createLot_success() throws Exception {
        LotRequest request = LotRequest.builder()
                .name("Lot A")
                .cropId(1L)
                .startDate(Instant.parse("2024-01-01T00:00:00Z"))
                .build();

        when(lotService.createLot(request))
                .thenReturn(LotResponse.builder().id(1L).name("Lot A").cropId(1L).cropName("Tomato").status("CREATED").build());

        mockMvc.perform(post("/api/lots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Lot A"))
                .andExpect(jsonPath("$.status").value("CREATED"));
    }

    @Test
    void createLot_invalid_shouldReturn400() throws Exception {
        mockMvc.perform(post("/api/lots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getLotById_success() throws Exception {
        when(lotService.getLotById(1L))
                .thenReturn(LotResponse.builder().id(1L).name("Lot A").cropId(1L).cropName("Tomato").status("IN_PRODUCTION").build());

        mockMvc.perform(get("/api/lots/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("IN_PRODUCTION"));
    }

    @Test
    void getAllLots_success() throws Exception {
        when(lotService.getAllLots(null)).thenReturn(List.of(
                LotResponse.builder().id(1L).name("Lot A").status("CREATED").build(),
                LotResponse.builder().id(2L).name("Lot B").status("IN_PRODUCTION").build()
        ));

        mockMvc.perform(get("/api/lots"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].status").value("CREATED"))
                .andExpect(jsonPath("$[1].status").value("IN_PRODUCTION"));
    }

    @Test
    void getAllLots_filterByStatus() throws Exception {
        when(lotService.getAllLots("IN_PRODUCTION")).thenReturn(List.of(
                LotResponse.builder().id(1L).name("Lot A").status("IN_PRODUCTION").build()
        ));

        mockMvc.perform(get("/api/lots?status=IN_PRODUCTION"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("IN_PRODUCTION"));
    }

    @Test
    void getLotsByCrop_success() throws Exception {
        when(lotService.getLotsByCrop(1L)).thenReturn(List.of(
                LotResponse.builder().id(1L).name("Lot A").status("CREATED").build()
        ));

        mockMvc.perform(get("/api/lots/crop/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void updateLot_success() throws Exception {
        when(lotService.updateLot(1L, LotRequest.builder().name("New").build()))
                .thenReturn(LotResponse.builder().id(1L).name("New").status("CREATED").build());

        mockMvc.perform(put("/api/lots/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"New\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New"))
                .andExpect(jsonPath("$.status").value("CREATED"));
    }

    @Test
    void deleteLot_success() throws Exception {
        doNothing().when(lotService).deleteLot(1L);

        mockMvc.perform(delete("/api/lots/1"))
                .andExpect(status().isOk());
    }

    @Test
    void downloadReport_success() throws Exception {
        byte[] pdfBytes = "%PDF-1.4 test content".getBytes();
        when(pdfReportService.generateLotReport(1L)).thenReturn(pdfBytes);

        mockMvc.perform(get("/api/lots/1/report"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"))
                .andExpect(header().string("Content-Disposition", Matchers.containsString("attachment; filename=\"informe_cosecha_1_")))
                .andExpect(content().bytes(pdfBytes));
    }

    @Test
    void getSummary_success() throws Exception {
        when(lotService.getLotSummary(1L)).thenReturn(LotSummary.builder()
                .lotId(1L)
                .lotName("Lot A")
                .status("IN_PRODUCTION")
                .totalEvents(3L)
                .durationDays(10L)
                .eventFrequency(0.3)
                .build());

        mockMvc.perform(get("/api/lots/1/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PRODUCTION"))
                .andExpect(jsonPath("$.totalEvents").value(3));
    }
}

