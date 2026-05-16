package com.invernadero.proyecto.service;

import com.invernadero.proyecto.Service.SseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class SseServiceTest {

    private SseService sseService;

    @BeforeEach
    void setUp() {
        sseService = new SseService();
    }

    @Test
    void createEmitter_returnsValidEmitter() {
        SseEmitter emitter = sseService.createEmitter();
        assertNotNull(emitter);
    }

    @Test
    void createEmitter_multipleEmitters_allDistinct() {
        SseEmitter e1 = sseService.createEmitter();
        SseEmitter e2 = sseService.createEmitter();
        assertNotNull(e1);
        assertNotNull(e2);
        assertNotSame(e1, e2);
    }

    @Test
    void sendEvent_noEmitters_doesNotThrow() {
        assertDoesNotThrow(() -> sseService.sendEvent("test", "data"));
    }

    @Test
    void sendEvent_withDeadEmitter_doesNotThrow() {
        sseService.createEmitter();

        assertDoesNotThrow(() -> sseService.sendEvent("dashboard", "{\"type\":\"TEST\"}"));
    }

    @Test
    void sendEvent_cleansUpDeadEmitters() {
        sseService.createEmitter();

        sseService.sendEvent("dashboard", "{\"type\":\"TEST\"}");

        assertDoesNotThrow(() -> sseService.sendEvent("dashboard", "{\"type\":\"AFTER_CLEANUP\"}"));
    }
}
