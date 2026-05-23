package com.gdg.backend.service;

import com.gdg.backend.dto.response.DrillTodayResponse;
import com.gdg.backend.entity.Entry;
import com.gdg.backend.repository.EntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DrillService {

    private final EntryRepository entryRepository;

    /**
     * 오늘 날짜 기준으로 해당 유저의 드릴 완료 여부를 조회한다.
     * Entry가 존재하면 드릴 결과를 반환하고, 없으면 hasDrill=false를 반환한다.
     */
    public DrillTodayResponse getTodayDrill(Long userId) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        return entryRepository.findFirstByUser_UserIdAndCreatedAtBetweenOrderByCreatedAtDesc(userId, startOfDay, endOfDay)
                .map(DrillTodayResponse::from)
                .orElse(DrillTodayResponse.empty());
    }
}
