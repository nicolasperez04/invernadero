package com.invernadero.proyecto.service;

import com.invernadero.proyecto.Dto.response.DashboardResponse;
import com.invernadero.proyecto.Dto.response.EventChartDTO;
import com.invernadero.proyecto.Dto.response.LotProgressDTO;
import com.invernadero.proyecto.Dto.response.LotStatusDTO;
import com.invernadero.proyecto.Entity.Crop;
import com.invernadero.proyecto.Entity.Lot;
import com.invernadero.proyecto.Repository.EventRepository;
import com.invernadero.proyecto.Repository.LotRepository;
import com.invernadero.proyecto.Service.DashboardService;
import com.invernadero.proyecto.Service.LotService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class DashboardServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private LotRepository lotRepository;

    @Mock
    private LotService lotService;

    @InjectMocks
    private DashboardService dashboardService;

    private Map<String, Object> createProgressMap(Instant sowingDate, int totalDays, int daysElapsed, int daysRemaining) {
        Map<String, Object> map = new HashMap<>();
        map.put("sowingDate", sowingDate);
        map.put("totalDays", totalDays);
        map.put("daysElapsed", daysElapsed);
        map.put("daysRemaining", daysRemaining);
        return map;
    }

    @Test
    void getDashboard_cropIdNull_returnsAllLots() {
        Crop crop = Crop.builder().id(1L).name("Tomate").estimatedGrowthDays(60).inactivityDaysThreshold(7).build();
        Lot lot = Lot.builder().id(1L).name("Lote 1").crop(crop).build();

        when(lotRepository.findAll()).thenReturn(List.of(lot));
        when(lotService.getLotStatus(anyLong())).thenReturn("IN_PRODUCTION");
        when(lotService.getInactivityStatus(anyLong())).thenReturn("GREEN");
        when(lotService.getCropProgress(anyLong())).thenReturn(50.0);
        when(lotService.getLotProgressDetails(anyLong())).thenReturn(
                createProgressMap(Instant.now().minus(30, ChronoUnit.DAYS), 60, 30, 30));
        when(lotService.getEstimatedHarvestDate(anyLong())).thenReturn(Instant.now().plus(30, ChronoUnit.DAYS));

        DashboardResponse response = dashboardService.getDashboard(null);

        assertNotNull(response);
        assertNotNull(response.getLotStatuses());
        assertEquals(1, response.getLotStatuses().size());
        verify(lotRepository, atLeast(1)).findAll();
    }

    @Test
    void getDashboard_withCropId_filtersByCrop() {
        Crop crop = Crop.builder().id(1L).name("Tomate").estimatedGrowthDays(60).inactivityDaysThreshold(7).build();
        Lot lot = Lot.builder().id(1L).name("Lote 1").crop(crop).build();

        when(lotRepository.findByCropId(1L)).thenReturn(List.of(lot));
        when(lotService.getLotStatus(anyLong())).thenReturn("IN_PRODUCTION");
        when(lotService.getInactivityStatus(anyLong())).thenReturn("YELLOW");
        when(lotService.getCropProgress(anyLong())).thenReturn(75.0);
        when(lotService.getLotProgressDetails(anyLong())).thenReturn(
                createProgressMap(Instant.now().minus(45, ChronoUnit.DAYS), 60, 45, 15));
        when(lotService.getEstimatedHarvestDate(anyLong())).thenReturn(Instant.now().plus(15, ChronoUnit.DAYS));

        DashboardResponse response = dashboardService.getDashboard(1L);

        assertNotNull(response);
        assertNotNull(response.getLotStatuses());
        assertEquals(1, response.getLotStatuses().size());
        assertEquals("YELLOW", response.getLotStatuses().get(0).getInactivityLevel());
        verify(lotRepository, atLeast(1)).findByCropId(1L);
    }

    @Test
    void getDashboard_noLots_returnsEmptyLists() {
        when(lotRepository.findAll()).thenReturn(List.of());

        DashboardResponse response = dashboardService.getDashboard(null);

        assertNotNull(response);
        assertTrue(response.getLotStatuses().isEmpty());
        assertTrue(response.getLotProgress().isEmpty());
        assertNotNull(response.getEventChart());
        assertEquals(7, response.getEventChart().getLabels().size());
    }

    @Test
    void getDashboard_withEvents_buildsEventChart() {
        Crop crop = Crop.builder().id(1L).name("Tomate").estimatedGrowthDays(60).inactivityDaysThreshold(7).build();
        Lot lot = Lot.builder().id(1L).name("Lote 1").crop(crop).build();

        Instant today = Instant.now();
        Object[] eventRow = new Object[]{today.toString().split("T")[0], 5L};
        List<Object[]> eventList = new ArrayList<>();
        eventList.add(eventRow);

        when(lotRepository.findAll()).thenReturn(List.of(lot));
        when(eventRepository.countEventsByDay(any(Instant.class), any())).thenReturn(eventList);
        when(lotService.getLotStatus(anyLong())).thenReturn("IN_PRODUCTION");
        when(lotService.getInactivityStatus(anyLong())).thenReturn("GREEN");
        when(lotService.getCropProgress(anyLong())).thenReturn(50.0);
        when(lotService.getLotProgressDetails(anyLong())).thenReturn(
                createProgressMap(Instant.now().minus(30, ChronoUnit.DAYS), 60, 30, 30));
        when(lotService.getEstimatedHarvestDate(anyLong())).thenReturn(Instant.now().plus(30, ChronoUnit.DAYS));

        DashboardResponse response = dashboardService.getDashboard(null);

        assertNotNull(response);
        assertNotNull(response.getEventChart());
        assertNotNull(response.getEventChart().getLabels());
        assertEquals(7, response.getEventChart().getLabels().size());
    }

    @Test
    void getDashboard_noEvents_chartWithZeros() {
        Crop crop = Crop.builder().id(1L).name("Tomate").estimatedGrowthDays(60).inactivityDaysThreshold(7).build();
        Lot lot = Lot.builder().id(1L).name("Lote 1").crop(crop).build();

        when(lotRepository.findAll()).thenReturn(List.of(lot));
        when(eventRepository.countEventsByDay(any(Instant.class), any())).thenReturn(new ArrayList<>());
        when(lotService.getLotStatus(anyLong())).thenReturn("CREATED");
        when(lotService.getInactivityStatus(anyLong())).thenReturn("GRAY");
        when(lotService.getCropProgress(anyLong())).thenReturn(0.0);
        when(lotService.getLotProgressDetails(anyLong())).thenReturn(
                createProgressMap(null, 0, 0, 0));
        when(lotService.getEstimatedHarvestDate(anyLong())).thenReturn(null);

        DashboardResponse response = dashboardService.getDashboard(null);

        assertNotNull(response);
        EventChartDTO chart = response.getEventChart();
        assertNotNull(chart.getValues());
        assertTrue(chart.getValues().stream().allMatch(v -> v == 0L));
    }

    @Test
    void getDashboard_lotStatuses_correctlyBuilt() {
        Crop crop = Crop.builder().id(1L).name("Tomate").estimatedGrowthDays(60).inactivityDaysThreshold(7).build();
        Lot lot1 = Lot.builder().id(1L).name("Lote 1").crop(crop).build();
        Lot lot2 = Lot.builder().id(2L).name("Lote 2").crop(crop).build();

        when(lotRepository.findAll()).thenReturn(List.of(lot1, lot2));
        when(lotService.getLotStatus(1L)).thenReturn("IN_PRODUCTION");
        when(lotService.getLotStatus(2L)).thenReturn("FINISHED");
        when(lotService.getInactivityStatus(1L)).thenReturn("GREEN");
        when(lotService.getInactivityStatus(2L)).thenReturn("RED");
        when(lotService.getCropProgress(anyLong())).thenReturn(50.0);
        when(lotService.getLotProgressDetails(anyLong())).thenReturn(
                createProgressMap(Instant.now().minus(30, ChronoUnit.DAYS), 60, 30, 30));
        when(lotService.getEstimatedHarvestDate(anyLong())).thenReturn(Instant.now().plus(30, ChronoUnit.DAYS));

        DashboardResponse response = dashboardService.getDashboard(null);

        assertNotNull(response);
        assertEquals(2, response.getLotStatuses().size());

        LotStatusDTO status1 = response.getLotStatuses().get(0);
        assertEquals(1L, status1.getLotId());
        assertEquals("IN_PRODUCTION", status1.getStatus());
        assertEquals("GREEN", status1.getInactivityLevel());

        LotStatusDTO status2 = response.getLotStatuses().get(1);
        assertEquals(2L, status2.getLotId());
        assertEquals("FINISHED", status2.getStatus());
        assertEquals("RED", status2.getInactivityLevel());
    }

    @Test
    void getDashboard_lotProgress_correctlyCalculated() {
        Crop crop = Crop.builder().id(1L).name("Tomate").estimatedGrowthDays(60).inactivityDaysThreshold(7).build();
        Lot lot = Lot.builder().id(1L).name("Lote 1").crop(crop).build();

        Instant sowingDate = Instant.now().minus(30, ChronoUnit.DAYS);
        Instant harvestDate = Instant.now().plus(30, ChronoUnit.DAYS);

        when(lotRepository.findAll()).thenReturn(List.of(lot));
        when(lotService.getLotStatus(anyLong())).thenReturn("IN_PRODUCTION");
        when(lotService.getInactivityStatus(anyLong())).thenReturn("GREEN");
        when(lotService.getCropProgress(1L)).thenReturn(50.0);
        when(lotService.getLotProgressDetails(1L)).thenReturn(
                createProgressMap(sowingDate, 60, 30, 30));
        when(lotService.getEstimatedHarvestDate(1L)).thenReturn(harvestDate);

        DashboardResponse response = dashboardService.getDashboard(null);

        assertNotNull(response);
        assertEquals(1, response.getLotProgress().size());

        LotProgressDTO progress = response.getLotProgress().get(0);
        assertEquals(1L, progress.getLotId());
        assertEquals("Lote 1", progress.getLotName());
        assertEquals(50.0, progress.getProgress());
        assertEquals(60, progress.getTotalDays());
        assertEquals(30, progress.getDaysElapsed());
        assertEquals(30, progress.getDaysRemaining());
    }

    @Test
    void getDashboard_multipleCropsWithDifferentStates() {
        Crop crop1 = Crop.builder().id(1L).name("Tomate").estimatedGrowthDays(60).inactivityDaysThreshold(7).build();
        Crop crop2 = Crop.builder().id(2L).name("Lechuga").estimatedGrowthDays(30).inactivityDaysThreshold(5).build();

        Lot lot1 = Lot.builder().id(1L).name("Lote Tomate").crop(crop1).build();
        Lot lot2 = Lot.builder().id(2L).name("Lote Lechuga").crop(crop2).build();

        when(lotRepository.findAll()).thenReturn(List.of(lot1, lot2));
        when(lotService.getLotStatus(1L)).thenReturn("IN_PRODUCTION");
        when(lotService.getLotStatus(2L)).thenReturn("CREATED");
        when(lotService.getInactivityStatus(1L)).thenReturn("GREEN");
        when(lotService.getInactivityStatus(2L)).thenReturn("GRAY");
        when(lotService.getCropProgress(anyLong())).thenReturn(50.0);
        when(lotService.getLotProgressDetails(anyLong())).thenReturn(
                createProgressMap(Instant.now().minus(30, ChronoUnit.DAYS), 60, 30, 30));
        when(lotService.getEstimatedHarvestDate(anyLong())).thenReturn(Instant.now().plus(30, ChronoUnit.DAYS));

        DashboardResponse response = dashboardService.getDashboard(null);

        assertNotNull(response);
        assertEquals(2, response.getLotStatuses().size());
        assertEquals(2, response.getLotProgress().size());
    }
}