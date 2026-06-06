package com.gdg.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gdg.backend.entity.Entry;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class CalendarRecord {

    private LocalDate date;

    @JsonProperty("drillId")
    private Integer drillId;

    @JsonProperty("isCompleted")
    private Boolean isCompleted;

    public static CalendarRecord from(Entry entry) {
        return CalendarRecord.builder()
                .date(entry.getRecordedDate())
                .drillId(entry.getDrillId())
                .isCompleted(entry.getDrillCompleted())
                .build();
    }
}
