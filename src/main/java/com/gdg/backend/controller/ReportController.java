package com.gdg.backend.controller;

import com.gdg.backend.dto.response.MonthlyReportResponse;
import com.gdg.backend.dto.response.WeeklyReportResponse;
import com.gdg.backend.entity.User;
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

    @Operation(summary = "주간 리포트 목록", description = "해당 유저의 주간 리포트를 최신순으로 조회합니다.")
    @GetMapping("/weekly")
    public ResponseEntity<List<WeeklyReportResponse>> getWeeklyReports(
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

    /* ── 테스트용 Mock API ── */

    @Operation(
            summary = "[TEST] Mock 주간 리포트 생성",
            description = "ML 서버 없이 더미 데이터로 주간 리포트를 생성합니다. "
                    + "FE 리포트 페이지 UI 테스트 전용이며, 생성 후 GET /reports/weekly 로 조회할 수 있습니다."
    )
    @PostMapping("/weekly/test")
    public ResponseEntity<WeeklyReportResponse> createMockWeeklyReport(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(reportService.createMockWeeklyReport(user.getUserId()));
    }

    @Operation(
            summary = "[TEST] Mock 월간 리포트 생성",
            description = "ML 서버 없이 더미 데이터로 월간 리포트를 생성합니다. "
                    + "FE 리포트 페이지 UI 테스트 전용이며, 생성 후 GET /reports/monthly 로 조회할 수 있습니다."
    )
    @PostMapping("/monthly/test")
    public ResponseEntity<MonthlyReportResponse> createMockMonthlyReport(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(reportService.createMockMonthlyReport(user.getUserId()));
    }
}
