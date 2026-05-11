package com.invernadero.proyecto.Service;

import com.invernadero.proyecto.Entity.EventType;
import com.invernadero.proyecto.Repository.EventTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Servicio para la gestión de tipos de eventos.
 * Proporciona consultas para el catálogo de tipos de eventos disponibles en el sistema.
 */
@Service
@RequiredArgsConstructor
public class EventTypeService {

    private final EventTypeRepository eventTypeRepository;

    /**
     * Obtiene todos los tipos de eventos disponibles en el sistema.
     *
     * @return lista completa de tipos de eventos
     */
    public List<EventType> getAllEventTypes() {
        return eventTypeRepository.findAll();
    }

    /**
     * Obtiene un tipo de evento por su identificador.
     *
     * @param id identificador único del tipo de evento
     * @return el tipo de evento encontrado
     * @throws RuntimeException si no se encuentra un tipo de evento con el ID especificado
     */
    public EventType getEventTypeById(Long id) {
        return eventTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event type not found"));
    }

    /**
     * Obtiene un tipo de evento por su nombre.
     *
     * @param name nombre del tipo de evento (ej: SOWING, HARVEST, WATERING)
     * @return el tipo de evento encontrado
     * @throws RuntimeException si no se encuentra un tipo de evento con el nombre especificado
     */
    public EventType getEventTypeByName(String name) {
        return eventTypeRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("Event type not found"));
    }
}