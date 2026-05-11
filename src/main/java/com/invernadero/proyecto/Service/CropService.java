package com.invernadero.proyecto.Service;

import com.invernadero.proyecto.Dto.Request.CropRequest;
import com.invernadero.proyecto.Dto.response.CropResponse;
import com.invernadero.proyecto.Entity.Crop;
import com.invernadero.proyecto.Repository.CropRepository;
import com.invernadero.proyecto.mapper.CropMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Servicio para la gestión de cultivos.
 * Proporciona operaciones CRUD y validación de datos para el catálogo de cultivos.
 */
@Service
@RequiredArgsConstructor
public class CropService {

    private final CropRepository cropRepository;

    /**
     * Crea un nuevo cultivo en el sistema.
     *
     * @param request datos del cultivo a crear (nombre, descripción, días de inactividad, días de crecimiento)
     * @return los datos del cultivo creado
     * @throws RuntimeException si ya existe un cultivo con el mismo nombre
     */
    public CropResponse createCrop(CropRequest request) {


        if (cropRepository.findByName(request.getName()).isPresent()) {
            throw new RuntimeException("Ya existe un cultivo con ese nombre");
        }

        Crop crop = Crop.builder()
                .name(request.getName())
                .description(request.getDescription())
                .inactivityDaysThreshold(request.getInactivityDaysThreshold())
                .estimatedGrowthDays(request.getEstimatedGrowthDays())
                .build();

        return CropMapper.toDTO(cropRepository.save(crop));
    }

    /**
     * Obtiene un cultivo por su identificador.
     *
     * @param id identificador único del cultivo
     * @return los datos del cultivo encontrado
     * @throws RuntimeException si no se encuentra un cultivo con el ID especificado
     */
    public CropResponse getCropById(Long id) {

        Crop crop = cropRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Crop not found"));

        return CropMapper.toDTO(crop);
    }

    /**
     * Obtiene todos los cultivos registrados en el sistema.
     *
     * @return lista de todos los cultivos
     */
    public List<CropResponse> getAllCrops() {

        return cropRepository.findAll()
                .stream()
                .map(CropMapper::toDTO)
                .toList();
    }

    /**
     * Actualiza los datos de un cultivo existente.
     * Solo actualiza los campos proporcionados que no sean nulos.
     *
     * @param id      identificador del cultivo a actualizar
     * @param request datos a actualizar (nombre, descripción, umbrales de días)
     * @return los datos del cultivo actualizado
     * @throws RuntimeException si no se encuentra un cultivo con el ID especificado
     */
    public CropResponse updateCrop(Long id, CropRequest request) {

        Crop crop = cropRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Crop not found"));

        if (request.getName() != null && !request.getName().isBlank()) {
            crop.setName(request.getName());
        }

        if (request.getDescription() != null) {
            crop.setDescription(request.getDescription());
        }


        if (request.getInactivityDaysThreshold() != null) {
            crop.setInactivityDaysThreshold(request.getInactivityDaysThreshold());
        }


        if (request.getEstimatedGrowthDays() != null) {
            crop.setEstimatedGrowthDays(request.getEstimatedGrowthDays());
        }

        return CropMapper.toDTO(cropRepository.save(crop));
    }

    /**
     * Elimina un cultivo por su identificador.
     *
     * @param id identificador del cultivo a eliminar
     */
    public void deleteCrop(Long id) {
        cropRepository.deleteById(id);
    }
}