package com.gdg.backend.dto.response;

import com.gdg.backend.entity.ReportWeekly;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.ALWAYS)
public class WeeklyReportResponse {

    private String weekId;
    private Long userId;
    private Map<String, Object> blocksJson;
    private Map<String, Object> visualizationsJson;
    private List<DailyDrillRecord> dailyDrills;
    private LifestyleSummary lifestyleSummary;
    private LocalDateTime generatedAt;
    private Boolean isChecked;
    private String userMemo;

    public static WeeklyReportResponse from(ReportWeekly report) {
        return WeeklyReportResponse.builder()
                .weekId(report.getWeekId())
                .userId(report.getUserId())
                .blocksJson(report.getBlocksJson())
                .visualizationsJson(report.getVisualizationsJson())
                .generatedAt(report.getGeneratedAt())
                .build();
    }

    public static WeeklyReportResponse from(ReportWeekly report, List<DailyDrillRecord> dailyDrills, LifestyleSummary lifestyleSummary) {
        return WeeklyReportResponse.builder()
                .weekId(report.getWeekId())
                .userId(report.getUserId())
                .blocksJson(report.getBlocksJson())
                .visualizationsJson(report.getVisualizationsJson())
                .dailyDrills(dailyDrills)
                .lifestyleSummary(lifestyleSummary)
                .generatedAt(report.getGeneratedAt())
                .build();
    }

    public static WeeklyReportResponse from(ReportWeekly report, Map<String, Object> blocksJson,
                                            Map<String, Object> visualizationsJson,
                                            List<DailyDrillRecord> dailyDrills, LifestyleSummary lifestyleSummary) {
        return WeeklyReportResponse.builder()
                .weekId(report.getWeekId())
                .userId(report.getUserId())
                .blocksJson(blocksJson)
                .visualizationsJson(visualizationsJson)
                .dailyDrills(dailyDrills)
                .lifestyleSummary(lifestyleSummary)
                .generatedAt(report.getGeneratedAt())
                .isChecked(report.isChecked())
                .userMemo(report.getUserMemo())
                .build();
    }

    public static WeeklyReportResponse from(ReportWeekly report, Map<String, Object> blocksJson,
                                            Map<String, Object> visualizationsJson,
                                            List<DailyDrillRecord> dailyDrills, LifestyleSummary lifestyleSummary,
                                            boolean wasChecked) {
        return WeeklyReportResponse.builder()
                .weekId(report.getWeekId())
                .userId(report.getUserId())
                .blocksJson(blocksJson)
                .visualizationsJson(visualizationsJson)
                .dailyDrills(dailyDrills)
                .lifestyleSummary(lifestyleSummary)
                .generatedAt(report.getGeneratedAt())
                .isChecked(wasChecked)
                .userMemo(report.getUserMemo())
                .build();
    }
}
