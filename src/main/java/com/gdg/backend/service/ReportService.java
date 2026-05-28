package com.gdg.backend.service;

import com.gdg.backend.dto.response.MonthlyReportResponse;
import com.gdg.backend.dto.response.WeeklyReportResponse;
import com.gdg.backend.repository.ReportMonthlyRepository;
import com.gdg.backend.repository.ReportWeeklyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportWeeklyRepository reportWeeklyRepository;
    private final ReportMonthlyRepository reportMonthlyRepository;

    public List<WeeklyReportResponse> getWeeklyReports(Long userId) {
        return reportWeeklyRepository.findAllByUserIdOrderByGeneratedAtDesc(userId).stream()
                .map(WeeklyReportResponse::from)
                .toList();
    }

    public WeeklyReportResponse getWeeklyReport(String weekId, Long userId) {
        return reportWeeklyRepository.findByWeekIdAndUserId(weekId, userId)
                .map(WeeklyReportResponse::from)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주간 리포트입니다."));
    }

    public List<MonthlyReportResponse> getMonthlyReports(Long userId) {
        return reportMonthlyRepository.findAllByUserIdOrderByGeneratedAtDesc(userId).stream()
                .map(MonthlyReportResponse::from)
                .toList();
    }

    public MonthlyReportResponse getMonthlyReport(String monthId, Long userId) {
        return reportMonthlyRepository.findByMonthIdAndUserId(monthId, userId)
                .map(MonthlyReportResponse::from)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 월간 리포트입니다."));
    }
}
