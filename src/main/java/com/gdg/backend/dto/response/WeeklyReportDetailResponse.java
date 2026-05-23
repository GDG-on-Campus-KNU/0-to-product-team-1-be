package com.gdg.backend.dto.response;

import com.gdg.backend.entity.Insight;
import com.gdg.backend.entity.Report;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Getter
@Builder
public class WeeklyReportDetailResponse {

    private Long reportId;
    private Map<String, Object> patternAnalysis;
    private Map<String, Object> emotionDistribution;
    private List<InsightResponse> insights;
    private LocalDateTime createdAt;

    @Getter
    @Builder
    public static class InsightResponse {
        private Long insightId;
        private String text;
        private String source;
        private String weekOf;

        public static InsightResponse from(Insight insight) {
            return InsightResponse.builder()
                    .insightId(insight.getInsightId())
                    .text(insight.getText())
                    .source(insight.getSource())
                    .weekOf(insight.getWeekOf())
                    .build();
        }
    }

    public static WeeklyReportDetailResponse of(Report report, List<Insight> insights) {
        return WeeklyReportDetailResponse.builder()
                .reportId(report.getReportId())
                .patternAnalysis(report.getPatternAnalysis())
                .emotionDistribution(report.getEmotionDistribution())
                .insights(insights.stream().map(InsightResponse::from).toList())
                .createdAt(report.getCreatedAt())
                .build();
    }
}
