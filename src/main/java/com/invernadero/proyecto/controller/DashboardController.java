package com.invernadero.proyecto.controller;

import com.invernadero.proyecto.Dto.response.DashboardResponse;
import com.invernadero.proyecto.Dto.response.EventChartDTO;
import com.invernadero.proyecto.Dto.response.LotProgressDTO;
import com.invernadero.proyecto.Dto.response.LotStatusDTO;
import com.invernadero.proyecto.Service.DashboardService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Visualización y métricas del sistema")
@SecurityRequirement(name = "bearer-jwt")
public class DashboardController {

    private final DashboardService dashboardService;


    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR','VIEWER')")
    public DashboardResponse getDashboard(
            @RequestParam(required = false) Long cropId) {
        return dashboardService.getDashboard(cropId);
    }

    @GetMapping("/events")
    public EventChartDTO getEvents(@RequestParam(required = false) Long cropId) {
        return dashboardService.getDashboard(cropId).getEventChart();
    }

    @GetMapping("/status")
    public List<LotStatusDTO> getStatus(@RequestParam(required = false) Long cropId) {
        return dashboardService.getDashboard(cropId).getLotStatuses();
    }

    @GetMapping("/progress")
    public List<LotProgressDTO> getProgress(@RequestParam(required = false) Long cropId) {
        return dashboardService.getDashboard(cropId).getLotProgress();
    }


}
