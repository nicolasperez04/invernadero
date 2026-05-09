package com.invernadero.proyecto.controller;

import com.invernadero.proyecto.Dto.Request.CropRequest;
import com.invernadero.proyecto.Dto.response.CropResponse;
import com.invernadero.proyecto.Service.CropService;
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
@RequestMapping("/api/crops")
@RequiredArgsConstructor
@Tag(name = "Cultivos", description = "Gestión de cultivos del sistema")
@SecurityRequirement(name = "bearer-jwt")
public class CropController {

    private final CropService cropService;

    @Operation(summary = "Crear cultivo", description = "Registra un nuevo cultivo en el sistema")
    @ApiResponse(responseCode = "200", description = "Cultivo creado exitosamente")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
    public CropResponse create(@Valid @RequestBody CropRequest request) {
        return cropService.createCrop(request);
    }

    @Operation(summary = "Obtener cultivo por ID")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR','VIEWER')")
    @GetMapping("/{id}")
    public CropResponse getById(@PathVariable Long id) {
        return cropService.getCropById(id);
    }

    @Operation(summary = "Listar cultivos")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR','VIEWER')")
    @GetMapping
    public List<CropResponse> getAll() {
        return cropService.getAllCrops();
    }

    @Operation(summary = "Actualizar cultivo")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public CropResponse update(@PathVariable Long id,
                                  @Valid @RequestBody CropRequest request) {
        return cropService.updateCrop(id, request);
    }

    @Operation(summary = "Eliminar cultivo")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        cropService.deleteCrop(id);
    }

}
