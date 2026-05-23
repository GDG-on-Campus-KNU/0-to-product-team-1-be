package com.gdg.backend.dto.response;

import com.gdg.backend.entity.Report;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class WeeklyReportListResponse {

    private Long reportId;
    private LocalDateTime createdAt;

    public static WeeklyReportListResponse from(Report report) {
        return WeeklyReportListResponse.builder()
                .reportId(report.getReportId())
                .createdAt(report.getCreatedAt())
                .build();
    }
}
