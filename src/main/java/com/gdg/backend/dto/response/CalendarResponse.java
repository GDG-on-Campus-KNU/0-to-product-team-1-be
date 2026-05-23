package com.gdg.backend.dto.response;

import com.gdg.backend.entity.Entry;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class CalendarResponse {

    private int year;
    private int month;
    private List<DailyRecord> records;

    @Getter
    @Builder
    public static class DailyRecord {
        private LocalDate date;
        private boolean drillComplete;

        public static DailyRecord from(Entry entry) {
            return DailyRecord.builder()
                    .date(entry.getCreatedAt().toLocalDate())
                    .drillComplete(Boolean.TRUE.equals(entry.getDrillComplete()))
                    .build();
        }
    }

    public static CalendarResponse of(int year, int month, List<Entry> entries) {
        List<DailyRecord> records = entries.stream()
                .map(DailyRecord::from)
                .toList();

        return CalendarResponse.builder()
                .year(year)
                .month(month)
                .records(records)
                .build();
    }
}
