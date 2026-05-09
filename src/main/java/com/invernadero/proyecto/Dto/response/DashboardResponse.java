package com.invernadero.proyecto.Dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DashboardResponse {

    private EventChartDTO eventChart;
    private List<LotStatusDTO> lotStatuses;
    private List<LotProgressDTO> lotProgress;


}
