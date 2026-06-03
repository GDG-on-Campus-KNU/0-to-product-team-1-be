package com.gdg.backend.dto.response;

import com.gdg.backend.entity.ReportMonthly;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Builder
public class MonthlyReportResponse {

    private String monthId;
    private Long userId;
    private Map<String, Object> blocksJson;
    private LocalDateTime generatedAt;

    public static MonthlyReportResponse from(ReportMonthly report) {
        return MonthlyReportResponse.builder()
                .monthId(report.getMonthId())
                .userId(report.getUserId())
                .blocksJson(report.getBlocksJson())
                .generatedAt(report.getGeneratedAt())
                .build();
    }
}
