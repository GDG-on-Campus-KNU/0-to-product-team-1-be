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
    private Map<String, Object> context;
    private Integer drillId;
    private Boolean drillComplete;
    private Map<String, Object> llmResult;
    private LocalDateTime createdAt;

    // ML 추천 결과
    private MlRecommendResponse recommendation;

    // v9.5 새 필드
    private String drillCategory;
    private String drillCalendarColor;
    private Boolean awaitingAnswer;
    private Boolean crisisFlag;
    private Map<String, Object> recommendationJson;

    public static EntryCreateResponse from(Entry entry) {
        return EntryCreateResponse.builder()
                .entryId(entry.getEntryId())
                .text(entry.getText())
                .context(entry.getContext())
                .drillId(entry.getDrillId())
                .drillComplete(entry.getDrillComplete())
                .llmResult(entry.getLlmResult())
                .createdAt(entry.getCreatedAt())
                .drillCategory(entry.getDrillCategory())
                .drillCalendarColor(entry.getDrillCalendarColor())
                .awaitingAnswer(entry.getAwaitingAnswer())
                .crisisFlag(entry.getCrisisFlag())
                .recommendationJson(entry.getRecommendationJson())
                .build();
    }

    public static EntryCreateResponse from(Entry entry, MlRecommendResponse recommendation) {
        return EntryCreateResponse.builder()
                .entryId(entry.getEntryId())
                .text(entry.getText())
                .context(entry.getContext())
                .drillId(entry.getDrillId())
                .drillComplete(entry.getDrillComplete())
                .llmResult(entry.getLlmResult())
                .createdAt(entry.getCreatedAt())
                .recommendation(recommendation)
                .drillCategory(entry.getDrillCategory())
                .drillCalendarColor(entry.getDrillCalendarColor())
                .awaitingAnswer(entry.getAwaitingAnswer())
                .crisisFlag(entry.getCrisisFlag())
                .recommendationJson(entry.getRecommendationJson())
                .build();
    }
}
