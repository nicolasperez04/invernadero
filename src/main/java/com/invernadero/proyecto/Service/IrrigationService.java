package com.invernadero.proyecto.Service;

import com.invernadero.proyecto.Entity.*;
import com.invernadero.proyecto.Repository.EventRepository;
import com.invernadero.proyecto.Repository.EventTypeRepository;
import com.invernadero.proyecto.Repository.LotRepository;
import com.invernadero.proyecto.Repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class IrrigationService {

    private final LotRepository lotRepository;
    private final EventRepository eventRepository;
    private final EventTypeRepository eventTypeRepository;
    private final UserRepository userRepository;
    private final SseService sseService;

    @Value("${app.irrigation.system-user-email}")
    private String systemUserEmail;

    private User systemUser;

    @PostConstruct
    public void init() {
        systemUser = userRepository.findByEmail(systemUserEmail)
                .orElseGet(() -> userRepository.findAll().stream().findFirst().orElse(null));
        if (systemUser == null) {
            log.warn("No se encontró ningún usuario en la BD — el scheduler de riego no podrá crear eventos");
        } else {
            log.info("Usuario del sistema para riego automático: {} (id={})", systemUser.getEmail(), systemUser.getId());
        }
    }

    @Scheduled(cron = "0 0 */1 * * *")
    @Transactional
    public void autoIrrigate() {
        if (systemUser == null) {
            log.warn("No hay usuario del sistema configurado — abortando riego automático");
            return;
        }

        log.info("Ejecutando scheduler: riego automático...");
        List<Lot> lots = lotRepository.findByStatus(LotStatus.IN_PRODUCTION);
        EventType riegoType = eventTypeRepository.findByName("IRRIGATION").orElse(null);
        if (riegoType == null) {
            log.warn("No se encontró tipo de evento RIEGO — abortando riego automático");
            return;
        }

        for (Lot lot : lots) {
            Integer freq = lot.getCrop().getIrrigationFrequencyHours();
            if (freq == null || freq <= 0) continue;

            Optional<Event> lastIrrigation =
                    eventRepository.findTopByLotIdAndTypeIdOrderByTimestampDesc(lot.getId(), riegoType.getId());

            long hoursSince = lastIrrigation
                    .map(e -> Duration.between(e.getTimestamp(), Instant.now()).toHours())
                    .orElse(Long.MAX_VALUE);

            if (hoursSince >= freq) {
                Event event = Event.builder()
                        .lot(lot)
                        .type(riegoType)
                        .user(systemUser)
                        .timestamp(Instant.now())
                        .description("Riego automático programado")
                        .createdAt(Instant.now())
                        .build();
                eventRepository.save(event);
                log.info("Riego automático creado para lote '{}' (último riego hace {}, frecuencia {}h)",
                        lot.getName(),
                        hoursSince == Long.MAX_VALUE ? "nunca" : hoursSince + "h",
                        freq);
                sseService.sendEvent("dashboard", "{\"type\":\"EVENT_CREATED\"}");
            }
        }
    }
}
