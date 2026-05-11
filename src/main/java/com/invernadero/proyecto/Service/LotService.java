package com.invernadero.proyecto.Service;

import com.invernadero.proyecto.Dto.Request.CropRequest;
import com.invernadero.proyecto.Dto.Request.LotRequest;
import com.invernadero.proyecto.Dto.response.LotResponse;
import com.invernadero.proyecto.Dto.response.LotSummary;
import com.invernadero.proyecto.Entity.Crop;
import com.invernadero.proyecto.Entity.Event;
import com.invernadero.proyecto.Entity.Lot;
import com.invernadero.proyecto.Repository.CropRepository;
import com.invernadero.proyecto.Repository.EventRepository;
import com.invernadero.proyecto.Repository.LotRepository;
import com.invernadero.proyecto.mapper.LotMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Servicio para la gestión de lotes de cultivo.
 * Maneja operaciones CRUD y cálculos de métricas agrícolas como progreso,
 * duración, frecuencia de eventos y estados de inactividad.
 */
@Service
@RequiredArgsConstructor
public class LotService {

    private final LotRepository lotRepository;
    private final CropRepository cropRepository;
    private final EventRepository eventRepository;

    /**
     * Crea un nuevo lote asociado a un cultivo existente.
     *
     * @param request datos del lote a crear (nombre, cultivo, fechas de inicio/fin)
     * @return los datos del lote creado
     * @throws RuntimeException si no existe un cultivo con el ID especificado
     */
    public LotResponse createLot(LotRequest request) {

        Crop crop = cropRepository.findById(request.getCropId())
                .orElseThrow(() -> new RuntimeException("Crop not found"));

        Lot lot = Lot.builder()
                .name(request.getName())
                .crop(crop)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .build();

        return LotMapper.toDTO(lotRepository.save(lot));
    }

    /**
     * Obtiene un lote por su identificador.
     *
     * @param id identificador único del lote
     * @return los datos del lote encontrado
     * @throws RuntimeException si no se encuentra un lote con el ID especificado
     */
    public LotResponse getLotById(Long id) {
        return lotRepository.findById(id)
                .map(LotMapper::toDTO)
                .orElseThrow(() -> new RuntimeException("Lot not found"));
    }

    /**
     * Obtiene todos los lotes registrados en el sistema.
     *
     * @return lista de todos los lotes
     */
    public List<LotResponse> getAllLots() {
        return lotRepository.findAll()
                .stream()
                .map(LotMapper::toDTO)
                .toList();
    }

    /**
     * Obtiene todos los lotes asociados a un cultivo específico.
     *
     * @param cropId identificador del cultivo
     * @return lista de lotes del cultivo especificado
     */
    public List<LotResponse> getLotsByCrop(Long cropId) {
        return lotRepository.findByCropId(cropId)
                .stream()
                .map(LotMapper::toDTO)
                .toList();
    }

    /**
     * Actualiza los datos de un lote existente.
     * Solo actualiza los campos proporcionados que no sean nulos.
     *
     * @param id      identificador del lote a actualizar
     * @param request datos a actualizar (nombre, fechas de inicio/fin)
     * @return los datos del lote actualizado
     * @throws RuntimeException si no se encuentra un lote con el ID especificado
     */
    public LotResponse updateLot(Long id, LotRequest request) {

        Lot lot = lotRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lot not found"));

        if (request.getName() != null) {
            lot.setName(request.getName());
        }

        if (request.getStartDate() != null) {
            lot.setStartDate(request.getStartDate());
        }

        if (request.getEndDate() != null) {
            lot.setEndDate(request.getEndDate());
        }

        return LotMapper.toDTO(lotRepository.save(lot));
    }

    /**
     * Elimina un lote por su identificador.
     *
     * @param id identificador del lote a eliminar
     */
    public void deleteLot(Long id) {
        lotRepository.deleteById(id);
    }

    /**
     * Determina el estado actual de un lote basado en sus eventos.
     *
     * @param lotId identificador del lote
     * @return estado del lote: CREATED (sin siembra), IN_PRODUCTION (siembra sin cosecha),
     *         o FINISHED (ya cosechado)
     */
    public String getLotStatus(Long lotId) {

        boolean hasSowing = eventRepository.existsByLotIdAndTypeName(lotId, "SOWING");
        boolean hasHarvest = eventRepository.existsByLotIdAndTypeName(lotId, "HARVEST");

        if (!hasSowing) return "CREATED";
        if (hasHarvest) return "FINISHED";

        return "IN_PRODUCTION";
    }

    /**
     * Cuenta el número total de eventos registrados en un lote.
     *
     * @param lotId identificador del lote
     * @return cantidad de eventos del lote
     */
    public long countEvents(Long lotId) {
        return eventRepository.findByLotId(lotId).size();
    }

    /**
     * Calcula la duración en días de un lote.
     * Mide desde la fecha de siembra hasta la cosecha, o hasta el momento actual
     * si aún no se ha cosechado.
     *
     * @param lotId identificador del lote
     * @return número de días transcurridos entre siembra y cosecha
     *         (o hasta ahora si no hay cosecha)
     */
    public long calculateDurationInDays(Long lotId) {

        List<Event> events = eventRepository.findByLotIdOrderByTimestampAsc(lotId);

        Instant sowingDate = null;
        Instant harvestDate = null;

        for (Event event : events) {

            if (event.getType().getName().equals("SOWING")) {
                sowingDate = event.getTimestamp();
            }

            if (event.getType().getName().equals("HARVEST")) {
                harvestDate = event.getTimestamp();
            }
        }

        if (sowingDate == null) return 0;

        if (harvestDate == null) {
            return Duration.between(sowingDate, Instant.now()).toDays();
        }

        return Duration.between(sowingDate, harvestDate).toDays();
    }

    /**
     * Calcula la frecuencia promedio de eventos por día en un lote.
     *
     * @param lotId identificador del lote
     * @return número promedio de eventos por día
     */
    public double calculateEventFrequency(Long lotId) {

        long totalEvents = countEvents(lotId);
        long days = calculateDurationInDays(lotId);

        if (days == 0) return totalEvents;

        return (double) totalEvents / days;
    }

    /**
     * Determina el estado de inactividad de un lote basado en el tiempo
     * transcurrido desde el último evento y el umbral de inactividad del cultivo.
     *
     * @param lotId identificador del lote
     * @return estado de inactividad: GREEN (activo), YELLOW (cerca del umbral),
     *         RED (umbral superado), GRAY (sin eventos), UNKNOWN (sin umbral definido)
     */
    public String getInactivityStatus(Long lotId) {

        Lot lot = lotRepository.findById(lotId)
                .orElseThrow(() -> new RuntimeException("Lot not found"));

        List<Event> events = eventRepository.findByLotIdOrderByTimestampDesc(lotId);

        if (events.isEmpty()) return "GRAY";

        Event lastEvent = events.get(0);

        long daysWithoutEvents = Duration.between(lastEvent.getTimestamp(), Instant.now()).toDays();

        Integer threshold = lot.getCrop().getInactivityDaysThreshold();

        if (threshold == null) return "UNKNOWN";

        if (daysWithoutEvents >= threshold) return "RED";
        if (daysWithoutEvents >= threshold / 2) return "YELLOW";

        return "GREEN";
    }

    /**
     * Calcula el progreso actual del cultivo como porcentaje.
     *
     * @param lotId identificador del lote
     * @return porcentaje de progreso (0-100), basado en días transcurridos
     *         desde la siembra vs días totales estimados de crecimiento
     */
    public double getCropProgress(Long lotId) {

        Lot lot = lotRepository.findById(lotId)
                .orElseThrow(() -> new RuntimeException("Lot not found"));

        if (lot.getEstimatedHarvestDate() == null) return 0;

        List<Event> events = eventRepository.findByLotIdOrderByTimestampAsc(lotId);

        Instant sowingDate = null;

        for (Event e : events) {
            if (e.getType().getName().equals("SOWING")) {
                sowingDate = e.getTimestamp();
                break;
            }
        }

        if (sowingDate == null) return 0;

        long totalDays = Duration.between(sowingDate, lot.getEstimatedHarvestDate()).toDays();
        long currentDays = Duration.between(sowingDate, Instant.now()).toDays();

        if (totalDays <= 0) return 100;

        double progress = (double) currentDays / totalDays * 100;

        return Math.min(progress, 100);
    }

    /**
     * Obtiene la fecha estimada de cosecha para un lote.
     *
     * @param lotId identificador del lote
     * @return fecha estimada de cosecha, o null si no se ha establecido
     * @throws RuntimeException si no se encuentra el lote
     */
    public Instant getEstimatedHarvestDate(Long lotId) {

        Lot lot = lotRepository.findById(lotId)
                .orElseThrow(() -> new RuntimeException("Lot not found"));

        return lot.getEstimatedHarvestDate();
    }

    /**
     * Obtiene la cantidad de eventos por día de los últimos 7 días.
     *
     * @return mapa con fechas (yyyy-MM-dd) como clave y cantidad de eventos como valor
     */
    public Map<String, Long> getEventsLast7Days() {

        Instant start = Instant.now().minus(Duration.ofDays(7));

        List<Object[]> results = eventRepository.countEventsByDay(start);

        Map<String, Long> response = new LinkedHashMap<>();

        for (Object[] row : results) {
            response.put(row[0].toString(), (Long) row[1]);
        }

        return response;
    }

    /**
     * Genera un resumen completo de un lote con todas sus métricas.
     *
     * @param lotId identificador del lote
     * @return resumen con estado, métricas de tiempo, eventos y progreso
     * @throws RuntimeException si no se encuentra el lote
     */
    public LotSummary getLotSummary(Long lotId) {
        Lot lot = lotRepository.findById(lotId)
                .orElseThrow(() -> new RuntimeException("Lot not found"));
        List<Event> events = eventRepository.findByLotIdOrderByTimestampDesc(lotId);
        Instant lastEventDate = null;
        String lastEventType = null;
        if (!events.isEmpty()) {
            lastEventDate = events.get(0).getTimestamp();
            lastEventType = events.get(0).getType().getName();
        }
        Map<String, Object> details = getLotProgressDetails(lotId);

        Object sowingDateRaw = details.get("sowingDate");
        String sowingDateStr = (sowingDateRaw == null) ? "null" : sowingDateRaw.toString();

        Instant harvestInstant = lot.getEstimatedHarvestDate();
        String harvestDateStr = (harvestInstant == null) ? "null" : harvestInstant.toString();

        return LotSummary.builder()
                .lotId(lot.getId())
                .lotName(lot.getName())
                .status(getLotStatus(lotId))
                .inactivityStatus(getInactivityStatus(lotId))
                .totalEvents(countEvents(lotId))
                .durationDays(calculateDurationInDays(lotId))
                .eventFrequency(calculateEventFrequency(lotId))
                .sowingDate(sowingDateStr)
                .totalDays((int) details.get("totalDays"))
                .daysElapsed((int) details.get("daysElapsed"))
                .daysRemaining((int) details.get("daysRemaining"))
                .estimatedHarvestDate(harvestDateStr)
                .lastEventDate(lastEventDate)
                .lastEventType(lastEventType)
                .build();
    }

    /**
     * Obtiene los detalles del progreso de un lote.
     * Incluye fecha de siembra, días totales estimados, días transcurridos y días restantes.
     *
     * @param lotId identificador del lote
     * @return mapa con claves: sowingDate, totalDays, daysElapsed, daysRemaining
     * @throws RuntimeException si no se encuentra el lote
     */
    public Map<String, Object> getLotProgressDetails(Long lotId) {
        Lot lot = lotRepository.findById(lotId)
                .orElseThrow(() -> new RuntimeException("Lot not found"));
        Instant sowingDate = null;
        List<Event> events = eventRepository.findByLotIdOrderByTimestampAsc(lotId);
        for (Event e : events) {
            if (e.getType().getName().equals("SOWING")) {
                sowingDate = e.getTimestamp();
                break;
            }
        }
        int totalDays = lot.getCrop() != null ? lot.getCrop().getEstimatedGrowthDays() : 0;
        int daysElapsed = 0;
        int daysRemaining = 0;
        if (sowingDate != null && lot.getEstimatedHarvestDate() != null) {
            daysElapsed = (int) Duration.between(sowingDate, Instant.now()).toDays();
            long totalDaysCalc = Duration.between(sowingDate, lot.getEstimatedHarvestDate()).toDays();
            totalDays = (int) totalDaysCalc;
            daysRemaining = Math.max(0, totalDays - daysElapsed);
        }
        return Map.of(
                "sowingDate", sowingDate,
                "totalDays", totalDays,
                "daysElapsed", daysElapsed,
                "daysRemaining", daysRemaining
        );
    }


}