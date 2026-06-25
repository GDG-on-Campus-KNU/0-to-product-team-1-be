package com.gdg.backend.dto.response;

import com.gdg.backend.entity.Entry;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Builder
public class DrillTodayResponse {

    private boolean hasDrill;
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

    public static DrillTodayResponse empty() {
        Map<String, Object> emotions = new java.util.LinkedHashMap<>();
        emotions.put("분노", 0.0); emotions.put("불안", 0.0); emotions.put("우울", 0.0);
        emotions.put("죄책", 0.0); emotions.put("중립", 0.0);
        Map<String, Object> patterns = new java.util.LinkedHashMap<>();
        patterns.put("독심술", 0.0); patterns.put("이분법", 0.0); patterns.put("당위진술", 0.0);
        patterns.put("미래예측", 0.0); patterns.put("자기비난", 0.0); patterns.put("과잉일반화", 0.0);
        Map<String, Object> defaultLabel = new java.util.HashMap<>();
        defaultLabel.put("emotions", emotions);
        defaultLabel.put("patterns", patterns);
        return DrillTodayResponse.builder()
                .hasDrill(false)
                .labelResultJson(defaultLabel)
                .drillCompleted(false)
                .build();
    }

    public static DrillTodayResponse from(Entry entry) {
        return DrillTodayResponse.builder()
                .hasDrill(true)
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
