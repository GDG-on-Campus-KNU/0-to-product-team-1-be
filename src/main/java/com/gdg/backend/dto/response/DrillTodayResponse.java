package com.gdg.backend.dto.response;

import com.gdg.backend.entity.Entry;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Builder
public class DrillTodayResponse {

    private boolean hasDrill;
    private Long entryId;
    private String text;
    private Map<String, Object> context;
    private Integer drillId;
    private Boolean drillComplete;
    private Map<String, Object> llmResult;
    private Boolean helpful;
    private LocalDateTime createdAt;

    public static DrillTodayResponse empty() {
        return DrillTodayResponse.builder()
                .hasDrill(false)
                .build();
    }

    public static DrillTodayResponse from(Entry entry) {
        return DrillTodayResponse.builder()
                .hasDrill(true)
                .entryId(entry.getEntryId())
                .text(entry.getText())
                .context(entry.getContext())
                .drillId(entry.getDrillId())
                .drillComplete(entry.getDrillComplete())
                .llmResult(entry.getLlmResult())
                .helpful(entry.getHelpful())
                .createdAt(entry.getCreatedAt())
                .build();
    }
}
