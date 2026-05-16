package com.invernadero.proyecto.service;

import com.invernadero.proyecto.Entity.Crop;
import com.invernadero.proyecto.Entity.Event;
import com.invernadero.proyecto.Entity.EventType;
import com.invernadero.proyecto.Entity.Lot;
import com.invernadero.proyecto.Repository.EventRepository;
import com.invernadero.proyecto.Repository.LotRepository;
import com.invernadero.proyecto.Service.PdfReportService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PdfReportServiceTest {

    private String extractText(byte[] pdf) throws Exception {
        try (PDDocument doc = PDDocument.load(new ByteArrayInputStream(pdf))) {
            return new PDFTextStripper().getText(doc);
        }
    }

    @Mock
    private LotRepository lotRepository;

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private PdfReportService pdfReportService;

    @Test
    void generateLotReport_success_withHarvest() throws Exception {
        Crop crop = Crop.builder().id(1L).name("Tomate").build();
        Lot lot = Lot.builder()
                .id(1L)
                .name("Lote Test")
                .crop(crop)
                .startDate(Instant.parse("2026-01-01T00:00:00Z"))
                .estimatedHarvestDate(Instant.parse("2026-03-01T00:00:00Z"))
                .build();

        EventType sowingType = EventType.builder().id(1L).name("SOWING").category("INITIAL").build();
        EventType irrigType = EventType.builder().id(2L).name("IRRIGATION").category("MAINTENANCE").build();
        EventType harvestType = EventType.builder().id(6L).name("HARVEST").category("FINAL").build();

        List<Event> events = List.of(
                Event.builder().id(1L).lot(lot).type(sowingType).timestamp(Instant.parse("2026-01-01T00:00:00Z")).description("Siembra inicial").build(),
                Event.builder().id(2L).lot(lot).type(irrigType).timestamp(Instant.parse("2026-01-05T00:00:00Z")).description("Riego semanal").build(),
                Event.builder().id(3L).lot(lot).type(harvestType).timestamp(Instant.parse("2026-03-01T00:00:00Z")).description("Cosecha final").build()
        );

        when(lotRepository.findById(1L)).thenReturn(Optional.of(lot));
        when(eventRepository.findByLotIdOrderByTimestampAsc(1L)).thenReturn(events);
        when(eventRepository.existsByLotIdAndTypeName(1L, "SOWING")).thenReturn(true);
        when(eventRepository.existsByLotIdAndTypeName(1L, "HARVEST")).thenReturn(true);

        byte[] pdf = pdfReportService.generateLotReport(1L);

        assertNotNull(pdf);
        assertTrue(pdf.length > 0, "PDF should not be empty");

        String content = extractText(pdf);
        assertTrue(content.contains("SIGMA"), "Should contain SIGMA header");
        assertTrue(content.contains("Lote Test"), "Should contain lot name");
        assertTrue(content.contains("Tomate"), "Should contain crop name");
        assertTrue(content.contains("SOWING"), "Should contain SOWING event");
        assertTrue(content.contains("HARVEST"), "Should contain HARVEST event");
        assertTrue(content.contains("IRRIGATION"), "Should contain IRRIGATION event");

        verify(lotRepository).findById(1L);
        verify(eventRepository).findByLotIdOrderByTimestampAsc(1L);
    }

    @Test
    void generateLotReport_success_noEvents() throws Exception {
        Crop crop = Crop.builder().id(2L).name("Lechuga").build();
        Lot lot = Lot.builder()
                .id(2L)
                .name("Lote Vacío")
                .crop(crop)
                .startDate(Instant.parse("2026-02-01T00:00:00Z"))
                .build();

        when(lotRepository.findById(2L)).thenReturn(Optional.of(lot));
        when(eventRepository.findByLotIdOrderByTimestampAsc(2L)).thenReturn(List.of());
        when(eventRepository.existsByLotIdAndTypeName(2L, "SOWING")).thenReturn(false);
        when(eventRepository.existsByLotIdAndTypeName(2L, "HARVEST")).thenReturn(false);

        byte[] pdf = pdfReportService.generateLotReport(2L);

        assertNotNull(pdf);
        assertTrue(pdf.length > 0, "PDF should not be empty even without events");

        String content = extractText(pdf);
        assertTrue(content.contains("SIGMA"), "Should contain SIGMA header");
        assertTrue(content.contains("Lote"), "Should contain lot name");
        assertTrue(content.contains("Lechuga"), "Should contain crop name");

        verify(lotRepository).findById(2L);
        verify(eventRepository).findByLotIdOrderByTimestampAsc(2L);
    }

    @Test
    void generateLotReport_lotNotFound() {
        when(lotRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> pdfReportService.generateLotReport(99L));
        verify(lotRepository).findById(99L);
        verify(eventRepository, never()).findByLotIdOrderByTimestampAsc(any());
    }
}
