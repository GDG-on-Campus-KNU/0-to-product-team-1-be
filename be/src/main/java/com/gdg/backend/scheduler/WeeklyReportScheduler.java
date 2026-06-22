package com.gdg.backend.scheduler;

import com.gdg.backend.dto.ml.MlWeeklyRequest;
import com.gdg.backend.entity.ReportWeekly;
import com.gdg.backend.repository.EntryRepository;
import com.gdg.backend.repository.ReportWeeklyRepository;
import com.gdg.backend.repository.UserRepository;
import com.gdg.backend.service.MlClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class WeeklyReportScheduler {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final UserRepository userRepository;
    private final EntryRepository entryRepository;
    private final ReportWeeklyRepository reportWeeklyRepository;
    private final MlClientService mlClientService;

    @Scheduled(cron = "0 0 20 * * SUN", zone = "Asia/Seoul")
    public void generateWeeklyReports() {
        generateWeeklyReports(null);
    }

    public void generateWeeklyReports(String targetWeekId) {
        String weekId = (targetWeekId != null) ? targetWeekId : getCurrentWeekId();
        LocalDateTime weekStart = (targetWeekId != null) ? getWeekStartOf(targetWeekId) : getWeekStart();
        LocalDateTime weekEnd = (targetWeekId != null)
                ? getWeekStartOf(targetWeekId).plusDays(6).withHour(23).withMinute(59).withSecond(59)
                : LocalDateTime.now(KST);

        log.info("주간 리포트 cron 시작: weekId={}", weekId);

        userRepository.findAll().forEach(user -> {
            try {
                LocalDate weekStartDate = weekStart.toLocalDate();
                LocalDate weekEndDate = weekEnd.toLocalDate();
                List<com.gdg.backend.entity.Entry> weekEntries = entryRepository
                        .findByUser_UserIdAndRecordedDateBetween(user.getUserId(), weekStartDate, weekEndDate);

                if (weekEntries.size() < 3) {
                    log.debug("사용자 {} entries 부족({}개), 스킵", user.getUserId(), weekEntries.size());
                    return;
                }

                List<Map<String, Object>> entryData = toEntryData(weekEntries);

                LocalDate prevStartDate = weekStartDate.minusWeeks(1);
                LocalDate prevEndDate = weekEndDate.minusWeeks(1);
                List<com.gdg.backend.entity.Entry> prevWeekEntries = entryRepository
                        .findByUser_UserIdAndRecordedDateBetween(user.getUserId(), prevStartDate, prevEndDate);
                List<Map<String, Object>> prevEntryData = toEntryData(prevWeekEntries);

                MlWeeklyRequest weeklyReq = MlWeeklyRequest.builder()
                        .userId(String.valueOf(user.getUserId()))
                        .weekId(weekId)
                        .entryCount(weekEntries.size())
                        .entries(entryData)
                        .prevEntries(prevEntryData.isEmpty() ? null : prevEntryData)
                        .build();
                Map<String, Object> weeklyResult = mlClientService.callWeekly(weeklyReq);

                // blocksJson: 텍스트/요약 블록
                List<String> blockKeys = List.of("overview", "dominant_pattern", "drill_action",
                        "self_check_quiz", "baseline_card", "weekly_coaching");
                Map<String, Object> blocksJson = new java.util.HashMap<>();
                blockKeys.forEach(k -> { if (weeklyResult.containsKey(k)) blocksJson.put(k, weeklyResult.get(k)); });

                // visualizationsJson: 차트/시각화 데이터
                List<String> vizKeys = List.of("condition_flow", "pattern_diff", "discoveries",
                        "emotion_pentagon", "calendar_distribution", "pattern_diff_message", "discoveries_message");
                Map<String, Object> visualizationsJson = new java.util.HashMap<>();
                vizKeys.forEach(k -> { if (weeklyResult.containsKey(k)) visualizationsJson.put(k, weeklyResult.get(k)); });

                ReportWeekly report = ReportWeekly.builder()
                        .weekId(weekId)
                        .userId(user.getUserId())
                        .user(user)
                        .blocksJson(blocksJson)
                        .visualizationsJson(visualizationsJson)
                        .generatedAt(LocalDateTime.now(KST))
                        .build();
                reportWeeklyRepository.save(report);

                log.info("사용자 {} 주간 리포트 생성 완료", user.getUserId());
            } catch (Exception e) {
                log.error("사용자 {} 주간 리포트 생성 실패", user.getUserId(), e);
            }
        });
    }

    private String getCurrentWeekId() {
        LocalDate today = LocalDate.now(KST);
        int week = today.get(java.time.temporal.WeekFields.ISO.weekOfWeekBasedYear());
        return today.getYear() + "-W" + String.format("%02d", week);
    }

    private LocalDateTime getWeekStart() {
        LocalDate monday = LocalDate.now(KST).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        return monday.atStartOfDay();
    }

    private LocalDateTime getWeekStartOf(String weekId) {
        // weekId 형식: "2026-W19"
        int year = Integer.parseInt(weekId.substring(0, 4));
        int week = Integer.parseInt(weekId.substring(6));
        LocalDate monday = LocalDate.of(year, 1, 4)
                .with(java.time.temporal.WeekFields.ISO.weekOfWeekBasedYear(), week)
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        return monday.atStartOfDay();
    }

    private List<Map<String, Object>> toEntryData(List<com.gdg.backend.entity.Entry> entries) {
        return entries.stream()
                .map(e -> {
                    Map<String, Object> m = new java.util.HashMap<>();
                    m.put("created_at", e.getCreatedAt() != null ? e.getCreatedAt().toString() : null);
                    m.put("self_condition", e.getContextJson() != null ? e.getContextJson().getOrDefault("self_condition", 0) : 0);
                    m.put("context", e.getContextJson() != null ? e.getContextJson() : Map.of());
                    m.put("label_result", e.getLabelResultJson() != null ? e.getLabelResultJson() : Map.of());
                    return m;
                })
                .collect(Collectors.toList());
    }
}
