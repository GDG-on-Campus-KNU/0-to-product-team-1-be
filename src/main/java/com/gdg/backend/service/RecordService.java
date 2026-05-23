package com.gdg.backend.service;

import com.gdg.backend.dto.response.CalendarResponse;
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
}
