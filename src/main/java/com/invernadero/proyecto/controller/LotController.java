package com.invernadero.proyecto.controller;

import com.invernadero.proyecto.Dto.Request.LotRequest;
import com.invernadero.proyecto.Dto.response.LotResponse;
import com.invernadero.proyecto.Dto.response.LotSummary;
import com.invernadero.proyecto.Service.LotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/lots")
@RequiredArgsConstructor
@Tag(name = "Lote", description = "Endpoints para gestionar los lotes de cultivo")
@SecurityRequirement(name = "bearer-jwt")
public class LotController {

    private final LotService loteService;

    @Operation(summary = "Crear un nuevo lote de cultivo", description = "Permite a los usuarios con rol ADMIN o OPERATOR crear un nuevo lote de cultivo.")
    @ApiResponse(responseCode = "200", description = "Lote creado exitosamente")
    @ApiResponse(responseCode = "400", description = "Solicitud inválida")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
    @PostMapping
    public LotResponse createLot(@Valid @RequestBody LotRequest request) {
        return loteService.createLot(request);
    }


    @Operation(summary = "Obtener un lote por ID", description = "Permite a los usuarios con rol ADMIN, OPERATOR o VIEWER obtener los detalles de un lote de cultivo específico utilizando su ID.")
    @ApiResponse(responseCode = "200", description = "Lote obtenido exitosamente")
    @ApiResponse(responseCode = "404", description = "Lote no encontrado")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR','VIEWER')")
    @GetMapping("/{id}")
    public LotResponse getLotById(@PathVariable Long id) {
        return loteService.getLotById(id);
    }

    @Operation(summary = "Obtener todos los lotes de cultivo", description = "Permite a los usuarios con rol ADMIN, OPERATOR o VIEWER obtener una lista de todos los lotes de cultivo disponibles.")
    @ApiResponse(responseCode = "200", description = "Lotes obtenidos exitosamente")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR','VIEWER')")
    @GetMapping
    public List<LotResponse> getAllLots() {
        return loteService.getAllLots();
    }

    @Operation(summary = "Obtener lotes por cultivo", description = "Permite a los usuarios con rol ADMIN, OPERATOR o VIEWER obtener una lista de lotes de cultivo asociados a un cultivo específico utilizando su ID.")
    @ApiResponse(responseCode = "200", description = "Lotes obtenidos exitosamente)")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR','VIEWER')")
    @GetMapping("/crop/{cropId}")
    public List<LotResponse> getLotsByCrop(@PathVariable Long cropId) {
        return loteService.getLotsByCrop(cropId);
    }

    @Operation(summary = "Actualizar el nombre de un lote de cultivo", description = "Permite a los usuarios con rol ADMIN o OPERATOR actualizar el nombre de un lote de cultivo específico utilizando su ID.")
    @ApiResponse(responseCode = "200", description = "Lote actualizado exitosamente")
    @ApiResponse(responseCode = "404", description = "Lote no encontrado")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
    @PutMapping("/{id}")
    public LotResponse updateLot(@PathVariable Long id,
                                    LotRequest request) {
        return loteService.updateLot(id, request);
    }


    @Operation(summary = "Eliminar un lote de cultivo", description = "Permite a los usuarios con rol ADMIN eliminar un lote de cultivo específico utilizando su ID.")
    @ApiResponse(responseCode = "200", description = "Lote eliminado exitosamente")
    @ApiResponse(responseCode = "404", description = "Lote no encontrado")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public void deleteLot(@PathVariable Long id) {
        loteService.deleteLot(id);
    }

    @Operation(summary = "Obtener el resumen de un lote de cultivo", description = "Permite a los usuarios con rol ADMIN, OPERATOR o VIEWER obtener un resumen del estado actual de un lote de cultivo específico utilizando su ID.")
    @ApiResponse(responseCode = "200", description = "Resumen del lote obtenido exitosamente")
    @ApiResponse(responseCode = "404", description = "Lote no encontrado")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR','VIEWER')")
    @GetMapping("/{id}/summary")
    public LotSummary getSummary(@PathVariable Long id) {
        return loteService.getLotSummary(id);
    }


}
