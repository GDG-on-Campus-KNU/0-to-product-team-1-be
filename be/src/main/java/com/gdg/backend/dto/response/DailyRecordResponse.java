package com.gdg.backend.dto.response;

import com.gdg.backend.entity.Entry;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Builder
public class DailyRecordResponse {

    private boolean hasEntry;
    private Long entryId;
    private String text;
    private Integer drillId;
    private String drillCategory;
    private String drillCalendarColor;
    private Map<String, Object> labelResultJson;
    private Map<String, Object> recommendationJson;
    private Boolean drillCompleted;
    private Boolean helpful;
    private Map<String, Object> contextJson;
    private LocalDate recordedDate;
    private LocalDateTime createdAt;

    public static DailyRecordResponse empty() {
        return DailyRecordResponse.builder()
                .hasEntry(false)
                .build();
    }

    public static DailyRecordResponse from(Entry entry) {
        return DailyRecordResponse.builder()
                .hasEntry(true)
                .entryId(entry.getEntryId())
                .text(entry.getText())
                .drillId(entry.getDrillId())
                .drillCategory(entry.getDrillCategory())
                .drillCalendarColor(entry.getDrillCalendarColor())
                .labelResultJson(entry.getLabelResultJson())
                .recommendationJson(entry.getRecommendationJson())
                .drillCompleted(entry.getDrillCompleted())
                .helpful(entry.getHelpful())
                .contextJson(entry.getContextJson())
                .recordedDate(entry.getRecordedDate())
                .createdAt(entry.getCreatedAt())
                .build();
    }
}
