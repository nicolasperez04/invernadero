package com.invernadero.proyecto.controller;

import com.invernadero.proyecto.Entity.EventType;
import com.invernadero.proyecto.Service.EventTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/event-types")
@Tag(name = "Tipos de Evento", description = "Gestión de tipos de eventos relacionados con los lotes")
@SecurityRequirement(name = "bearer-jwt")
public class EventTypeController {

    private final EventTypeService eventTypeService;


    @Operation(summary = "Listar tipos de eventos", description = "Obtiene una lista de todos los tipos de eventos registrados en el sistema")
    @ApiResponse(responseCode = "200", description = "Tipos de eventos obtenidos exitosamente")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR','VIEWER')")
    @GetMapping
    public List<EventType> getAllEventTypes() {
        return eventTypeService.getAllEventTypes();
    }

    @Operation(summary = "Obtener tipo de evento por ID", description = "Obtiene los detalles de un tipo de evento específico por su ID")
    @ApiResponse(responseCode = "200", description = "Tipo de evento obtenido exitosamente")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR','VIEWER')")
    @GetMapping("/{id}")
    public EventType getEventTypeById(@PathVariable Long id) {
        return eventTypeService.getEventTypeById(id);
    }

    @Operation(summary = "Obtener tipo de evento por nombre", description = "Obtiene los detalles de un tipo de evento específico por su nombre")
    @ApiResponse(responseCode = "200", description = "Tipo de evento obtenido exitosamente")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR','VIEWER')")
    @GetMapping("/name/{name}")
    public EventType getEventTypeByName(@PathVariable String name) {
        return eventTypeService.getEventTypeByName(name);
    }

}
