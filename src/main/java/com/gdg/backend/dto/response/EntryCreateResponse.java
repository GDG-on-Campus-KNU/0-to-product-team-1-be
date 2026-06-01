package com.gdg.backend.dto.response;

import com.gdg.backend.dto.ml.MlRecommendResponse;
import com.gdg.backend.entity.Entry;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Builder
public class EntryCreateResponse {

    private Long entryId;
    private String text;
    private Integer drillId;
    private Map<String, Object> labelResultJson;
    private LocalDateTime createdAt;

    // ML 추천 결과
    private MlRecommendResponse recommendation;

    // v9.5 필드
    private String drillCategory;
    private String drillCalendarColor;
    private Boolean crisisFlag;
    private Map<String, Object> recommendationJson;
    private Map<String, Object> contextJson;

    public static EntryCreateResponse from(Entry entry) {
        return EntryCreateResponse.builder()
                .entryId(entry.getEntryId())
                .text(entry.getText())
                .drillId(entry.getDrillId())
                .labelResultJson(entry.getLabelResultJson())
                .createdAt(entry.getCreatedAt())
                .drillCategory(entry.getDrillCategory())
                .drillCalendarColor(entry.getDrillCalendarColor())
                .crisisFlag(entry.getCrisisFlag())
                .recommendationJson(entry.getRecommendationJson())
                .contextJson(entry.getContextJson())
                .build();
    }

    public static EntryCreateResponse from(Entry entry, MlRecommendResponse recommendation) {
        return EntryCreateResponse.builder()
                .entryId(entry.getEntryId())
                .text(entry.getText())
                .drillId(entry.getDrillId())
                .labelResultJson(entry.getLabelResultJson())
                .createdAt(entry.getCreatedAt())
                .recommendation(recommendation)
                .drillCategory(entry.getDrillCategory())
                .drillCalendarColor(entry.getDrillCalendarColor())
                .crisisFlag(entry.getCrisisFlag())
                .recommendationJson(entry.getRecommendationJson())
                .contextJson(entry.getContextJson())
                .build();
    }
}
