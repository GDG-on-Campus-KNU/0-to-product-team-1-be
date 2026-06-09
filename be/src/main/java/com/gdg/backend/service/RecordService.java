package com.gdg.backend.service;

import com.gdg.backend.dto.response.CalendarRecord;
import com.gdg.backend.dto.response.DrillTodayResponse;
import com.gdg.backend.entity.Entry;
import com.gdg.backend.repository.EntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


@Service
@RequiredArgsConstructor
public class RecordService {

    private final EntryRepository entryRepository;

    public List<CalendarRecord> getCalendar(Long userId, int year, int month) {
        LocalDate startOfMonth = LocalDate.of(year, month, 1);
        LocalDate endOfMonth = startOfMonth.plusMonths(1);

        return entryRepository.findByUser_UserIdAndRecordedDateBetween(userId, startOfMonth, endOfMonth)
                .stream()
                .filter(entry -> entry.getDrillId() != null
                        || (entry.getRecommendationJson() != null && entry.getRecommendationJson().get("drill") != null))
                .map(CalendarRecord::from)
                .toList();
    }

    public DrillTodayResponse getDailyRecord(Long userId, LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        return entryRepository.findFirstByUser_UserIdAndCreatedAtBetweenOrderByCreatedAtDesc(userId, startOfDay, endOfDay)
                .map(DrillTodayResponse::from)
                .orElse(DrillTodayResponse.empty());
    }
}
