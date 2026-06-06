package com.gdg.backend.dto.response;

import com.gdg.backend.entity.ReportWeekly;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class WeeklyReportSummaryResponse {

    private String weekId;
    private int completedDrills;
    private int totalDrills;
    private LocalDateTime generatedAt;

    public static WeeklyReportSummaryResponse from(ReportWeekly report, int completedDrills, int totalDrills) {
        return WeeklyReportSummaryResponse.builder()
                .weekId(report.getWeekId())
                .completedDrills(completedDrills)
                .totalDrills(totalDrills)
                .generatedAt(report.getGeneratedAt())
                .build();
    }
}
