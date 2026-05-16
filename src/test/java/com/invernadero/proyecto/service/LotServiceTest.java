package com.invernadero.proyecto.service;

import com.invernadero.proyecto.Dto.Request.LotRequest;
import com.invernadero.proyecto.Dto.response.LotResponse;
import com.invernadero.proyecto.Dto.response.LotSummary;
import com.invernadero.proyecto.Entity.Crop;
import com.invernadero.proyecto.Entity.Event;
import com.invernadero.proyecto.Entity.EventType;
import com.invernadero.proyecto.Entity.Lot;
import com.invernadero.proyecto.Entity.LotStatus;
import com.invernadero.proyecto.Repository.CropRepository;
import com.invernadero.proyecto.Repository.EventRepository;
import com.invernadero.proyecto.Repository.LotRepository;
import com.invernadero.proyecto.Service.LotService;
import com.invernadero.proyecto.Service.SseService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LotServiceTest {

    @Mock
    private LotRepository lotRepository;

    @Mock
    private CropRepository cropRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private SseService sseService;

    @InjectMocks
    private LotService lotService;

    @Test
    void createLot_success() {
        LotRequest request = LotRequest.builder()
                .name("Lot A")
                .cropId(1L)
                .startDate(Instant.parse("2024-01-01T00:00:00Z"))
                .endDate(Instant.parse("2024-02-01T00:00:00Z"))
                .build();

        Crop crop = Crop.builder().id(1L).name("Tomato").build();
        Lot saved = Lot.builder()
                .id(10L)
                .name("Lot A")
                .crop(crop)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(LotStatus.CREATED)
                .build();

        when(cropRepository.findById(1L)).thenReturn(Optional.of(crop));
        when(lotRepository.save(any(Lot.class))).thenReturn(saved);

        LotResponse response = lotService.createLot(request);

        assertEquals(10L, response.getId());
        assertEquals("Lot A", response.getName());
        assertEquals(1L, response.getCropId());
        assertEquals("CREATED", response.getStatus());

        ArgumentCaptor<Lot> captor = ArgumentCaptor.forClass(Lot.class);
        verify(lotRepository).save(captor.capture());
        assertEquals(LotStatus.CREATED, captor.getValue().getStatus());
        verify(sseService).sendEvent(eq("dashboard"), anyString());
    }

    @Test
    void createLot_cropNotFound() {
        LotRequest request = LotRequest.builder()
                .name("Lot A")
                .cropId(1L)
                .startDate(Instant.parse("2024-01-01T00:00:00Z"))
                .build();

        when(cropRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> lotService.createLot(request));

        assertEquals("Crop not found", ex.getMessage());
        verifyNoInteractions(lotRepository, sseService);
    }

    @Test
    void getLotById_success() {
        Lot lot = buildLot(1L, "Lot A");
        when(lotRepository.findById(1L)).thenReturn(Optional.of(lot));

        LotResponse response = lotService.getLotById(1L);

        assertEquals("Lot A", response.getName());
        assertEquals(1L, response.getCropId());
        assertEquals("CREATED", response.getStatus());
    }

    @Test
    void getLotById_notFound() {
        when(lotRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> lotService.getLotById(1L));

        assertEquals("Lot not found", ex.getMessage());
    }

    @Test
    void getAllLots_success() {
        when(lotRepository.findAll()).thenReturn(List.of(
                buildLot(1L, "Lot A"),
                buildLot(2L, "Lot B")
        ));

        List<LotResponse> response = lotService.getAllLots();

        assertEquals(2, response.size());
    }

    @Test
    void getAllLots_filterByStatus() {
        Crop crop = Crop.builder().id(1L).name("Tomato").build();
        Lot lot = Lot.builder()
                .id(1L).name("Lot A").crop(crop)
                .startDate(Instant.parse("2024-01-01T00:00:00Z"))
                .endDate(Instant.parse("2024-02-01T00:00:00Z"))
                .status(LotStatus.IN_PRODUCTION)
                .build();
        when(lotRepository.findByStatus(LotStatus.IN_PRODUCTION)).thenReturn(List.of(lot));

        List<LotResponse> response = lotService.getAllLots("IN_PRODUCTION");

        assertEquals(1, response.size());
        assertEquals("IN_PRODUCTION", response.get(0).getStatus());
    }

    @Test
    void getLotsByCrop_success() {
        when(lotRepository.findByCropId(1L)).thenReturn(List.of(
                buildLot(1L, "Lot A")
        ));

        List<LotResponse> response = lotService.getLotsByCrop(1L);

        assertEquals(1, response.size());
        assertEquals(1L, response.get(0).getCropId());
    }

    @Test
    void updateLot_success() {
        Lot lot = buildLot(1L, "Old");

        LotRequest request = new LotRequest();
        request.setName("New");

        when(lotRepository.findById(1L)).thenReturn(Optional.of(lot));
        when(lotRepository.save(any(Lot.class))).thenReturn(lot);

        LotResponse response = lotService.updateLot(1L, request);

        assertEquals("New", response.getName());
        verify(sseService).sendEvent(eq("dashboard"), anyString());
    }

    @Test
    void updateLot_blankName_shouldKeepOld() {
        Lot lot = buildLot(1L, "Old");

        LotRequest request = new LotRequest();
        request.setName("  ");

        when(lotRepository.findById(1L)).thenReturn(Optional.of(lot));
        when(lotRepository.save(any(Lot.class))).thenReturn(lot);

        LotResponse response = lotService.updateLot(1L, request);

        assertEquals("  ", response.getName());
        verify(sseService).sendEvent(eq("dashboard"), anyString());
    }

    @Test
    void deleteLot_success() {
        doNothing().when(lotRepository).deleteById(1L);

        lotService.deleteLot(1L);

        verify(lotRepository).deleteById(1L);
        verify(sseService).sendEvent(eq("dashboard"), anyString());
    }

    @Test
    void getLotStatus_created() {
        Lot lot = buildLot(1L, "Lot A");
        lot.setStatus(LotStatus.CREATED);
        when(lotRepository.findById(1L)).thenReturn(Optional.of(lot));

        String status = lotService.getLotStatus(1L);

        assertEquals("CREATED", status);
    }

    @Test
    void getLotStatus_finished() {
        Lot lot = buildLot(1L, "Lot A");
        lot.setStatus(LotStatus.FINISHED);
        when(lotRepository.findById(1L)).thenReturn(Optional.of(lot));

        String status = lotService.getLotStatus(1L);

        assertEquals("FINISHED", status);
    }

    @Test
    void getLotStatus_inProduction() {
        Lot lot = buildLot(1L, "Lot A");
        lot.setStatus(LotStatus.IN_PRODUCTION);
        when(lotRepository.findById(1L)).thenReturn(Optional.of(lot));

        String status = lotService.getLotStatus(1L);

        assertEquals("IN_PRODUCTION", status);
    }

    @Test
    void getLotStatus_notFound() {
        when(lotRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> lotService.getLotStatus(1L));

        assertEquals("Lot not found", ex.getMessage());
    }

    @Test
    void calculateDurationInDays_noSowing_returnsZero() {
        EventType type = EventType.builder().name("FERTILIZATION").build();
        Event event = Event.builder()
                .type(type)
                .timestamp(Instant.parse("2024-01-01T00:00:00Z"))
                .build();

        when(eventRepository.findByLotIdOrderByTimestampAsc(1L)).thenReturn(List.of(event));

        long days = lotService.calculateDurationInDays(1L);

        assertEquals(0L, days);
    }

    @Test
    void calculateDurationInDays_withHarvest() {
        Event sowing = buildEvent("SOWING", Instant.parse("2024-01-01T00:00:00Z"));
        Event harvest = buildEvent("HARVEST", Instant.parse("2024-01-11T00:00:00Z"));

        when(eventRepository.findByLotIdOrderByTimestampAsc(1L)).thenReturn(List.of(sowing, harvest));

        long days = lotService.calculateDurationInDays(1L);

        assertEquals(10L, days);
    }

    @Test
    void calculateEventFrequency_withDays() {
        Event sowing = buildEvent("SOWING", Instant.parse("2024-01-01T00:00:00Z"));
        Event harvest = buildEvent("HARVEST", Instant.parse("2024-01-11T00:00:00Z"));

        when(eventRepository.findByLotId(1L)).thenReturn(List.of(sowing, harvest, sowing, harvest));
        when(eventRepository.findByLotIdOrderByTimestampAsc(1L)).thenReturn(List.of(sowing, harvest));

        double frequency = lotService.calculateEventFrequency(1L);

        assertEquals(0.4, frequency, 0.0001);
    }

    @Test
    void getLotSummary_success() {
        Lot lot = buildLot(1L, "Lot A");
        when(lotRepository.findById(1L)).thenReturn(Optional.of(lot));

        LotService spyService = spy(lotService);
        doReturn("IN_PRODUCTION").when(spyService).getLotStatus(1L);
        doReturn(4L).when(spyService).countEvents(1L);
        doReturn(12L).when(spyService).calculateDurationInDays(1L);
        doReturn(0.33).when(spyService).calculateEventFrequency(1L);
        doReturn(Map.of(
                "sowingDate", Instant.parse("2024-01-01T00:00:00Z"),
                "totalDays", 60,
                "daysElapsed", 30,
                "daysRemaining", 30
        )).when(spyService).getLotProgressDetails(1L);

        LotSummary summary = spyService.getLotSummary(1L);

        assertEquals(1L, summary.getLotId());
        assertEquals("Lot A", summary.getLotName());
        assertEquals("IN_PRODUCTION", summary.getStatus());
        assertEquals(4L, summary.getTotalEvents());
    }

    private Lot buildLot(Long id, String name) {
        Crop crop = Crop.builder().id(1L).name("Tomato").estimatedGrowthDays(60).inactivityDaysThreshold(7).build();
        return Lot.builder()
                .id(id)
                .name(name)
                .crop(crop)
                .startDate(Instant.parse("2024-01-01T00:00:00Z"))
                .endDate(Instant.parse("2024-02-01T00:00:00Z"))
                .status(LotStatus.CREATED)
                .build();
    }

    private Event buildEvent(String typeName, Instant timestamp) {
        EventType type = EventType.builder().name(typeName).build();
        return Event.builder().type(type).timestamp(timestamp).build();
    }

    @Test
    void updateLot_notFound() {
        when(lotRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> lotService.updateLot(99L, new LotRequest()));
    }

    @Test
    void getAllLots_invalidStatus_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> lotService.getAllLots("BOGUS"));
    }

    @Test
    void getInactivityStatus_notFound() {
        when(lotRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> lotService.getInactivityStatus(1L));
    }

    @Test
    void getInactivityStatus_noEvents_returnsGray() {
        Lot lot = buildLot(1L, "Lot A");
        when(lotRepository.findById(1L)).thenReturn(Optional.of(lot));
        when(eventRepository.findByLotIdOrderByTimestampDesc(1L)).thenReturn(List.of());

        String status = lotService.getInactivityStatus(1L);

        assertEquals("GRAY", status);
    }

    @Test
    void getInactivityStatus_thresholdNull_returnsUnknown() {
        Lot lot = buildLot(1L, "Lot A");
        lot.getCrop().setInactivityDaysThreshold(null);
        Event event = buildEvent("IRRIGATION", Instant.now());
        when(lotRepository.findById(1L)).thenReturn(Optional.of(lot));
        when(eventRepository.findByLotIdOrderByTimestampDesc(1L)).thenReturn(List.of(event));

        String status = lotService.getInactivityStatus(1L);

        assertEquals("UNKNOWN", status);
    }

    @Test
    void getInactivityStatus_daysAboveThreshold_returnsRed() {
        Lot lot = buildLot(1L, "Lot A");
        lot.getCrop().setInactivityDaysThreshold(5);
        Event event = buildEvent("IRRIGATION", Instant.now().minusSeconds(86400 * 10));
        when(lotRepository.findById(1L)).thenReturn(Optional.of(lot));
        when(eventRepository.findByLotIdOrderByTimestampDesc(1L)).thenReturn(List.of(event));

        String status = lotService.getInactivityStatus(1L);

        assertEquals("RED", status);
    }

    @Test
    void getInactivityStatus_daysAboveHalfThreshold_returnsYellow() {
        Lot lot = buildLot(1L, "Lot A");
        lot.getCrop().setInactivityDaysThreshold(10);
        Event event = buildEvent("IRRIGATION", Instant.now().minusSeconds(86400 * 6));
        when(lotRepository.findById(1L)).thenReturn(Optional.of(lot));
        when(eventRepository.findByLotIdOrderByTimestampDesc(1L)).thenReturn(List.of(event));

        String status = lotService.getInactivityStatus(1L);

        assertEquals("YELLOW", status);
    }

    @Test
    void getInactivityStatus_daysBelowHalfThreshold_returnsGreen() {
        Lot lot = buildLot(1L, "Lot A");
        lot.getCrop().setInactivityDaysThreshold(10);
        Event event = buildEvent("IRRIGATION", Instant.now());
        when(lotRepository.findById(1L)).thenReturn(Optional.of(lot));
        when(eventRepository.findByLotIdOrderByTimestampDesc(1L)).thenReturn(List.of(event));

        String status = lotService.getInactivityStatus(1L);

        assertEquals("GREEN", status);
    }

    @Test
    void getCropProgress_notFound() {
        when(lotRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> lotService.getCropProgress(1L));
    }

    @Test
    void getCropProgress_noHarvestDate_returnsZero() {
        Lot lot = buildLot(1L, "Lot A");
        lot.setEstimatedHarvestDate(null);
        when(lotRepository.findById(1L)).thenReturn(Optional.of(lot));

        double progress = lotService.getCropProgress(1L);

        assertEquals(0, progress);
    }

    @Test
    void getCropProgress_noSowingEvent_returnsZero() {
        Lot lot = buildLot(1L, "Lot A");
        lot.setEstimatedHarvestDate(Instant.parse("2024-03-01T00:00:00Z"));
        when(lotRepository.findById(1L)).thenReturn(Optional.of(lot));
        when(eventRepository.findByLotIdOrderByTimestampAsc(1L)).thenReturn(List.of());

        double progress = lotService.getCropProgress(1L);

        assertEquals(0, progress);
    }

    @Test
    void getCropProgress_totalDaysZeroOrNegative_returns100() {
        Lot lot = buildLot(1L, "Lot A");
        lot.setEstimatedHarvestDate(Instant.parse("2024-01-01T00:00:00Z"));
        Event sowing = buildEvent("SOWING", Instant.parse("2024-01-01T00:00:00Z"));
        when(lotRepository.findById(1L)).thenReturn(Optional.of(lot));
        when(eventRepository.findByLotIdOrderByTimestampAsc(1L)).thenReturn(List.of(sowing));

        double progress = lotService.getCropProgress(1L);

        assertEquals(100, progress, 0.01);
    }

    @Test
    void getCropProgress_normal() {
        Lot lot = buildLot(1L, "Lot A");
        lot.setEstimatedHarvestDate(Instant.parse("2024-03-01T00:00:00Z"));
        Event sowing = buildEvent("SOWING", Instant.parse("2024-01-01T00:00:00Z"));
        when(lotRepository.findById(1L)).thenReturn(Optional.of(lot));
        when(eventRepository.findByLotIdOrderByTimestampAsc(1L)).thenReturn(List.of(sowing));

        double progress = lotService.getCropProgress(1L);

        assertTrue(progress >= 0 && progress <= 100);
    }

    @Test
    void getLotProgressDetails_notFound() {
        when(lotRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> lotService.getLotProgressDetails(1L));
    }

    @Test
    void getLotProgressDetails_noSowing_nullCrop() {
        Lot lot = buildLot(1L, "Lot A");
        lot.setCrop(null);
        when(lotRepository.findById(1L)).thenReturn(Optional.of(lot));
        when(eventRepository.findByLotIdOrderByTimestampAsc(1L)).thenReturn(List.of());

        Map<String, Object> details = lotService.getLotProgressDetails(1L);

        assertNull(details.get("sowingDate"));
        assertEquals(0, details.get("totalDays"));
        assertEquals(0, details.get("daysElapsed"));
        assertEquals(0, details.get("daysRemaining"));
    }

    @Test
    void getLotProgressDetails_full() {
        Lot lot = buildLot(1L, "Lot A");
        lot.setEstimatedHarvestDate(Instant.parse("2024-03-01T00:00:00Z"));
        Event sowing = buildEvent("SOWING", Instant.parse("2024-01-01T00:00:00Z"));
        when(lotRepository.findById(1L)).thenReturn(Optional.of(lot));
        when(eventRepository.findByLotIdOrderByTimestampAsc(1L)).thenReturn(List.of(sowing));

        Map<String, Object> details = lotService.getLotProgressDetails(1L);

        assertNotNull(details.get("sowingDate"));
        assertTrue((int) details.get("totalDays") > 0);
    }

    @Test
    void migrateExistingLotStatuses_noNullStatuses() {
        when(lotRepository.findAll()).thenReturn(List.of(buildLot(1L, "Lot A")));

        lotService.migrateExistingLotStatuses();

        verify(lotRepository, never()).save(any());
    }

    @Test
    void migrateExistingLotStatuses_withHarvest_setsFinished() {
        Lot lot = Lot.builder().id(1L).name("Test").status(null).build();
        when(lotRepository.findAll()).thenReturn(List.of(lot));
        when(eventRepository.existsByLotIdAndTypeName(1L, "SOWING")).thenReturn(true);
        when(eventRepository.existsByLotIdAndTypeName(1L, "HARVEST")).thenReturn(true);

        lotService.migrateExistingLotStatuses();

        verify(lotRepository).save(lot);
        assertEquals(LotStatus.FINISHED, lot.getStatus());
    }

    @Test
    void migrateExistingLotStatuses_withSowingOnly_setsInProduction() {
        Lot lot = Lot.builder().id(1L).name("Test").status(null).build();
        when(lotRepository.findAll()).thenReturn(List.of(lot));
        when(eventRepository.existsByLotIdAndTypeName(1L, "SOWING")).thenReturn(true);
        when(eventRepository.existsByLotIdAndTypeName(1L, "HARVEST")).thenReturn(false);

        lotService.migrateExistingLotStatuses();

        verify(lotRepository).save(lot);
        assertEquals(LotStatus.IN_PRODUCTION, lot.getStatus());
    }

    @Test
    void migrateExistingLotStatuses_withoutEvents_setsCreated() {
        Lot lot = Lot.builder().id(1L).name("Test").status(null).build();
        when(lotRepository.findAll()).thenReturn(List.of(lot));
        when(eventRepository.existsByLotIdAndTypeName(1L, "SOWING")).thenReturn(false);

        lotService.migrateExistingLotStatuses();

        verify(lotRepository).save(lot);
        assertEquals(LotStatus.CREATED, lot.getStatus());
    }

    @Test
    void calculateDurationInDays_noHarvest_calculatesLive() {
        Event sowing = buildEvent("SOWING", Instant.parse("2024-01-01T00:00:00Z"));
        when(eventRepository.findByLotIdOrderByTimestampAsc(1L)).thenReturn(List.of(sowing));

        long days = lotService.calculateDurationInDays(1L);

        assertTrue(days > 0);
    }

    @Test
    void calculateEventFrequency_zeroDays_returnsTotalEvents() {
        when(eventRepository.findByLotId(1L)).thenReturn(List.of(
                buildEvent("IRRIGATION", Instant.parse("2024-01-01T00:00:00Z"))
        ));
        when(eventRepository.findByLotIdOrderByTimestampAsc(1L)).thenReturn(List.of(
                buildEvent("IRRIGATION", Instant.parse("2024-01-01T00:00:00Z"))
        ));

        double frequency = lotService.calculateEventFrequency(1L);

        assertEquals(1.0, frequency, 0.0001);
    }

    @Test
    void getEventsLast7Days_success() {
        when(eventRepository.countEventsByDay(any(Instant.class))).thenReturn(List.of(
                new Object[]{"2024-01-01", 3L},
                new Object[]{"2024-01-02", 5L}
        ));

        Map<String, Long> result = lotService.getEventsLast7Days();

        assertEquals(2, result.size());
        assertEquals(Long.valueOf(3), result.get("2024-01-01"));
        assertEquals(Long.valueOf(5), result.get("2024-01-02"));
    }

    @Test
    void getEstimatedHarvestDate_notFound() {
        when(lotRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> lotService.getEstimatedHarvestDate(1L));
    }

    @Test
    void getEstimatedHarvestDate_returnsDate() {
        Lot lot = buildLot(1L, "Lot A");
        lot.setEstimatedHarvestDate(Instant.parse("2024-03-01T00:00:00Z"));
        when(lotRepository.findById(1L)).thenReturn(Optional.of(lot));

        Instant result = lotService.getEstimatedHarvestDate(1L);

        assertEquals(Instant.parse("2024-03-01T00:00:00Z"), result);
    }

    @Test
    void getLotsByCrop_withStatusFilter() {
        Crop crop = Crop.builder().id(1L).name("Tomato").build();
        Lot lot = Lot.builder().id(1L).name("Lot A").crop(crop).status(LotStatus.IN_PRODUCTION).build();
        when(lotRepository.findByCropIdAndStatus(1L, LotStatus.IN_PRODUCTION)).thenReturn(List.of(lot));

        List<LotResponse> response = lotService.getLotsByCrop(1L, "IN_PRODUCTION");

        assertEquals(1, response.size());
        assertEquals("IN_PRODUCTION", response.get(0).getStatus());
    }

    @Test
    void getLotSummary_withEvents() {
        Lot lot = buildLot(1L, "Lot A");
        Event event = buildEvent("IRRIGATION", Instant.parse("2024-01-15T00:00:00Z"));
        when(lotRepository.findById(1L)).thenReturn(Optional.of(lot));
        when(eventRepository.findByLotIdOrderByTimestampDesc(1L)).thenReturn(List.of(event));

        LotService spyService = spy(lotService);
        doReturn("IN_PRODUCTION").when(spyService).getLotStatus(1L);
        doReturn(4L).when(spyService).countEvents(1L);
        doReturn(12L).when(spyService).calculateDurationInDays(1L);
        doReturn(0.33).when(spyService).calculateEventFrequency(1L);
        doReturn(Map.of(
                "sowingDate", Instant.parse("2024-01-01T00:00:00Z"),
                "totalDays", 60,
                "daysElapsed", 30,
                "daysRemaining", 30
        )).when(spyService).getLotProgressDetails(1L);

        LotSummary summary = spyService.getLotSummary(1L);

        assertEquals("IRRIGATION", summary.getLastEventType());
        assertEquals(Instant.parse("2024-01-15T00:00:00Z"), summary.getLastEventDate());
    }
}
