package com.invernadero.proyecto.service;

import com.invernadero.proyecto.Entity.Crop;
import com.invernadero.proyecto.Entity.Lot;
import com.invernadero.proyecto.Entity.Notification;
import com.invernadero.proyecto.Entity.NotificationType;
import com.invernadero.proyecto.Repository.EventRepository;
import com.invernadero.proyecto.Repository.LotRepository;
import com.invernadero.proyecto.Repository.NotificationRepository;
import com.invernadero.proyecto.Service.AlertService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlertServiceTest {

    @Mock
    private LotRepository lotRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private AlertService alertService;

    private Lot createLotWithHarvestDate(Long id, String name, int daysFromNow) {
        Crop crop = Crop.builder().id(1L).name("Tomato").estimatedGrowthDays(60).inactivityDaysThreshold(7).build();
        return Lot.builder()
                .id(id)
                .name(name)
                .crop(crop)
                .estimatedHarvestDate(Instant.now().plus(daysFromNow, ChronoUnit.DAYS))
                .build();
    }

    @Test
    void checkUpcomingHarvests_7Days_createsInfoNotification() {
        Lot lot = createLotWithHarvestDate(1L, "Lote Test", 7);

        when(lotRepository.findAll()).thenReturn(List.of(lot));
        when(eventRepository.existsByLotIdAndTypeName(1L, "HARVEST")).thenReturn(false);
        when(eventRepository.existsByLotIdAndTypeName(1L, "SOWING")).thenReturn(true);
        when(notificationRepository.existsByLotIdAndType(1L, NotificationType.HARVEST_7_DAYS)).thenReturn(false);

        alertService.checkUpcomingHarvests();

        verify(notificationRepository).save(argThat(n ->
                n.getType() == NotificationType.HARVEST_7_DAYS &&
                        n.getLevel().name().equals("INFO") &&
                        n.getMessage().contains("7 días")
        ));
    }

    @Test
    void checkUpcomingHarvests_3Days_createsWarningNotification() {
        Lot lot = createLotWithHarvestDate(1L, "Lote Test", 3);

        when(lotRepository.findAll()).thenReturn(List.of(lot));
        when(eventRepository.existsByLotIdAndTypeName(1L, "HARVEST")).thenReturn(false);
        when(eventRepository.existsByLotIdAndTypeName(1L, "SOWING")).thenReturn(true);
        when(notificationRepository.existsByLotIdAndType(1L, NotificationType.HARVEST_3_DAYS)).thenReturn(false);

        alertService.checkUpcomingHarvests();

        verify(notificationRepository).save(argThat(n ->
                n.getType() == NotificationType.HARVEST_3_DAYS &&
                        n.getLevel().name().equals("WARNING") &&
                        n.getMessage().contains("3 días")
        ));
    }

    @Test
    void checkUpcomingHarvests_1Day_createsCriticalNotification() {
        Lot lot = createLotWithHarvestDate(1L, "Lote Test", 1);

        when(lotRepository.findAll()).thenReturn(List.of(lot));
        when(eventRepository.existsByLotIdAndTypeName(1L, "HARVEST")).thenReturn(false);
        when(eventRepository.existsByLotIdAndTypeName(1L, "SOWING")).thenReturn(true);
        when(notificationRepository.existsByLotIdAndType(1L, NotificationType.HARVEST_1_DAY)).thenReturn(false);

        alertService.checkUpcomingHarvests();

        verify(notificationRepository).save(argThat(n ->
                n.getType() == NotificationType.HARVEST_1_DAY &&
                        n.getLevel().name().equals("CRITICAL") &&
                        n.getMessage().contains("mañana")
        ));
    }

    @Test
    void checkUpcomingHarvests_overdue_createsCriticalNotification() {
        Lot lot = createLotWithHarvestDate(1L, "Lote Test", -5);

        when(lotRepository.findAll()).thenReturn(List.of(lot));
        when(eventRepository.existsByLotIdAndTypeName(1L, "HARVEST")).thenReturn(false);
        when(eventRepository.existsByLotIdAndTypeName(1L, "SOWING")).thenReturn(true);
        when(notificationRepository.existsByLotIdAndType(1L, NotificationType.HARVEST_OVERDUE)).thenReturn(false);

        alertService.checkUpcomingHarvests();

        verify(notificationRepository).save(argThat(n ->
                n.getType() == NotificationType.HARVEST_OVERDUE &&
                        n.getLevel().name().equals("CRITICAL") &&
                        n.getMessage().contains("vencida")
        ));
    }

    @Test
    void checkUpcomingHarvests_duplicate_skipsNotification() {
        Lot lot = createLotWithHarvestDate(1L, "Lote Test", 3);

        when(lotRepository.findAll()).thenReturn(List.of(lot));
        when(eventRepository.existsByLotIdAndTypeName(1L, "HARVEST")).thenReturn(false);
        when(eventRepository.existsByLotIdAndTypeName(1L, "SOWING")).thenReturn(true);
        when(notificationRepository.existsByLotIdAndType(1L, NotificationType.HARVEST_3_DAYS)).thenReturn(true);

        alertService.checkUpcomingHarvests();

        verify(notificationRepository, never()).save(any());
    }

    @Test
    void checkUpcomingHarvests_finishedLot_cleansUpNotifications() {
        Lot lot = createLotWithHarvestDate(1L, "Lote Test", 3);
        Notification existing = Notification.builder()
                .id(1L).lotId(1L).lotName("Lote Test")
                .type(NotificationType.HARVEST_7_DAYS)
                .read(false)
                .build();

        when(lotRepository.findAll()).thenReturn(List.of(lot));
        when(eventRepository.existsByLotIdAndTypeName(1L, "HARVEST")).thenReturn(true);
        when(notificationRepository.findByLotIdAndReadFalse(1L)).thenReturn(List.of(existing));

        alertService.checkUpcomingHarvests();

        assertTrue(existing.isRead());
        verify(notificationRepository, atLeastOnce()).save(existing);
    }

    @Test
    void checkUpcomingHarvests_noEstimatedDate_skips() {
        Lot lot = Lot.builder().id(1L).name("Lote Test").build();

        when(lotRepository.findAll()).thenReturn(List.of(lot));

        alertService.checkUpcomingHarvests();

        verify(eventRepository, never()).existsByLotIdAndTypeName(anyLong(), anyString());
    }

    @Test
    void checkUpcomingHarvests_noSowing_skips() {
        Lot lot = createLotWithHarvestDate(1L, "Lote Test", 3);

        when(lotRepository.findAll()).thenReturn(List.of(lot));
        when(eventRepository.existsByLotIdAndTypeName(1L, "HARVEST")).thenReturn(false);
        when(eventRepository.existsByLotIdAndTypeName(1L, "SOWING")).thenReturn(false);

        alertService.checkUpcomingHarvests();

        verify(notificationRepository, never()).save(any());
    }

    @Test
    void getAllNotifications_returnsList() {
        when(notificationRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(
                Notification.builder().id(1L).message("Test").build()
        ));

        List<Notification> result = alertService.getAllNotifications();

        assertEquals(1, result.size());
        assertEquals("Test", result.get(0).getMessage());
    }

    @Test
    void getUnreadCount_returnsCount() {
        when(notificationRepository.countByReadFalse()).thenReturn(5L);

        long count = alertService.getUnreadCount();

        assertEquals(5L, count);
    }

    @Test
    void markAsRead_marksNotificationRead() {
        Notification n = Notification.builder().id(1L).read(false).build();
        when(notificationRepository.findById(1L)).thenReturn(java.util.Optional.of(n));

        alertService.markAsRead(1L);

        assertTrue(n.isRead());
        verify(notificationRepository).save(n);
    }

    @Test
    void markAllAsRead_marksAllUnread() {
        Notification n1 = Notification.builder().id(1L).read(false).build();
        Notification n2 = Notification.builder().id(2L).read(false).build();
        when(notificationRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(n1, n2));

        alertService.markAllAsRead();

        assertTrue(n1.isRead());
        assertTrue(n2.isRead());
        verify(notificationRepository, times(2)).save(any());
    }
}
