package com.gdg.backend.service;

import com.gdg.backend.dto.response.CalendarResponse;
import com.gdg.backend.dto.response.DailyRecordResponse;
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

    public CalendarResponse getCalendar(Long userId, int year, int month) {
        LocalDateTime startOfMonth = LocalDate.of(year, month, 1).atStartOfDay();
        LocalDateTime endOfMonth = startOfMonth.plusMonths(1);

        List<Entry> entries = entryRepository.findAllByUser_UserIdAndCreatedAtBetween(userId, startOfMonth, endOfMonth);

        return CalendarResponse.of(year, month, entries);
    }

    public DailyRecordResponse getDailyRecord(Long userId, LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        return entryRepository.findFirstByUser_UserIdAndCreatedAtBetweenOrderByCreatedAtDesc(userId, startOfDay, endOfDay)
                .map(DailyRecordResponse::from)
                .orElse(DailyRecordResponse.empty());
    }
}
