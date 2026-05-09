package com.invernadero.proyecto.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.invernadero.proyecto.Dto.response.DashboardResponse;
import com.invernadero.proyecto.Dto.response.EventChartDTO;
import com.invernadero.proyecto.Dto.response.LotProgressDTO;
import com.invernadero.proyecto.Dto.response.LotStatusDTO;
import com.invernadero.proyecto.Security.JwtAuthenticationFilter;
import com.invernadero.proyecto.Security.JwtService;
import com.invernadero.proyecto.Service.DashboardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DashboardController.class)
@AutoConfigureMockMvc(addFilters = false)
public class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DashboardService dashboardService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private ObjectMapper objectMapper;

    // =============================
    // ✅ GET /api/dashboard - SIN PARAMS
    // =============================
    @Test
    void getDashboard_noParams_returnsOk() throws Exception {
        DashboardResponse response = DashboardResponse.builder()
                .eventChart(EventChartDTO.builder().labels(List.of()).values(List.of()).build())
                .lotStatuses(List.of())
                .lotProgress(List.of())
                .build();

        when(dashboardService.getDashboard(null)).thenReturn(response);

        mockMvc.perform(get("/api/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eventChart").exists())
                .andExpect(jsonPath("$.lotStatuses").isArray())
                .andExpect(jsonPath("$.lotProgress").isArray());
    }

    // =============================
    // ✅ GET /api/dashboard - CON CROP ID
    // =============================
    @Test
    void getDashboard_withCropId_returnsFilteredData() throws Exception {
        DashboardResponse response = DashboardResponse.builder()
                .eventChart(EventChartDTO.builder()
                        .labels(List.of("2026-05-01", "2026-05-02"))
                        .values(List.of(5L, 3L))
                        .build())
                .lotStatuses(List.of(LotStatusDTO.builder()
                        .lotId(1L).lotName("Lote 1").status("IN_PRODUCTION").inactivityLevel("GREEN").build()))
                .lotProgress(List.of(LotProgressDTO.builder()
                        .lotId(1L).lotName("Lote 1").progress(50.0).build()))
                .build();

        when(dashboardService.getDashboard(1L)).thenReturn(response);

        mockMvc.perform(get("/api/dashboard").param("cropId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lotStatuses[0].lotId").value(1))
                .andExpect(jsonPath("$.lotStatuses[0].status").value("IN_PRODUCTION"))
                .andExpect(jsonPath("$.lotStatuses[0].inactivityLevel").value("GREEN"));
    }

    // =============================
    // ✅ GET /api/dashboard/events - SIN PARAMS
    // =============================
    @Test
    void getEvents_noParams_returnsEventChart() throws Exception {
        EventChartDTO chart = EventChartDTO.builder()
                .labels(List.of("2026-05-01", "2026-05-02", "2026-05-03"))
                .values(List.of(10L, 15L, 8L))
                .build();

        DashboardResponse fullResponse = DashboardResponse.builder()
                .eventChart(chart)
                .lotStatuses(List.of())
                .lotProgress(List.of())
                .build();

        when(dashboardService.getDashboard(null)).thenReturn(fullResponse);

        mockMvc.perform(get("/api/dashboard/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.labels").isArray())
                .andExpect(jsonPath("$.values").isArray())
                .andExpect(jsonPath("$.labels.length()").value(3));
    }

    // =============================
    // ✅ GET /api/dashboard/events - CON CROP ID
    // =============================
    @Test
    void getEvents_withCropId_returnsFilteredEvents() throws Exception {
        EventChartDTO chart = EventChartDTO.builder()
                .labels(List.of("2026-05-01", "2026-05-02"))
                .values(List.of(2L, 4L))
                .build();

        DashboardResponse fullResponse = DashboardResponse.builder()
                .eventChart(chart)
                .lotStatuses(List.of())
                .lotProgress(List.of())
                .build();

        when(dashboardService.getDashboard(1L)).thenReturn(fullResponse);

        mockMvc.perform(get("/api/dashboard/events").param("cropId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.values[0]").value(2))
                .andExpect(jsonPath("$.values[1]").value(4));
    }

    // =============================
    // ✅ GET /api/dashboard/status - SIN PARAMS
    // =============================
    @Test
    void getStatus_noParams_returnsLotStatuses() throws Exception {
        List<LotStatusDTO> statuses = List.of(
                LotStatusDTO.builder().lotId(1L).lotName("Lote 1").status("IN_PRODUCTION").inactivityLevel("GREEN").build(),
                LotStatusDTO.builder().lotId(2L).lotName("Lote 2").status("FINISHED").inactivityLevel("GRAY").build()
        );

        DashboardResponse fullResponse = DashboardResponse.builder()
                .eventChart(EventChartDTO.builder().labels(List.of()).values(List.of()).build())
                .lotStatuses(statuses)
                .lotProgress(List.of())
                .build();

        when(dashboardService.getDashboard(null)).thenReturn(fullResponse);

        mockMvc.perform(get("/api/dashboard/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].lotName").value("Lote 1"))
                .andExpect(jsonPath("$[1].status").value("FINISHED"));
    }

    // =============================
    // ✅ GET /api/dashboard/status - CON CROP ID
    // =============================
    @Test
    void getStatus_withCropId_returnsFilteredStatuses() throws Exception {
        List<LotStatusDTO> statuses = List.of(
                LotStatusDTO.builder().lotId(1L).lotName("Lote Tomate").status("IN_PRODUCTION").inactivityLevel("YELLOW").build()
        );

        DashboardResponse fullResponse = DashboardResponse.builder()
                .eventChart(EventChartDTO.builder().labels(List.of()).values(List.of()).build())
                .lotStatuses(statuses)
                .lotProgress(List.of())
                .build();

        when(dashboardService.getDashboard(1L)).thenReturn(fullResponse);

        mockMvc.perform(get("/api/dashboard/status").param("cropId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].inactivityLevel").value("YELLOW"));
    }

    // =============================
    // ✅ GET /api/dashboard/progress - SIN PARAMS
    // =============================
    @Test
    void getProgress_noParams_returnsLotProgress() throws Exception {
        List<LotProgressDTO> progressList = List.of(
                LotProgressDTO.builder()
                        .lotId(1L)
                        .lotName("Lote 1")
                        .progress(50.0)
                        .estimatedHarvestDate("2026-06-01")
                        .sowingDate("2026-04-01")
                        .totalDays(60)
                        .daysElapsed(30)
                        .daysRemaining(30)
                        .build()
        );

        DashboardResponse fullResponse = DashboardResponse.builder()
                .eventChart(EventChartDTO.builder().labels(List.of()).values(List.of()).build())
                .lotStatuses(List.of())
                .lotProgress(progressList)
                .build();

        when(dashboardService.getDashboard(null)).thenReturn(fullResponse);

        mockMvc.perform(get("/api/dashboard/progress"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].progress").value(50.0))
                .andExpect(jsonPath("$[0].totalDays").value(60))
                .andExpect(jsonPath("$[0].daysElapsed").value(30))
                .andExpect(jsonPath("$[0].daysRemaining").value(30));
    }

    // =============================
    // ✅ GET /api/dashboard/progress - CON CROP ID
    // =============================
    @Test
    void getProgress_withCropId_returnsFilteredProgress() throws Exception {
        List<LotProgressDTO> progressList = List.of(
                LotProgressDTO.builder()
                        .lotId(1L)
                        .lotName("Lote Lechuga")
                        .progress(75.0)
                        .estimatedHarvestDate("2026-05-15")
                        .sowingDate("2026-04-15")
                        .totalDays(30)
                        .daysElapsed(22)
                        .daysRemaining(8)
                        .build()
        );

        DashboardResponse fullResponse = DashboardResponse.builder()
                .eventChart(EventChartDTO.builder().labels(List.of()).values(List.of()).build())
                .lotStatuses(List.of())
                .lotProgress(progressList)
                .build();

        when(dashboardService.getDashboard(2L)).thenReturn(fullResponse);

        mockMvc.perform(get("/api/dashboard/progress").param("cropId", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].lotName").value("Lote Lechuga"))
                .andExpect(jsonPath("$[0].progress").value(75.0));
    }

    // =============================
    // ✅ GET /api/dashboard - RESPUESTA COMPLETA
    // =============================
    @Test
    void getDashboard_completeResponseStructure() throws Exception {
        EventChartDTO chart = EventChartDTO.builder()
                .labels(List.of("2026-05-01", "2026-05-02"))
                .values(List.of(5L, 10L))
                .build();

        List<LotStatusDTO> statuses = List.of(
                LotStatusDTO.builder().lotId(1L).lotName("Lote 1").status("IN_PRODUCTION").inactivityLevel("GREEN").build()
        );

        List<LotProgressDTO> progressList = List.of(
                LotProgressDTO.builder()
                        .lotId(1L)
                        .lotName("Lote 1")
                        .progress(33.3)
                        .totalDays(90)
                        .daysElapsed(30)
                        .daysRemaining(60)
                        .build()
        );

        DashboardResponse response = DashboardResponse.builder()
                .eventChart(chart)
                .lotStatuses(statuses)
                .lotProgress(progressList)
                .build();

        when(dashboardService.getDashboard(1L)).thenReturn(response);

        mockMvc.perform(get("/api/dashboard").param("cropId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eventChart.labels[0]").value("2026-05-01"))
                .andExpect(jsonPath("$.eventChart.values[0]").value(5))
                .andExpect(jsonPath("$.lotStatuses[0].lotId").value(1))
                .andExpect(jsonPath("$.lotProgress[0].progress").value(33.3));
    }

    // =============================
    // ✅ GET /api/dashboard/events - EMPTY CHART
    // =============================
    @Test
    void getEvents_emptyChart_returnsEmptyArrays() throws Exception {
        EventChartDTO emptyChart = EventChartDTO.builder()
                .labels(List.of())
                .values(List.of())
                .build();

        DashboardResponse fullResponse = DashboardResponse.builder()
                .eventChart(emptyChart)
                .lotStatuses(List.of())
                .lotProgress(List.of())
                .build();

        when(dashboardService.getDashboard(null)).thenReturn(fullResponse);

        mockMvc.perform(get("/api/dashboard/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.labels").isEmpty())
                .andExpect(jsonPath("$.values").isEmpty());
    }
}