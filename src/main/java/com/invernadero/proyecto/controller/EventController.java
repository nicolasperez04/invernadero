package com.invernadero.proyecto.controller;

import com.invernadero.proyecto.Dto.Request.EventRequest;
import com.invernadero.proyecto.Dto.response.EventResponse;
import com.invernadero.proyecto.Service.EventService;
import com.invernadero.proyecto.mapper.EventMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
@Tag(name = "Eventos", description = "Gestión de eventos relacionados con los lotes")
@SecurityRequirement(name = "bearer-jwt")
public class EventController {

    private final EventService eventService;

    @Operation(summary = "Registrar evento", description = "Registra un nuevo evento para un lote específico")
    @ApiResponse(responseCode = "200", description = "Evento registrado exitosamente")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
    @PostMapping
    public EventResponse createEvent(@Valid @RequestBody EventRequest request) {
        return eventService.registerEvent(request);
    }


    @Operation(summary = "Obtener evento por ID", description = "Obtiene los detalles de un evento específico por su ID")
    @ApiResponse(responseCode = "200", description = "Evento obtenido exitosamente")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR','VIEWER')")
    @GetMapping("/{id}")
    public EventResponse getEventById(@PathVariable Long id) {
        return eventService.getEventById(id);
    }


    @Operation(summary = "Listar eventos", description = "Obtiene una lista de todos los eventos registrados en el sistema")
    @ApiResponse(responseCode = "200", description = "Eventos obtenidos exitosamente")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR','VIEWER')")
    @GetMapping
    public List<EventResponse> getAllEvents() {
        return eventService.getAllEvents();
    }


    @Operation(summary = "Obtener eventos por lote", description = "Obtiene una lista de eventos asociados a un lote específico")
    @ApiResponse(responseCode = "200", description = "Eventos obtenidos exitosamente")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR','VIEWER')")
    @GetMapping("/lot/{lotId}")
    public List<EventResponse> getEventsByLot(@PathVariable Long lotId) {
        return eventService.getEventsByLot(lotId);
    }

    @Operation(summary = "Obtener historial de eventos por lote", description = "Obtiene el historial completo de eventos asociados a un lote específico, ordenados por fecha")
    @ApiResponse(responseCode = "200", description = "Historial de eventos obtenido exitosamente")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR','VIEWER')")
    @GetMapping("/lot/{lotId}/history")
    public List<EventResponse> getEventHistory(@PathVariable Long lotId) {
        return eventService.getEventsByLot(lotId);
    }



    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR','VIEWER')")
    @GetMapping("/filter")
    public List<EventResponse> filterEvents(
            @RequestParam Long lotId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Instant startDate,
            @RequestParam(required = false) Instant endDate
    ) {
        return eventService.filterEvents(lotId, type, startDate, endDate)
                .stream()
                .map(EventMapper::toDTO)
                .toList();
    }

}
