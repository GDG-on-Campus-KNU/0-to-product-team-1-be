package com.gdg.backend.dto.response;

import com.gdg.backend.entity.ReportWeekly;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Builder
public class WeeklyReportResponse {

    private String weekId;
    private Long userId;
    private Map<String, Object> blocksJson;
    private Map<String, Object> visualizationsJson;
    private LocalDateTime generatedAt;

    public static WeeklyReportResponse from(ReportWeekly report) {
        return WeeklyReportResponse.builder()
                .weekId(report.getWeekId())
                .userId(report.getUserId())
                .blocksJson(report.getBlocksJson())
                .visualizationsJson(report.getVisualizationsJson())
                .generatedAt(report.getGeneratedAt())
                .build();
    }
}
