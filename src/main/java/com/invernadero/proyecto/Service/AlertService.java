package com.invernadero.proyecto.Service;

import com.invernadero.proyecto.Entity.*;
import com.invernadero.proyecto.Repository.EventRepository;
import com.invernadero.proyecto.Repository.LotRepository;
import com.invernadero.proyecto.Repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlertService {

    private final LotRepository lotRepository;
    private final EventRepository eventRepository;
    private final NotificationRepository notificationRepository;
    private final LotService lotService;

    @Scheduled(cron = "0 0 */6 * * *")
    @Transactional
    public void checkInactivityAlerts() {
        log.info("Ejecutando scheduler: revisando inactividad de lotes...");
        List<Lot> lots = lotRepository.findAll();
        for (Lot lot : lots) {
            String status = lotService.getInactivityStatus(lot.getId());
            String lotName = lot.getName();
            if ("RED".equals(status)) {
                createIfNotExists(lot.getId(), lotName, NotificationType.INACTIVITY_CRITICAL,
                        NotificationLevel.CRITICAL,
                        "El lote " + lotName + " lleva varios días sin actividad — ¡revisar urgente!");
            } else if ("YELLOW".equals(status)) {
                createIfNotExists(lot.getId(), lotName, NotificationType.INACTIVITY_WARNING,
                        NotificationLevel.WARNING,
                        "El lote " + lotName + " está próximo al límite de inactividad");
            }
        }
    }

    @Scheduled(cron = "0 0 6 * * *")
    @Transactional
    public void checkUpcomingHarvests() {
        log.info("Ejecutando scheduler: revisando cosechas próximas...");
        List<Lot> lots = lotRepository.findAll();
        LocalDate today = LocalDate.now();

        for (Lot lot : lots) {
            if (lot.getEstimatedHarvestDate() == null) continue;

            boolean hasHarvest = eventRepository.existsByLotIdAndTypeName(lot.getId(), "HARVEST");
            if (hasHarvest) {
                cleanupNotifications(lot.getId());
                continue;
            }

            boolean hasSowing = eventRepository.existsByLotIdAndTypeName(lot.getId(), "SOWING");
            if (!hasSowing) continue;

            LocalDate harvestDate = lot.getEstimatedHarvestDate()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();

            long daysUntil = ChronoUnit.DAYS.between(today, harvestDate);
            String lotName = lot.getName();

            if (daysUntil == 7) {
                createIfNotExists(lot.getId(), lotName, NotificationType.HARVEST_7_DAYS,
                        NotificationLevel.INFO,
                        "La cosecha del lote " + lotName + " está a 7 días");
            } else if (daysUntil == 3) {
                createIfNotExists(lot.getId(), lotName, NotificationType.HARVEST_3_DAYS,
                        NotificationLevel.WARNING,
                        "La cosecha del lote " + lotName + " está a 3 días — preparar recursos");
            } else if (daysUntil == 1) {
                createIfNotExists(lot.getId(), lotName, NotificationType.HARVEST_1_DAY,
                        NotificationLevel.CRITICAL,
                        "¡El lote " + lotName + " debe cosecharse mañana!");
            } else if (daysUntil < 0) {
                createIfNotExists(lot.getId(), lotName, NotificationType.HARVEST_OVERDUE,
                        NotificationLevel.CRITICAL,
                        "¡La cosecha del lote " + lotName + " está vencida!");
            }
        }
    }

    private void createIfNotExists(Long lotId, String lotName, NotificationType type,
                                   NotificationLevel level, String message) {
        if (notificationRepository.existsByLotIdAndType(lotId, type)) return;
        Notification notification = Notification.builder()
                .lotId(lotId)
                .lotName(lotName)
                .type(type)
                .level(level)
                .message(message)
                .createdAt(LocalDate.now())
                .read(false)
                .build();
        notificationRepository.save(notification);
        log.info("Notificación creada: [{}] {} - {}", type, lotName, message);
    }

    private void cleanupNotifications(Long lotId) {
        List<Notification> unread = notificationRepository.findByLotIdAndReadFalse(lotId);
        for (Notification n : unread) {
            n.setRead(true);
            notificationRepository.save(n);
        }
        if (!unread.isEmpty()) {
            log.info("Notificaciones del lote {} limpiadas (marcadas como leídas)", lotId);
        }
    }

    public List<Notification> getAllNotifications() {
        return notificationRepository.findAllByOrderByCreatedAtDesc();
    }

    public long getUnreadCount() {
        return notificationRepository.countByReadFalse();
    }

    @Transactional
    public void markAsRead(Long id) {
        notificationRepository.findById(id).ifPresent(n -> {
            n.setRead(true);
            notificationRepository.save(n);
        });
    }

    @Transactional
    public void markAllAsRead() {
        List<Notification> unread = notificationRepository.findAllByOrderByCreatedAtDesc()
                .stream().filter(n -> !n.isRead()).toList();
        for (Notification n : unread) {
            n.setRead(true);
            notificationRepository.save(n);
        }
    }
}
