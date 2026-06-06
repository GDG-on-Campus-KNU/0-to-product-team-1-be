package com.gdg.backend.service;

import com.gdg.backend.dto.response.MonthlyReportResponse;
import com.gdg.backend.dto.response.WeeklyReportResponse;
import com.gdg.backend.dto.response.WeeklyReportSummaryResponse;
import com.gdg.backend.entity.ReportMonthly;
import com.gdg.backend.entity.ReportWeekly;
import com.gdg.backend.repository.EntryRepository;
import com.gdg.backend.repository.ReportMonthlyRepository;
import com.gdg.backend.repository.ReportWeeklyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReportService {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final ReportWeeklyRepository reportWeeklyRepository;
    private final ReportMonthlyRepository reportMonthlyRepository;
    private final EntryRepository entryRepository;

    public List<WeeklyReportSummaryResponse> getWeeklyReports(Long userId) {
        return reportWeeklyRepository.findAllByUserIdOrderByGeneratedAtDesc(userId).stream()
                .map(report -> toSummaryResponse(report, userId))
                .toList();
    }

    private WeeklyReportSummaryResponse toSummaryResponse(ReportWeekly report, Long userId) {
        LocalDate[] range = getWeekRange(report.getWeekId());
        int total = countTotalDrills(userId, range);
        int completed = countCompletedDrills(userId, range);

        return WeeklyReportSummaryResponse.from(report, completed, total);
    }

    private int countTotalDrills(Long userId, LocalDate[] range) {
        return entryRepository.countByUser_UserIdAndRecordedDateBetween(userId, range[0], range[1]);
    }

    private int countCompletedDrills(Long userId, LocalDate[] range) {
        return entryRepository.countByUser_UserIdAndRecordedDateBetweenAndDrillCompletedTrue(userId, range[0], range[1]);
    }

    private LocalDate[] getWeekRange(String weekId) {
        LocalDate monday = LocalDate.parse(weekId + "-1", DateTimeFormatter.ofPattern("YYYY-'W'ww-e", Locale.KOREA));
        LocalDate sunday = monday.plusDays(6);
        return new LocalDate[]{monday, sunday};
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

    /**
     * 테스트용 Mock 주간 리포트 생성 (ML 서버 불필요)
     */
    public WeeklyReportResponse createMockWeeklyReport(Long userId) {
        LocalDate now = LocalDate.now(KST);
        int week = now.get(java.time.temporal.WeekFields.ISO.weekOfWeekBasedYear());
        String weekId = now.getYear() + "-W" + String.format("%02d", week);

        Map<String, Object> blocksJson = new HashMap<>();
        blocksJson.put("block1_action_rate", Map.of(
                "rate", 75.0,
                "description", "이번 주 드릴 수행률입니다."
        ));
        blocksJson.put("block2_emotion_summary", Map.of(
                "primary_emotion", "불안",
                "description", "이번 주 가장 많이 나타난 감정입니다."
        ));
        blocksJson.put("block3_weekly_tip", Map.of(
                "tip", "규칙적인 수면 패턴을 유지해보세요.",
                "source", "CBT 기반 권고"
        ));

        Map<String, Object> visualizationsJson = new HashMap<>();
        visualizationsJson.put("emotion_trend", List.of(
                Map.of("day", "Mon", "score", 3),
                Map.of("day", "Tue", "score", 4),
                Map.of("day", "Wed", "score", 2),
                Map.of("day", "Thu", "score", 5),
                Map.of("day", "Fri", "score", 3),
                Map.of("day", "Sat", "score", 4),
                Map.of("day", "Sun", "score", 4)
        ));
        visualizationsJson.put("category_distribution", Map.of(
                "호흡", 3, "인지재구성", 2, "마음챙김", 1
        ));

        ReportWeekly report = ReportWeekly.builder()
                .weekId(weekId)
                .userId(userId)
                .blocksJson(blocksJson)
                .visualizationsJson(visualizationsJson)
                .generatedAt(LocalDateTime.now(KST))
                .build();
        reportWeeklyRepository.save(report);

        return WeeklyReportResponse.from(report);
    }

    /**
     * 테스트용 Mock 월간 리포트 생성 (ML 서버 불필요)
     */
    public MonthlyReportResponse createMockMonthlyReport(Long userId) {
        String monthId = LocalDate.now(KST).minusMonths(1)
                .format(DateTimeFormatter.ofPattern("yyyy-MM"));

        Map<String, Object> blocksJson = new HashMap<>();
        blocksJson.put("monthly_summary", Map.of(
                "total_entries", 22,
                "total_drills", 18,
                "avg_condition", 3.5
        ));
        blocksJson.put("monthly_emotion", Map.of(
                "primary", "불안",
                "secondary", "우울",
                "positive_ratio", 0.45
        ));
        blocksJson.put("monthly_insight", Map.of(
                "message", "지난 달 대비 긍정 감정 비율이 10% 증가했습니다.",
                "suggestion", "현재 루틴을 유지하면서 마음챙김 드릴을 추가해보세요."
        ));

        ReportMonthly report = ReportMonthly.builder()
                .monthId(monthId)
                .userId(userId)
                .blocksJson(blocksJson)
                .generatedAt(LocalDateTime.now(KST))
                .build();
        reportMonthlyRepository.save(report);

        return MonthlyReportResponse.from(report);
    }
}
