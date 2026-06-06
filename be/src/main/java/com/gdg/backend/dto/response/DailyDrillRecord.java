package com.gdg.backend.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class DailyDrillRecord {
    private LocalDate date;
    private String drillCategory;
    private Boolean drillCompleted;

    public static DailyDrillRecord of(LocalDate date, String drillCategory, Boolean drillCompleted) {
        return DailyDrillRecord.builder()
                .date(date)
                .drillCategory(drillCategory)
                .drillCompleted(drillCompleted)
                .build();
    }
}
