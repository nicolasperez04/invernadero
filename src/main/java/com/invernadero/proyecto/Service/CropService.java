package com.invernadero.proyecto.Service;

import com.invernadero.proyecto.Dto.Request.CropRequest;
import com.invernadero.proyecto.Dto.response.CropResponse;
import com.invernadero.proyecto.Entity.Crop;
import com.invernadero.proyecto.Repository.CropRepository;
import com.invernadero.proyecto.mapper.CropMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CropService {

    private final CropRepository cropRepository;

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

    public CropResponse getCropById(Long id) {

        Crop crop = cropRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Crop not found"));

        return CropMapper.toDTO(crop);
    }

    public List<CropResponse> getAllCrops() {

        return cropRepository.findAll()
                .stream()
                .map(CropMapper::toDTO)
                .toList();
    }

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

    public void deleteCrop(Long id) {
        cropRepository.deleteById(id);
    }
}
