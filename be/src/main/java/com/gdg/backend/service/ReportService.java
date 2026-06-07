package com.gdg.backend.service;

import com.gdg.backend.dto.response.DailyDrillRecord;
import com.gdg.backend.dto.response.LifestyleSummary;
import com.gdg.backend.dto.response.MonthlyReportResponse;
import com.gdg.backend.dto.response.WeeklyReportResponse;
import com.gdg.backend.dto.response.WeeklyReportSummaryResponse;
import com.gdg.backend.entity.Entry;
import com.gdg.backend.entity.ReportMonthly;
import com.gdg.backend.entity.ReportWeekly;
import com.gdg.backend.repository.EntryRepository;
import com.gdg.backend.repository.ReportMonthlyRepository;
import com.gdg.backend.repository.ReportWeeklyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

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
        ReportWeekly report = reportWeeklyRepository.findByWeekIdAndUserId(weekId, userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주간 리포트입니다."));

        LocalDate[] range = getWeekRange(weekId);
        List<DailyDrillRecord> dailyDrills = buildDailyDrills(userId, range);
        LifestyleSummary lifestyleSummary = buildLifestyleSummary(userId, range);

        return WeeklyReportResponse.from(report, dailyDrills, lifestyleSummary);
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
        visualizationsJson.put("emotion_pentagon", Map.of(
                "axes", List.of(
                        Map.of("label", "불안", "value", 0.236),
                        Map.of("label", "우울", "value", 0.071),
                        Map.of("label", "분노", "value", 0.057),
                        Map.of("label", "죄책", "value", 0.0),
                        Map.of("label", "중립", "value", 0.357)
                ),
                "dominant", "중립",
                "entries_used", 7
        ));
        visualizationsJson.put("condition_flow", Map.of(
                "points", List.of(
                        Map.of("dow", "월", "avg_condition", 4.0, "count", 1),
                        Map.of("dow", "화", "avg_condition", 4.0, "count", 1),
                        Map.of("dow", "수", "avg_condition", 3.0, "count", 1),
                        Map.of("dow", "목", "avg_condition", 2.0, "count", 1),
                        Map.of("dow", "금", "avg_condition", 2.0, "count", 1),
                        Map.of("dow", "토", "avg_condition", 3.0, "count", 1),
                        Map.of("dow", "일", "avg_condition", 4.0, "count", 1)
                )
        ));
        visualizationsJson.put("pattern_diff", List.of(
                Map.of("pattern", "미래예측", "current_percent", 75.0, "prev_percent", 60.0, "delta_percent", 15.0, "arrow", "up"),
                Map.of("pattern", "자기비난", "current_percent", 25.0, "prev_percent", 30.0, "delta_percent", -5.0, "arrow", "down")
        ));
        visualizationsJson.put("discoveries", List.of(
                Map.of("text", "잠이 부족한 날 미래예측 표현이 더 자주 보였어요", "category", "context", "source", "system"),
                Map.of("text", "주말에 컨디션이 좋아졌어요", "category", "context", "source", "system"),
                Map.of("text", "이번 주 '괜찮은 하루' 표현이 1회 보였어요", "category", "cognitive", "source", "system", "count", 1)
        ));
        visualizationsJson.put("weekly_coaching", Map.of(
                "state", Map.of("key", "fatigue", "label", "소진기", "summary", "주 전반이 더 좋았고 후반에 가라앉는 흐름이 보였어요."),
                "next_week_focus", Map.of("category", "grounding", "label_ko", "마음 챙김", "reason", "다음 주엔 마음을 먼저 가라앉히는 연습이 도움이 될 수 있어요.")
        ));

        ReportWeekly report = ReportWeekly.of(weekId, userId, blocksJson, visualizationsJson, LocalDateTime.now(KST));
        reportWeeklyRepository.save(report);

        List<DailyDrillRecord> mockDrills = List.of(
                DailyDrillRecord.of(now.with(DayOfWeek.MONDAY), "생각 전환", true),
                DailyDrillRecord.of(now.with(DayOfWeek.TUESDAY), "긍정 확인", true),
                DailyDrillRecord.of(now.with(DayOfWeek.WEDNESDAY), null, false),
                DailyDrillRecord.of(now.with(DayOfWeek.THURSDAY), "마음 챙김", true),
                DailyDrillRecord.of(now.with(DayOfWeek.FRIDAY), "호흡 조절", true),
                DailyDrillRecord.of(now.with(DayOfWeek.SATURDAY), "산책", true),
                DailyDrillRecord.of(now.with(DayOfWeek.SUNDAY), null, false)
        );

        LifestyleSummary mockLifestyle = LifestyleSummary.of(7.2, 45, 4.0, "보통", 8.4, 60.0, 3.5, "보통");

        return WeeklyReportResponse.from(report, mockDrills, mockLifestyle);
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

        ReportMonthly report = ReportMonthly.of(monthId, userId, blocksJson, LocalDateTime.now(KST));
        reportMonthlyRepository.save(report);

        return MonthlyReportResponse.from(report);
    }

    private List<DailyDrillRecord> buildDailyDrills(Long userId, LocalDate[] range) {
        return entryRepository.findByUser_UserIdAndRecordedDateBetween(userId, range[0], range[1])
                .stream()
                .map(entry -> DailyDrillRecord.of(entry.getRecordedDate(), entry.getDrillCategory(), entry.getDrillCompleted()))
                .toList();
    }

    private LifestyleSummary buildLifestyleSummary(Long userId, LocalDate[] range) {
        List<Entry> entries = entryRepository.findByUser_UserIdAndRecordedDateBetween(userId, range[0], range[1]);
        List<Entry> prevEntries = entryRepository.findByUser_UserIdAndRecordedDateBetween(userId, range[0].minusDays(7), range[0].minusDays(1));

        return LifestyleSummary.of(
                avgFromContext(entries, "sleep_hours"),
                avgFromContext(entries, "exercise_today"),
                avgFromContext(entries, "self_condition"),
                modeFromContext(entries, "social_today"),
                avgFromContext(prevEntries, "sleep_hours"),
                avgFromContext(prevEntries, "exercise_today"),
                avgFromContext(prevEntries, "self_condition"),
                modeFromContext(prevEntries, "social_today")
        );
    }

    private double avgFromContext(List<Entry> entries, String key) {
        return entries.stream()
                .filter(e -> e.getContextJson() != null && e.getContextJson().containsKey(key))
                .mapToDouble(e -> ((Number) e.getContextJson().get(key)).doubleValue())
                .average()
                .orElse(0.0);
    }

    private String modeFromContext(List<Entry> entries, String key) {
        return entries.stream()
                .filter(e -> e.getContextJson() != null && e.getContextJson().containsKey(key))
                .map(e -> e.getContextJson().get(key).toString())
                .collect(Collectors.groupingBy(v -> v, Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }
}
