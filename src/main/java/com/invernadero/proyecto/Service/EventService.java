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

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final LotRepository lotRepository;
    private final EventTypeRepository eventTypeRepository;
    private final UserRepository userRepository;

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

        // 🌱 NUEVO → si es siembra, calcular fecha estimada
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


    public EventResponse getEventById(Long id) {
        return eventRepository.findById(id)
                .map(EventMapper::toDTO)
                .orElseThrow(() -> new RuntimeException("Event not found"));
    }

    public List<EventResponse> getAllEvents() {
        return eventRepository.findAll()
                .stream()
                .map(EventMapper::toDTO)
                .toList();
    }

    public List<EventResponse> getEventsByLot(Long lotId) {

        return eventRepository.findByLotIdOrderByTimestampAsc(lotId)
                .stream()
                .map(EventMapper::toDTO)
                .toList();
    }

    public List<Event> getEventHistoryByLot(Long lotId) {
        return eventRepository.findByLotIdOrderByTimestampAsc(lotId);
    }

    public List<Event> filterEvents(Long lotId, String type, Instant startDate, Instant endDate) {
        return eventRepository.filterEvents(lotId, type, startDate, endDate);
    }

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
