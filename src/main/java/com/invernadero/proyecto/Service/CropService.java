package com.invernadero.proyecto.Service;

import com.invernadero.proyecto.Dto.Request.CropRequest;
import com.invernadero.proyecto.Dto.response.CropResponse;
import com.invernadero.proyecto.Entity.Crop;
import com.invernadero.proyecto.Entity.CropEventType;
import com.invernadero.proyecto.Entity.EventType;
import com.invernadero.proyecto.Repository.CropEventTypeRepository;
import com.invernadero.proyecto.Repository.CropRepository;
import com.invernadero.proyecto.Repository.EventTypeRepository;
import com.invernadero.proyecto.mapper.CropMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio para la gestión de cultivos.
 * Proporciona operaciones CRUD y validación de datos para el catálogo de cultivos.
 */
@Service
@RequiredArgsConstructor
public class CropService {

    private final CropRepository cropRepository;
    private final EventTypeRepository eventTypeRepository;
    private final CropEventTypeRepository cropEventTypeRepository;
    private final SseService sseService;

    /**
     * Crea un nuevo cultivo en el sistema.
     * Automáticamente asigna todos los tipos de evento disponibles al nuevo cultivo.
     *
     * @param request datos del cultivo a crear (nombre, descripción, días de inactividad, días de crecimiento)
     * @return los datos del cultivo creado
     * @throws RuntimeException si ya existe un cultivo con el mismo nombre
     */
    @Transactional
    public CropResponse createCrop(CropRequest request) {

        if (cropRepository.findByName(request.getName()).isPresent()) {
            throw new RuntimeException("Ya existe un cultivo con ese nombre");
        }

        Crop crop = Crop.builder()
                .name(request.getName())
                .description(request.getDescription())
                .inactivityDaysThreshold(request.getInactivityDaysThreshold())
                .estimatedGrowthDays(request.getEstimatedGrowthDays())
                .irrigationFrequencyHours(request.getIrrigationFrequencyHours())
                .recommendedFertilizationDays(request.getRecommendedFertilizationDays())
                .recommendedPestControlDays(request.getRecommendedPestControlDays())
                .build();

        Crop savedCrop = cropRepository.save(crop);

        List<EventType> allTypes = eventTypeRepository.findAll();
        List<CropEventType> associations = allTypes.stream()
                .map(type -> CropEventType.builder()
                        .crop(savedCrop)
                        .eventType(type)
                        .build())
                .toList();
        cropEventTypeRepository.saveAll(associations);

        CropResponse response = CropMapper.toDTO(savedCrop);
        sseService.sendEvent("dashboard", "{\"type\":\"CROP_UPDATED\"}");
        return response;
    }

    /**
     * Obtiene los tipos de evento asociados a un cultivo.
     *
     * @param cropId identificador del cultivo
     * @return lista de tipos de evento asociados
     */
    public List<EventType> getEventTypesByCrop(Long cropId) {
        return cropEventTypeRepository.findByCropId(cropId)
                .stream()
                .map(CropEventType::getEventType)
                .toList();
    }

    /**
     * Agrega un tipo de evento a un cultivo.
     *
     * @param cropId      identificador del cultivo
     * @param eventTypeId identificador del tipo de evento
     */
    @Transactional
    public void addEventTypeToCrop(Long cropId, Long eventTypeId) {
        if (cropEventTypeRepository.existsByCropIdAndEventTypeId(cropId, eventTypeId)) {
            throw new RuntimeException("El tipo de evento ya está asociado a este cultivo");
        }

        Crop crop = cropRepository.findById(cropId)
                .orElseThrow(() -> new RuntimeException("Crop not found"));
        EventType eventType = eventTypeRepository.findById(eventTypeId)
                .orElseThrow(() -> new RuntimeException("Event type not found"));

        CropEventType association = CropEventType.builder()
                .crop(crop)
                .eventType(eventType)
                .build();
        cropEventTypeRepository.save(association);
    }

    /**
     * Remueve un tipo de evento de un cultivo.
     *
     * @param cropId      identificador del cultivo
     * @param eventTypeId identificador del tipo de evento
     */
    @Transactional
    public void removeEventTypeFromCrop(Long cropId, Long eventTypeId) {
        if (!cropEventTypeRepository.existsByCropIdAndEventTypeId(cropId, eventTypeId)) {
            throw new RuntimeException("El tipo de evento no está asociado a este cultivo");
        }
        cropEventTypeRepository.deleteByCropIdAndEventTypeId(cropId, eventTypeId);
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
    @Transactional
    public CropResponse updateCrop(Long id, CropRequest request) {

        Crop crop = cropRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Crop not found"));

        if (request.getName() != null && !request.getName().isBlank()) {
            if (!crop.getName().equals(request.getName()) &&
                cropRepository.findByName(request.getName()).isPresent()) {
                throw new RuntimeException("Ya existe un cultivo con ese nombre");
            }
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

        if (request.getIrrigationFrequencyHours() != null) {
            crop.setIrrigationFrequencyHours(request.getIrrigationFrequencyHours());
        }

        if (request.getRecommendedFertilizationDays() != null) {
            crop.setRecommendedFertilizationDays(request.getRecommendedFertilizationDays());
        }

        if (request.getRecommendedPestControlDays() != null) {
            crop.setRecommendedPestControlDays(request.getRecommendedPestControlDays());
        }

        CropResponse response = CropMapper.toDTO(cropRepository.save(crop));
        sseService.sendEvent("dashboard", "{\"type\":\"CROP_UPDATED\"}");
        return response;
    }

    /**
     * Elimina un cultivo por su identificador.
     *
     * @param id identificador del cultivo a eliminar
     */
    @Transactional
    public void deleteCrop(Long id) {
        cropRepository.deleteById(id);
        sseService.sendEvent("dashboard", "{\"type\":\"CROP_UPDATED\"}");
    }
}