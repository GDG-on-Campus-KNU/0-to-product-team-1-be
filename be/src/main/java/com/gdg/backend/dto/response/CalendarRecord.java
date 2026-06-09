package com.gdg.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gdg.backend.entity.Entry;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.Map;

@Getter
@Builder
public class CalendarRecord {

    private LocalDate date;

    @JsonProperty("drillId")
    private Integer drillId;

    @JsonProperty("isCompleted")
    private Boolean isCompleted;

    public static CalendarRecord from(Entry entry) {
        Integer drillId = entry.getDrillId();
        if (drillId == null && entry.getRecommendationJson() != null) {
            Object drill = entry.getRecommendationJson().get("drill");
            if (drill instanceof Map<?, ?> drillMap) {
                Object id = drillMap.get("id");
                if (id instanceof Number n) drillId = n.intValue();
            }
        }
        return CalendarRecord.builder()
                .date(entry.getRecordedDate())
                .drillId(drillId)
                .isCompleted(entry.getDrillCompleted())
                .build();
    }
}
