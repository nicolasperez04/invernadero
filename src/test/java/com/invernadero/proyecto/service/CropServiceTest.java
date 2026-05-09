package com.invernadero.proyecto.service;

import com.invernadero.proyecto.Dto.Request.CropRequest;
import com.invernadero.proyecto.Dto.response.CropResponse;
import com.invernadero.proyecto.Entity.Crop;
import com.invernadero.proyecto.Repository.CropRepository;
import com.invernadero.proyecto.Service.CropService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
public class CropServiceTest {

    @Mock
    private CropRepository cropRepository;

    @InjectMocks
    private CropService cropService;


    @Test
    void createCrop_success() {

        CropRequest request = new CropRequest();
        request.setName("Tomate");
        request.setDescription("Cultivo rojo");

        Crop savedCrop = Crop.builder()
                .id(1L)
                .name("Tomate")
                .description("Cultivo rojo")
                .estimatedGrowthDays(60)
                .inactivityDaysThreshold(7)
                .build();

        when(cropRepository.save(any(Crop.class))).thenReturn(savedCrop);

        CropResponse response = cropService.createCrop(request);

        assertNotNull(response);
        assertEquals("Tomate", response.getName());

        verify(cropRepository).save(any(Crop.class));
    }

    // =============================
    // ✅ GET BY ID
    // =============================
    @Test
    void getCropById_success() {

        Crop crop = Crop.builder()
                .id(1L)
                .name("Lechuga")
                .description("Verde")
                .estimatedGrowthDays(60)
                .inactivityDaysThreshold(7)
                .build();

        when(cropRepository.findById(1L)).thenReturn(Optional.of(crop));

        CropResponse response = cropService.getCropById(1L);

        assertEquals("Lechuga", response.getName());
    }

    @Test
    void getCropById_notFound() {

        when(cropRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> cropService.getCropById(1L));

        assertEquals("Crop not found", ex.getMessage());
    }

    // =============================
    // ✅ GET ALL
    // =============================
    @Test
    void getAllCrops_success() {

        List<Crop> crops = List.of(
                Crop.builder().id(1L).name("Tomate").estimatedGrowthDays(60).inactivityDaysThreshold(7).build(),
                Crop.builder().id(2L).name("Papa").estimatedGrowthDays(90).inactivityDaysThreshold(10).build()
        );

        when(cropRepository.findAll()).thenReturn(crops);

        List<CropResponse> response = cropService.getAllCrops();

        assertEquals(2, response.size());
    }

    @Test
    void getAllCrops_empty() {

        when(cropRepository.findAll()).thenReturn(List.of());

        List<CropResponse> response = cropService.getAllCrops();

        assertTrue(response.isEmpty());
    }

    // =============================
    // ✅ UPDATE
    // =============================
    @Test
    void updateCrop_success_fullUpdate() {

        Crop crop = Crop.builder()
                .id(1L)
                .name("Old")
                .description("Old desc")
                .estimatedGrowthDays(60)
                .inactivityDaysThreshold(7)
                .build();

        CropRequest request = new CropRequest();
        request.setName("New");
        request.setDescription("New desc");

        when(cropRepository.findById(1L)).thenReturn(Optional.of(crop));
        when(cropRepository.save(any())).thenReturn(crop);

        CropResponse response = cropService.updateCrop(1L, request);

        assertEquals("New", response.getName());
        assertEquals("New desc", response.getDescription());
    }

    @Test
    void updateCrop_partialUpdate() {

        Crop crop = Crop.builder()
                .id(1L)
                .name("Old")
                .description("Old desc")
                .estimatedGrowthDays(60)
                .inactivityDaysThreshold(7)
                .build();

        CropRequest request = new CropRequest();
        request.setName("New");

        when(cropRepository.findById(1L)).thenReturn(Optional.of(crop));
        when(cropRepository.save(any())).thenReturn(crop);

        CropResponse response = cropService.updateCrop(1L, request);

        assertEquals("New", response.getName());
        assertEquals("Old desc", response.getDescription());
    }

    @Test
    void updateCrop_notFound() {

        when(cropRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> cropService.updateCrop(1L, new CropRequest()));
    }

    // =============================
    // ✅ DELETE
    // =============================
    @Test
    void deleteCrop_success() {

        doNothing().when(cropRepository).deleteById(1L);

        cropService.deleteCrop(1L);

        verify(cropRepository).deleteById(1L);
    }

    @Test
    void deleteCrop_exception() {

        doThrow(new RuntimeException("Error"))
                .when(cropRepository).deleteById(1L);

        assertThrows(RuntimeException.class,
                () -> cropService.deleteCrop(1L));
    }

}
