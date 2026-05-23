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

    /**
     * 해당 월의 날짜별 드릴 완료 여부를 조회한다.
     */
    public CalendarResponse getCalendar(Long userId, int year, int month) {
        LocalDateTime startOfMonth = LocalDate.of(year, month, 1).atStartOfDay();
        LocalDateTime endOfMonth = startOfMonth.plusMonths(1);

        List<Entry> entries = entryRepository.findAllByUser_UserIdAndCreatedAtBetween(userId, startOfMonth, endOfMonth);

        return CalendarResponse.of(year, month, entries);
    }

    /**
     * 특정 날짜의 드릴 상세 정보를 조회한다.
     * Entry가 없으면 hasEntry=false를 반환한다.
     */
    public DailyRecordResponse getDailyRecord(Long userId, LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        return entryRepository.findByUser_UserIdAndCreatedAtBetween(userId, startOfDay, endOfDay)
                .map(DailyRecordResponse::from)
                .orElse(DailyRecordResponse.empty());
    }
}
