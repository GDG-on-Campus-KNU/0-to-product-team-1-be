package com.gdg.backend.controller;

import com.gdg.backend.dto.response.MonthlyReportResponse;
import com.gdg.backend.dto.response.WeeklyReportResponse;
import com.gdg.backend.dto.response.WeeklyReportSummaryResponse;
import com.gdg.backend.entity.User;
import com.gdg.backend.scheduler.WeeklyReportScheduler;
import com.gdg.backend.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Report", description = "주간/월간 리포트 조회 API")
@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final WeeklyReportScheduler weeklyReportScheduler;

    @Operation(summary = "주간 리포트 목록", description = "해당 유저의 주간 리포트를 최신순으로 조회합니다.")
    @GetMapping("/weekly")
    public ResponseEntity<List<WeeklyReportSummaryResponse>> getWeeklyReports(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(reportService.getWeeklyReports(user.getUserId()));
    }

    @Operation(summary = "주간 리포트 상세", description = "특정 주간 리포트의 상세 정보를 조회합니다.")
    @GetMapping("/weekly/{weekId}")
    public ResponseEntity<WeeklyReportResponse> getWeeklyReport(
            @PathVariable String weekId,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(reportService.getWeeklyReport(weekId, user.getUserId()));
    }

    @Operation(summary = "월간 리포트 목록", description = "해당 유저의 월간 리포트를 최신순으로 조회합니다.")
    @GetMapping("/monthly")
    public ResponseEntity<List<MonthlyReportResponse>> getMonthlyReports(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(reportService.getMonthlyReports(user.getUserId()));
    }

    @Operation(summary = "월간 리포트 상세", description = "특정 월간 리포트의 상세 정보를 조회합니다.")
    @GetMapping("/monthly/{monthId}")
    public ResponseEntity<MonthlyReportResponse> getMonthlyReport(
            @PathVariable String monthId,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(reportService.getMonthlyReport(monthId, user.getUserId()));
    }

    /* ── 테스트용 API ── */

    @Operation(
            summary = "[TEST] 주간 리포트 스케줄러 수동 실행",
            description = "ML 서버 연동해서 전체 유저 주간 리포트를 즉시 생성. 수정된 필드명 검증용."
    )
    @PostMapping("/weekly/trigger")
    public ResponseEntity<Void> triggerWeeklyReport() {
        weeklyReportScheduler.generateWeeklyReports();
        return ResponseEntity.ok().build();
    }


    @Operation(
            summary = "[TEST] Mock 주간 리포트 생성",
            description = "ML 서버 없이 더미 데이터로 주간 리포트를 생성 "
    )
    @PostMapping("/weekly/test")
    public ResponseEntity<WeeklyReportResponse> createMockWeeklyReport(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(reportService.createMockWeeklyReport(user.getUserId()));
    }

    @Operation(
            summary = "[TEST] Mock 월간 리포트 생성",
            description = "ML 서버 없이 더미 데이터로 월간 리포트를 생성 "
    )
    @PostMapping("/monthly/test")
    public ResponseEntity<MonthlyReportResponse> createMockMonthlyReport(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(reportService.createMockMonthlyReport(user.getUserId()));
    }
}
