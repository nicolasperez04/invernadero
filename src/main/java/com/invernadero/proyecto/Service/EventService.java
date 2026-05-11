package com.invernadero.proyecto.Service;

import com.invernadero.proyecto.Dto.Request.EventRequest;
import com.invernadero.proyecto.Dto.response.EventResponse;
import com.invernadero.proyecto.Entity.Event;
import com.invernadero.proyecto.Entity.EventType;
import com.invernadero.proyecto.Entity.Lot;
import com.invernadero.proyecto.Entity.User;
import com.invernadero.proyecto.Repository.EventRepository;
import com.invernadero.proyecto.Repository.EventTypeRepository;
import com.invernadero.proyecto.Repository.LotRepository;
import com.invernadero.proyecto.Repository.UserRepository;
import com.invernadero.proyecto.mapper.EventMapper;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Servicio para la gestión de eventos en lotes.
 * Maneja el registro, consulta y validación de eventos agrícolas (siembra, cosecha, etc.).
 */
@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final LotRepository lotRepository;
    private final EventTypeRepository eventTypeRepository;
    private final UserRepository userRepository;

    /**
     * Registra un nuevo evento en un lote.
     * Valida la existencia del lote, tipo de evento y usuario. Si el evento es de tipo SOWING,
     * calcula y asigna la fecha estimada de cosecha basándose en los días de crecimiento del cultivo.
     *
     * @param request datos del evento a registrar (lote, tipo, timestamp, descripción)
     * @return los datos del evento registrado
     * @throws RuntimeException si el lote, tipo de evento o usuario no existen,
     *                          o si se viola la secuencia válida de eventos
     */
    public EventResponse registerEvent(EventRequest request) {

        validateEvent(request.getLotId(), request.getType(), request.getTimestamp());

        Lot lot = lotRepository.findById(request.getLotId())
                .orElseThrow(() -> new RuntimeException("Lot not found"));

        EventType type = eventTypeRepository.findByName(request.getType())
                .orElseThrow(() -> new RuntimeException("Event type not found"));

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        validateEventSequence(lot.getId(), type.getName());

        Event event = Event.builder()
                .lot(lot)
                .type(type)
                .user(user)
                .timestamp(request.getTimestamp())
                .description(request.getDescription())
                .createdAt(Instant.now())
                .build();

        Event saved = eventRepository.save(event);

        if (type.getName().equals("SOWING")) {

            Integer days = lot.getCrop().getEstimatedGrowthDays();

            if (days != null) {
                Instant estimatedHarvest = request.getTimestamp().plus(Duration.ofDays(days));
                lot.setEstimatedHarvestDate(estimatedHarvest);
                lotRepository.save(lot);
            }
        }

        return EventMapper.toDTO(saved);
    }

    /**
     * Obtiene un evento por su identificador.
     *
     * @param id identificador único del evento
     * @return los datos del evento encontrado
     * @throws RuntimeException si no se encuentra un evento con el ID especificado
     */
    public EventResponse getEventById(Long id) {
        return eventRepository.findById(id)
                .map(EventMapper::toDTO)
                .orElseThrow(() -> new RuntimeException("Event not found"));
    }

    /**
     * Obtiene todos los eventos registrados en el sistema.
     *
     * @return lista de todos los eventos
     */
    public List<EventResponse> getAllEvents() {
        return eventRepository.findAll()
                .stream()
                .map(EventMapper::toDTO)
                .toList();
    }

    /**
     * Obtiene todos los eventos de un lote específico, ordenados por timestamp ascendente.
     *
     * @param lotId identificador del lote
     * @return lista de eventos del lote ordenados cronológicamente
     */
    public List<EventResponse> getEventsByLot(Long lotId) {

        return eventRepository.findByLotIdOrderByTimestampAsc(lotId)
                .stream()
                .map(EventMapper::toDTO)
                .toList();
    }

    /**
     * Obtiene el historial completo de eventos de un lote (entidades Event sin convertir a DTO).
     *
     * @param lotId identificador del lote
     * @return lista de entidades Event del lote ordenadas por timestamp
     */
    public List<Event> getEventHistoryByLot(Long lotId) {
        return eventRepository.findByLotIdOrderByTimestampAsc(lotId);
    }

    /**
     * Filtra eventos por múltiples criterios: lote, tipo de evento y rango de fechas.
     *
     * @param lotId     identificador del lote (puede ser null)
     * @param type      nombre del tipo de evento (puede ser null)
     * @param startDate fecha de inicio del rango (puede ser null)
     * @param endDate   fecha de fin del rango (puede ser null)
     * @return lista de eventos que cumplen todos los criterios especificados
     */
    public List<Event> filterEvents(Long lotId, String type, Instant startDate, Instant endDate) {
        return eventRepository.filterEvents(lotId, type, startDate, endDate);
    }

    /**
     * Valida que los datos básicos del evento sean correctos.
     *
     * @param lotId     identificador del lote
     * @param typeName  nombre del tipo de evento
     * @param timestamp fecha y hora del evento
     * @throws RuntimeException si algún campo requerido es nulo o está en blanco
     */
    private void validateEvent(Long lotId, String typeName, Instant timestamp) {

        if (lotId == null) {
            throw new RuntimeException("Lot is required");
        }

        if (typeName == null || typeName.isBlank()) {
            throw new RuntimeException("Event type is required");
        }

        if (timestamp == null) {
            throw new RuntimeException("Timestamp is required");
        }
    }

    /**
     * Valida que la secuencia de eventos sea correcta para un lote.
     * Reglas: no se puede cosechar sin haber sembrado, no se puede sembrar dos veces,
     * y no se pueden agregar eventos a un lote ya cosechado.
     *
     * @param lotId     identificador del lote
     * @param eventType tipo de evento que se intenta registrar
     * @throws RuntimeException si la secuencia de eventos es inválida
     */
    private void validateEventSequence(Long lotId, String eventType) {

        boolean hasSowing = eventRepository.existsByLotIdAndTypeName(lotId, "SOWING");
        boolean hasHarvest = eventRepository.existsByLotIdAndTypeName(lotId, "HARVEST");

        if (eventType.equals("HARVEST") && !hasSowing) {
            throw new RuntimeException("Cannot register harvest before sowing");
        }

        if (eventType.equals("SOWING") && hasSowing) {
            throw new RuntimeException("Sowing already exists for this lot");
        }

        if (hasHarvest) {
            throw new RuntimeException("This lot is already finished");
        }
    }

}