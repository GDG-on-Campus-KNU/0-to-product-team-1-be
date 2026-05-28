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
}
