package com.invernadero.proyecto.controller;

import com.invernadero.proyecto.Entity.Notification;
import com.invernadero.proyecto.Service.AlertService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Gestión de notificaciones del sistema")
@SecurityRequirement(name = "bearer-jwt")
public class NotificationController {

    private final AlertService alertService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR','VIEWER')")
    public List<Notification> getAll() {
        return alertService.getAllNotifications();
    }

    @GetMapping("/unread-count")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR','VIEWER')")
    public Map<String, Long> getUnreadCount() {
        return Map.of("count", alertService.getUnreadCount());
    }

    @PutMapping("/{id}/read")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR','VIEWER')")
    public void markAsRead(@PathVariable Long id) {
        alertService.markAsRead(id);
    }

    @PutMapping("/read-all")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR','VIEWER')")
    public void markAllAsRead() {
        alertService.markAllAsRead();
    }
}
