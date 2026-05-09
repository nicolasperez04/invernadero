package com.invernadero.proyecto.service;

import com.invernadero.proyecto.Dto.Request.LotRequest;
import com.invernadero.proyecto.Dto.response.LotResponse;
import com.invernadero.proyecto.Dto.response.LotSummary;
import com.invernadero.proyecto.Entity.Crop;
import com.invernadero.proyecto.Entity.Event;
import com.invernadero.proyecto.Entity.EventType;
import com.invernadero.proyecto.Entity.Lot;
import com.invernadero.proyecto.Repository.CropRepository;
import com.invernadero.proyecto.Repository.EventRepository;
import com.invernadero.proyecto.Repository.LotRepository;
import com.invernadero.proyecto.Service.LotService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
                .build();

        when(cropRepository.findById(1L)).thenReturn(Optional.of(crop));
        when(lotRepository.save(any(Lot.class))).thenReturn(saved);

        LotResponse response = lotService.createLot(request);

        assertEquals(10L, response.getId());
        assertEquals("Lot A", response.getName());
        assertEquals(1L, response.getCropId());
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
        verifyNoInteractions(lotRepository);
    }

    @Test
    void getLotById_success() {
        Lot lot = buildLot(1L, "Lot A");
        when(lotRepository.findById(1L)).thenReturn(Optional.of(lot));

        LotResponse response = lotService.getLotById(1L);

        assertEquals("Lot A", response.getName());
        assertEquals(1L, response.getCropId());
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
    }

    @Test
    void deleteLot_success() {
        doNothing().when(lotRepository).deleteById(1L);

        lotService.deleteLot(1L);

        verify(lotRepository).deleteById(1L);
    }

    @Test
    void getLotStatus_created() {
        when(eventRepository.existsByLotIdAndTypeName(1L, "SOWING")).thenReturn(false);
        when(eventRepository.existsByLotIdAndTypeName(1L, "HARVEST")).thenReturn(false);

        String status = lotService.getLotStatus(1L);

        assertEquals("CREATED", status);
    }

    @Test
    void getLotStatus_finished() {
        when(eventRepository.existsByLotIdAndTypeName(1L, "SOWING")).thenReturn(true);
        when(eventRepository.existsByLotIdAndTypeName(1L, "HARVEST")).thenReturn(true);

        String status = lotService.getLotStatus(1L);

        assertEquals("FINISHED", status);
    }

    @Test
    void getLotStatus_inProduction() {
        when(eventRepository.existsByLotIdAndTypeName(1L, "SOWING")).thenReturn(true);
        when(eventRepository.existsByLotIdAndTypeName(1L, "HARVEST")).thenReturn(false);

        String status = lotService.getLotStatus(1L);

        assertEquals("IN_PRODUCTION", status);
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
                .build();
    }

    private Event buildEvent(String typeName, Instant timestamp) {
        EventType type = EventType.builder().name(typeName).build();
        return Event.builder().type(type).timestamp(timestamp).build();
    }
}

