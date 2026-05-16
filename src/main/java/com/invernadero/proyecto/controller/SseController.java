package com.invernadero.proyecto.controller;

import com.invernadero.proyecto.Security.JwtService;
import com.invernadero.proyecto.Service.SseService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/sse")
@RequiredArgsConstructor
@Tag(name = "SSE", description = "Server-Sent Events para actualizaciones en tiempo real")
public class SseController {

    private final SseService sseService;
    private final JwtService jwtService;

    @GetMapping("/subscribe")
    public ResponseEntity<SseEmitter> subscribe(@RequestParam(value = "token", required = false) String token) {
        if (token == null || !jwtService.validateToken(token)) {
            return ResponseEntity.status(401).build();
        }

        SseEmitter emitter = sseService.createEmitter();
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_EVENT_STREAM)
                .body(emitter);
    }
}
