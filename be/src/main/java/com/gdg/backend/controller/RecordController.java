package com.gdg.backend.controller;

import com.gdg.backend.dto.response.CalendarRecord;
import com.gdg.backend.dto.response.DailyRecordResponse;
import com.gdg.backend.entity.User;
import com.gdg.backend.service.RecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Record", description = "기록 조회 관련 API (Entry 기반 조회)")
@RestController
@RequestMapping("/records")
@RequiredArgsConstructor
public class RecordController {

    private final RecordService recordService;

    @Operation(summary = "캘린더 조회", description = "해당 월의 날짜별 드릴 완료 여부를 조회합니다.")
    @GetMapping("/calendar")
    public ResponseEntity<List<CalendarRecord>> getCalendar(
            @AuthenticationPrincipal User user,
            @RequestParam int year,
            @RequestParam int month) {
        return ResponseEntity.ok(recordService.getCalendar(user.getUserId(), year, month));
    }

    @Operation(summary = "일별 기록 조회", description = "특정 날짜의 드릴 상세 정보를 조회합니다.")
    @GetMapping("/daily/{date}")
    public ResponseEntity<DailyRecordResponse> getDailyRecord(
            @AuthenticationPrincipal User user,
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        return ResponseEntity.ok(recordService.getDailyRecord(user.getUserId(), date));
    }
}
