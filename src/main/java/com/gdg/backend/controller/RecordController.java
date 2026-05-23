package com.gdg.backend.controller;

import com.gdg.backend.dto.response.CalendarResponse;
import com.gdg.backend.dto.response.DailyRecordResponse;
import com.gdg.backend.dto.response.WeeklyReportDetailResponse;
import com.gdg.backend.dto.response.WeeklyReportListResponse;
import com.gdg.backend.service.RecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Record", description = "기록 조회 관련 API")
@RestController
@RequestMapping("/records")
@RequiredArgsConstructor
public class RecordController {

    private final RecordService recordService;

    // TODO: 인증 구현 후 @AuthenticationPrincipal로 userId 추출하도록 변경
    @Operation(summary = "캘린더 조회", description = "해당 월의 날짜별 드릴 완료 여부를 조회합니다.")
    @GetMapping("/calendar")
    public ResponseEntity<CalendarResponse> getCalendar(
            @RequestParam Long userId,
            @RequestParam int year,
            @RequestParam int month) {
        return ResponseEntity.ok(recordService.getCalendar(userId, year, month));
    }

    // TODO: 인증 구현 후 @AuthenticationPrincipal로 userId 추출하도록 변경
    @Operation(summary = "일별 기록 조회", description = "특정 날짜의 드릴 상세 정보를 조회합니다.")
    @GetMapping("/daily/{date}")
    public ResponseEntity<DailyRecordResponse> getDailyRecord(
            @RequestParam Long userId,
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        return ResponseEntity.ok(recordService.getDailyRecord(userId, date));
    }

    // TODO: 인증 구현 후 @AuthenticationPrincipal로 userId 추출하도록 변경
    @Operation(summary = "주간 리포트 목록 조회", description = "해당 유저의 주간 리포트 목록을 최신순으로 조회합니다.")
    @GetMapping("/weekly")
    public ResponseEntity<List<WeeklyReportListResponse>> getWeeklyReports(
            @RequestParam Long userId) {
        return ResponseEntity.ok(recordService.getWeeklyReports(userId));
    }

    @Operation(summary = "주간 리포트 상세 조회", description = "특정 주간 리포트의 상세 정보와 인사이트를 조회합니다.")
    @GetMapping("/weekly/{weekId}")
    public ResponseEntity<WeeklyReportDetailResponse> getWeeklyReportDetail(
            @PathVariable Long weekId) {
        return ResponseEntity.ok(recordService.getWeeklyReportDetail(weekId));
    }
}
