package com.invernadero.proyecto.Service;

import com.invernadero.proyecto.Entity.EventType;
import com.invernadero.proyecto.Repository.EventTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventTypeService {

    private final EventTypeRepository eventTypeRepository;

    // =========================
    // READ
    // =========================

    public List<EventType> getAllEventTypes() {
        return eventTypeRepository.findAll();
    }

    public EventType getEventTypeById(Long id) {
        return eventTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event type not found"));
    }

    public EventType getEventTypeByName(String name) {
        return eventTypeRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("Event type not found"));
    }
}
