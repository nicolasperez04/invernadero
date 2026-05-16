package com.invernadero.proyecto.Service;

import com.invernadero.proyecto.Entity.Event;
import com.invernadero.proyecto.Entity.Lot;
import com.invernadero.proyecto.Repository.EventRepository;
import com.invernadero.proyecto.Repository.LotRepository;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PdfReportService {

    private final LotRepository lotRepository;
    private final EventRepository eventRepository;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final ZoneId ZONE = ZoneId.of("America/Bogota");

    public byte[] generateLotReport(Long lotId) {
        Lot lot = lotRepository.findById(lotId)
                .orElseThrow(() -> new RuntimeException("Lot not found"));

        List<Event> events = eventRepository.findByLotIdOrderByTimestampAsc(lotId);

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4, 36, 36, 36, 36);
            PdfWriter.getInstance(document, baos);
            document.open();

            addTitle(document, lot);
            document.add(Chunk.NEWLINE);

            addLotInfo(document, lot);
            document.add(Chunk.NEWLINE);

            addKeyDates(document, lot, events);
            document.add(Chunk.NEWLINE);

            addStatistics(document, lot, events);
            document.add(Chunk.NEWLINE);

            addEventsTable(document, events);

            document.close();
            return baos.toByteArray();
        } catch (DocumentException e) {
            throw new RuntimeException("Error generating PDF report", e);
        }
    }

    private void addTitle(Document document, Lot lot) throws DocumentException {
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.DARK_GRAY);
        Paragraph title = new Paragraph("SIGMA - Informe de Cosecha", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        Font metaFont = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.GRAY);
        String generated = ZonedDateTime.now(ZONE).format(DATETIME_FMT);
        Paragraph meta = new Paragraph("Generado: " + generated, metaFont);
        meta.setAlignment(Element.ALIGN_CENTER);
        document.add(meta);

        Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BaseColor.DARK_GRAY);
        Paragraph subtitle = new Paragraph("Lote: " + lot.getName(), subtitleFont);
        subtitle.setAlignment(Element.ALIGN_CENTER);
        document.add(subtitle);
    }

    private void addLotInfo(Document document, Lot lot) throws DocumentException {
        Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, BaseColor.DARK_GRAY);
        Paragraph section = new Paragraph("Datos del Lote", sectionFont);
        section.setSpacingBefore(10);
        document.add(section);

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1, 2});

        addRow(table, "Nombre", lot.getName());
        addRow(table, "Cultivo", lot.getCrop().getName());
        addRow(table, "Estado", getLotStatus(lot));

        document.add(table);
    }

    private void addKeyDates(Document document, Lot lot, List<Event> events) throws DocumentException {
        Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, BaseColor.DARK_GRAY);
        Paragraph section = new Paragraph("Fechas Clave", sectionFont);
        section.setSpacingBefore(10);
        document.add(section);

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1, 2});

        addRow(table, "Fecha de inicio", formatInstant(lot.getStartDate()));

        Instant sowingDate = findEventDate(events, "SOWING");
        addRow(table, "Fecha de siembra", formatInstant(sowingDate));

        Instant harvestDate = findEventDate(events, "HARVEST");
        addRow(table, "Fecha de cosecha", formatInstant(harvestDate));

        addRow(table, "Cosecha estimada", formatInstant(lot.getEstimatedHarvestDate()));

        long duration = calculateDuration(sowingDate, harvestDate);
        addRow(table, "Duración total", duration + " días");

        document.add(table);
    }

    private void addStatistics(Document document, Lot lot, List<Event> events) throws DocumentException {
        Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, BaseColor.DARK_GRAY);
        Paragraph section = new Paragraph("Estadísticas", sectionFont);
        section.setSpacingBefore(10);
        document.add(section);

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1, 2});

        long totalEvents = events.size();
        addRow(table, "Total de eventos", String.valueOf(totalEvents));

        Instant sowingDate = findEventDate(events, "SOWING");
        Instant harvestDate = findEventDate(events, "HARVEST");
        long durationDays = calculateDuration(sowingDate, harvestDate);
        double frequency = durationDays > 0 ? (double) totalEvents / durationDays : totalEvents;
        addRow(table, "Frecuencia de eventos", String.format("%.2f eventos/día", frequency));

        document.add(table);
    }

    private void addEventsTable(Document document, List<Event> events) throws DocumentException {
        Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, BaseColor.DARK_GRAY);
        Paragraph section = new Paragraph("Historial de Eventos", sectionFont);
        section.setSpacingBefore(10);
        document.add(section);

        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{2, 2, 2, 3});

        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.WHITE);
        BaseColor headerBg = new BaseColor(66, 66, 66);

        addHeaderCell(table, "Fecha", headerFont, headerBg);
        addHeaderCell(table, "Tipo", headerFont, headerBg);
        addHeaderCell(table, "Categoría", headerFont, headerBg);
        addHeaderCell(table, "Descripción", headerFont, headerBg);

        Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 9);
        boolean alternate = false;
        BaseColor evenBg = new BaseColor(245, 245, 245);

        for (Event event : events) {
            BaseColor bg = alternate ? evenBg : BaseColor.WHITE;
            addCell(table, formatInstant(event.getTimestamp()), cellFont, bg);
            addCell(table, event.getType().getName(), cellFont, bg);
            addCell(table, event.getType().getCategory(), cellFont, bg);
            addCell(table, event.getDescription() != null ? event.getDescription() : "", cellFont, bg);
            alternate = !alternate;
        }

        document.add(table);
    }

    private void addRow(PdfPTable table, String label, String value) {
        Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
        Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 10);

        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPaddingBottom(4);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPaddingBottom(4);
        table.addCell(valueCell);
    }

    private void addHeaderCell(PdfPTable table, String text, Font font, BaseColor bg) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(bg);
        cell.setPadding(6);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }

    private void addCell(PdfPTable table, String text, Font font, BaseColor bg) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(bg);
        cell.setPadding(4);
        table.addCell(cell);
    }

    private Instant findEventDate(List<Event> events, String typeName) {
        return events.stream()
                .filter(e -> e.getType().getName().equals(typeName))
                .map(Event::getTimestamp)
                .findFirst()
                .orElse(null);
    }

    private long calculateDuration(Instant start, Instant end) {
        if (start == null) return 0;
        if (end == null) return Duration.between(start, Instant.now()).toDays();
        return Duration.between(start, end).toDays();
    }

    private String formatInstant(Instant instant) {
        if (instant == null) return "—";
        return ZonedDateTime.ofInstant(instant, ZONE).format(DATE_FMT);
    }

    private String getLotStatus(Lot lot) {
        Long lotId = lot.getId();
        boolean hasSowing = eventRepository.existsByLotIdAndTypeName(lotId, "SOWING");
        boolean hasHarvest = eventRepository.existsByLotIdAndTypeName(lotId, "HARVEST");
        if (!hasSowing) return "Creado";
        if (hasHarvest) return "Finalizado";
        return "En producción";
    }
}
