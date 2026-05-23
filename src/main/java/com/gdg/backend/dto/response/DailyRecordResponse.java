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
    private Map<String, Object> context;
    private Integer drillId;
    private Boolean drillComplete;
    private Map<String, Object> llmResult;
    private Boolean helpful;
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
                .context(entry.getContext())
                .drillId(entry.getDrillId())
                .drillComplete(entry.getDrillComplete())
                .llmResult(entry.getLlmResult())
                .helpful(entry.getHelpful())
                .createdAt(entry.getCreatedAt())
                .build();
    }
}
