package com.invernadero.proyecto.service;

import com.invernadero.proyecto.Dto.Request.CropRequest;
import com.invernadero.proyecto.Dto.response.CropResponse;
import com.invernadero.proyecto.Entity.Crop;
import com.invernadero.proyecto.Entity.CropEventType;
import com.invernadero.proyecto.Entity.EventType;
import com.invernadero.proyecto.Repository.CropEventTypeRepository;
import com.invernadero.proyecto.Repository.CropRepository;
import com.invernadero.proyecto.Repository.EventTypeRepository;
import com.invernadero.proyecto.Service.CropService;
import com.invernadero.proyecto.Service.SseService;
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

    @Mock
    private EventTypeRepository eventTypeRepository;

    @Mock
    private CropEventTypeRepository cropEventTypeRepository;

    @Mock
    private SseService sseService;

    @InjectMocks
    private CropService cropService;


    @Test
    void createCrop_success() {

        CropRequest request = new CropRequest();
        request.setName("Tomate");
        request.setDescription("Cultivo rojo");
        request.setInactivityDaysThreshold(7);
        request.setEstimatedGrowthDays(60);

        EventType sowing = EventType.builder().id(1L).name("SOWING").build();
        EventType irrigation = EventType.builder().id(2L).name("IRRIGATION").build();

        Crop savedCrop = Crop.builder()
                .id(1L)
                .name("Tomate")
                .description("Cultivo rojo")
                .estimatedGrowthDays(60)
                .inactivityDaysThreshold(7)
                .build();

        when(cropRepository.findByName("Tomate")).thenReturn(Optional.empty());
        when(cropRepository.save(any(Crop.class))).thenReturn(savedCrop);
        when(eventTypeRepository.findAll()).thenReturn(List.of(sowing, irrigation));

        CropResponse response = cropService.createCrop(request);

        assertNotNull(response);
        assertEquals("Tomate", response.getName());

        verify(cropRepository).save(any(Crop.class));
        verify(eventTypeRepository).findAll();
        verify(cropEventTypeRepository).saveAll(anyList());
        verify(sseService).sendEvent(eq("dashboard"), anyString());
    }

    @Test
    void createCrop_duplicateName() {

        CropRequest request = new CropRequest();
        request.setName("Tomate");

        when(cropRepository.findByName("Tomate")).thenReturn(Optional.of(Crop.builder().id(1L).name("Tomate").build()));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> cropService.createCrop(request));

        assertEquals("Ya existe un cultivo con ese nombre", ex.getMessage());
        verify(sseService, never()).sendEvent(anyString(), anyString());
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
        verify(sseService).sendEvent(eq("dashboard"), anyString());
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
        verify(sseService).sendEvent(eq("dashboard"), anyString());
    }

    @Test
    void updateCrop_notFound() {

        when(cropRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> cropService.updateCrop(1L, new CropRequest()));

        verifyNoInteractions(sseService);
    }

    // =============================
    // ✅ DELETE
    // =============================
    @Test
    void deleteCrop_success() {

        doNothing().when(cropRepository).deleteById(1L);

        cropService.deleteCrop(1L);

        verify(cropRepository).deleteById(1L);
        verify(sseService).sendEvent(eq("dashboard"), anyString());
    }

    @Test
    void deleteCrop_exception() {

        doThrow(new RuntimeException("Error"))
                .when(cropRepository).deleteById(1L);

        assertThrows(RuntimeException.class,
                () -> cropService.deleteCrop(1L));

        verify(sseService, never()).sendEvent(anyString(), anyString());
    }

    // =============================
    // ✅ CROP EVENT TYPES
    // =============================
    @Test
    void addEventTypeToCrop_alreadyAssociated() {

        when(cropEventTypeRepository.existsByCropIdAndEventTypeId(1L, 2L)).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> cropService.addEventTypeToCrop(1L, 2L));

        assertEquals("El tipo de evento ya está asociado a este cultivo", ex.getMessage());
    }

    @Test
    void removeEventTypeFromCrop_notAssociated() {

        when(cropEventTypeRepository.existsByCropIdAndEventTypeId(1L, 2L)).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> cropService.removeEventTypeFromCrop(1L, 2L));

        assertEquals("El tipo de evento no está asociado a este cultivo", ex.getMessage());
    }

    @Test
    void addEventTypeToCrop_success() {

        Crop crop = Crop.builder().id(1L).name("Tomate").estimatedGrowthDays(60).inactivityDaysThreshold(7).build();
        EventType pruning = EventType.builder().id(3L).name("PRUNING").build();

        when(cropEventTypeRepository.existsByCropIdAndEventTypeId(1L, 3L)).thenReturn(false);
        when(cropRepository.findById(1L)).thenReturn(Optional.of(crop));
        when(eventTypeRepository.findById(3L)).thenReturn(Optional.of(pruning));

        cropService.addEventTypeToCrop(1L, 3L);

        verify(cropEventTypeRepository).save(any(CropEventType.class));
    }

    @Test
    void removeEventTypeFromCrop_success() {

        when(cropEventTypeRepository.existsByCropIdAndEventTypeId(1L, 2L)).thenReturn(true);
        doNothing().when(cropEventTypeRepository).deleteByCropIdAndEventTypeId(1L, 2L);

        cropService.removeEventTypeFromCrop(1L, 2L);

        verify(cropEventTypeRepository).deleteByCropIdAndEventTypeId(1L, 2L);
    }

    @Test
    void getEventTypesByCrop_success() {

        EventType sowing = EventType.builder().id(1L).name("SOWING").build();
        EventType irrigation = EventType.builder().id(2L).name("IRRIGATION").build();
        Crop crop = Crop.builder().id(1L).name("Tomate").estimatedGrowthDays(60).inactivityDaysThreshold(7).build();

        List<CropEventType> associations = List.of(
                CropEventType.builder().id(1L).crop(crop).eventType(sowing).build(),
                CropEventType.builder().id(2L).crop(crop).eventType(irrigation).build()
        );

        when(cropEventTypeRepository.findByCropId(1L)).thenReturn(associations);

        List<EventType> result = cropService.getEventTypesByCrop(1L);

        assertEquals(2, result.size());
        assertEquals("SOWING", result.get(0).getName());
    }

    @Test
    void addEventTypeToCrop_cropNotFound() {
        when(cropEventTypeRepository.existsByCropIdAndEventTypeId(1L, 3L)).thenReturn(false);
        when(cropRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> cropService.addEventTypeToCrop(1L, 3L));

        assertEquals("Crop not found", ex.getMessage());
    }

    @Test
    void addEventTypeToCrop_eventTypeNotFound() {
        Crop crop = Crop.builder().id(1L).name("Tomate").estimatedGrowthDays(60).inactivityDaysThreshold(7).build();
        when(cropEventTypeRepository.existsByCropIdAndEventTypeId(1L, 3L)).thenReturn(false);
        when(cropRepository.findById(1L)).thenReturn(Optional.of(crop));
        when(eventTypeRepository.findById(3L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> cropService.addEventTypeToCrop(1L, 3L));

        assertEquals("Event type not found", ex.getMessage());
    }

    @Test
    void updateCrop_nullName_keepsOld() {
        Crop crop = Crop.builder()
                .id(1L)
                .name("Old")
                .description("Old desc")
                .estimatedGrowthDays(60)
                .inactivityDaysThreshold(7)
                .build();

        CropRequest request = new CropRequest();
        request.setName(null);

        when(cropRepository.findById(1L)).thenReturn(Optional.of(crop));
        when(cropRepository.save(any())).thenReturn(crop);

        CropResponse response = cropService.updateCrop(1L, request);

        assertEquals("Old", response.getName());
    }

    @Test
    void updateCrop_blankName_keepsOld() {
        Crop crop = Crop.builder()
                .id(1L)
                .name("Old")
                .description("Old desc")
                .estimatedGrowthDays(60)
                .inactivityDaysThreshold(7)
                .build();

        CropRequest request = new CropRequest();
        request.setName("  ");

        when(cropRepository.findById(1L)).thenReturn(Optional.of(crop));
        when(cropRepository.save(any())).thenReturn(crop);

        CropResponse response = cropService.updateCrop(1L, request);

        assertEquals("Old", response.getName());
    }
}
