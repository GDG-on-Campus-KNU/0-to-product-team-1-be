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
        return reportWeeklyRepository.findAllByUserIdOrderByWeekIdDesc(userId).stream()
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

        if (!report.isChecked()) {
            report.setChecked(true);
            reportWeeklyRepository.save(report);
        }

        LocalDate[] range = getWeekRange(weekId);
        List<DailyDrillRecord> dailyDrills = buildDailyDrills(userId, range);
        LifestyleSummary lifestyleSummary = buildLifestyleSummary(userId, range);

        Map<String, Object> feBlocks = WeeklyReportCompat.toFeBlocks(
                report.getBlocksJson(), report.getVisualizationsJson());
        Map<String, Object> feVisualizations = WeeklyReportCompat.toFeVisualizations(
                report.getBlocksJson(), report.getVisualizationsJson());

        return WeeklyReportResponse.from(report, feBlocks, feVisualizations, dailyDrills, lifestyleSummary);
    }

    public void saveUserMemo(String weekId, Long userId, String memo) {
        ReportWeekly report = reportWeeklyRepository.findByWeekIdAndUserId(weekId, userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주간 리포트입니다."));

        if (report.getUserMemo() != null) {
            throw new IllegalStateException("이미 작성된 메모는 수정할 수 없습니다.");
        }

        report.setUserMemo(memo);
        reportWeeklyRepository.save(report);
    }

    public List<MonthlyReportResponse> getMonthlyReports(Long userId) {
        return reportMonthlyRepository.findAllByUserIdOrderByMonthIdDesc(userId).stream()
                .map(MonthlyReportResponse::from)
                .toList();
    }

    public MonthlyReportResponse getMonthlyReport(String monthId, Long userId) {
        return reportMonthlyRepository.findByMonthIdAndUserId(monthId, userId)
                .map(MonthlyReportResponse::from)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 월간 리포트입니다."));
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
