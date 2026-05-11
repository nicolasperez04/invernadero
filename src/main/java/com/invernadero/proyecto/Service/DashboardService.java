package com.invernadero.proyecto.Service;

import com.invernadero.proyecto.Dto.response.DashboardResponse;
import com.invernadero.proyecto.Dto.response.EventChartDTO;
import com.invernadero.proyecto.Dto.response.LotProgressDTO;
import com.invernadero.proyecto.Dto.response.LotStatusDTO;
import com.invernadero.proyecto.Entity.Lot;
import com.invernadero.proyecto.Repository.EventRepository;
import com.invernadero.proyecto.Repository.LotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Servicio para la generación del dashboard del sistema SIGMA.
 * Proporciona una vista consolidada con estados de lotes, progreso de cultivos
 * y gráficos de eventos de los últimos 30 días.
 */
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final EventRepository eventRepository;
    private final LotRepository lotRepository;
    private final LotService lotService;

    /**
     * Genera una respuesta completa del dashboard.
     * Incluye estados de lotes, progreso de cultivos y gráfico de eventos
     * de los últimos 30 días, opcionalmente filtrado por cultivo.
     *
     * @param cropId identificador del cultivo para filtrar (null para mostrar todos)
     * @return objeto DashboardResponse con estados, progreso y gráfico de eventos
     */
    public DashboardResponse getDashboard(Long cropId) {
        return DashboardResponse.builder()
                .eventChart(buildEventChart(cropId))
                .lotStatuses(buildLotStatuses(cropId))
                .lotProgress(buildLotProgress(cropId))
                .build();
    }

    /**
     * Construye el gráfico de eventos de los últimos 30 días.
     * Genera un mapa de fechas con conteo de eventos, inicializando todos
     * los días con cero y luego actualizando con los datos reales.
     *
     * @param cropId identificador del cultivo para filtrar (null para todos)
     * @return DTO con etiquetas (fechas) y valores (conteo de eventos)
     */
    private EventChartDTO buildEventChart(Long cropId) {

        Instant start = Instant.now().minus(Duration.ofDays(30));

        List<Object[]> results = eventRepository.countEventsByDay(start, cropId);

        Map<String, Long> map = new LinkedHashMap<>();

        for (int i = 29; i >= 0; i--) {
            String date = LocalDate.now().minusDays(i).toString();
            map.put(date, 0L);
        }

        for (Object[] row : results) {
            map.put(row[0].toString(), (Long) row[1]);
        }

        return EventChartDTO.builder()
                .labels(new ArrayList<>(map.keySet()))
                .values(new ArrayList<>(map.values()))
                .build();
    }

    /**
     * Obtiene la lista de lotes según el filtro de cultivo.
     *
     * @param cropId identificador del cultivo (null para todos los lotes)
     * @return lista de entidades Lot
     */
    private List<Lot> getLots(Long cropId) {

        if (cropId == null) {
            return lotRepository.findAll();
        }

        return lotRepository.findByCropId(cropId);
    }

    /**
     * Construye la lista de estados de lotes con su estado actual e
     * nivel de inactividad.
     *
     * @param cropId identificador del cultivo para filtrar (null para todos)
     * @return lista de estados de lotes
     */
    private List<LotStatusDTO> buildLotStatuses(Long cropId) {

        return getLots(cropId)
                .stream()
                .map(lot -> LotStatusDTO.builder()
                        .lotId(lot.getId())
                        .lotName(lot.getName())
                        .status(lotService.getLotStatus(lot.getId()))
                        .inactivityLevel(lotService.getInactivityStatus(lot.getId()))
                        .build())
                .toList();
    }

    /**
     * Construye la lista de progreso de lotes con métricas de tiempo
     * y progreso del cultivo.
     *
     * @param cropId identificador del cultivo para filtrar (null para todos)
     * @return lista de progreso de lotes
     */
    private List<LotProgressDTO> buildLotProgress(Long cropId) {
        return getLots(cropId).stream().map(lot -> {
            double progress = lotService.getCropProgress(lot.getId());
            Map<String, Object> details = lotService.getLotProgressDetails(lot.getId());
            Instant harvestInstant = lotService.getEstimatedHarvestDate(lot.getId());
            String harvestDateStr = harvestInstant != null ? harvestInstant.toString() : "null";
            return LotProgressDTO.builder()
                    .lotId(lot.getId())
                    .lotName(lot.getName())
                    .progress(progress)
                    .estimatedHarvestDate(harvestDateStr)
                    .sowingDate(String.valueOf((Instant) details.get("sowingDate")))
                    .totalDays((int) details.get("totalDays"))
                    .daysElapsed((int) details.get("daysElapsed"))
                    .daysRemaining((int) details.get("daysRemaining"))
                    .build();
        }).toList();
    }

}