package com.gdg.backend.scheduler;

import com.gdg.backend.dto.ml.MlReportRequest;
import com.gdg.backend.dto.ml.MlWeeklyRequest;
import com.gdg.backend.entity.Report;
import com.gdg.backend.repository.EntryRepository;
import com.gdg.backend.repository.ReportRepository;
import com.gdg.backend.repository.UserRepository;
import com.gdg.backend.service.EmailService;
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
import java.util.Map;

/**
 * 7.1 매주 일요일 20:00 KST 주간 리포트 생성
 * 7.3 메일 발송 보류 (협의 완료 — FE 직접 조회)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WeeklyReportScheduler {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final UserRepository userRepository;
    private final EntryRepository entryRepository;
    private final ReportRepository reportRepository;
    private final MlClientService mlClientService;
    private final EmailService emailService;

    @Scheduled(cron = "0 0 20 * * SUN", zone = "Asia/Seoul")
    public void generateWeeklyReports() {
        String weekId = getCurrentWeekId();
        LocalDateTime weekStart = getWeekStart();
        LocalDateTime weekEnd = LocalDateTime.now(KST);

        log.info("주간 리포트 cron 시작: weekId={}", weekId);

        // 7.5 한 명 실패해도 다른 사용자 영향 X
        userRepository.findAll().forEach(user -> {
            try {
                long count = entryRepository.countByUser_UserIdAndCreatedAtBetween(
                        user.getUserId(), weekStart, weekEnd);

                if (count < 3) {
                    log.debug("사용자 {} entries 부족({}개), 스킵", user.getUserId(), count);
                    return;
                }

                // 7.2 ML POST /reports + /weekly 호출
                MlReportRequest reportReq = MlReportRequest.builder()
                        .userId(String.valueOf(user.getUserId()))
                        .weekId(weekId)
                        .build();
                Map<String, Object> reportResult = mlClientService.callReports(reportReq);

                MlWeeklyRequest weeklyReq = MlWeeklyRequest.builder()
                        .userId(String.valueOf(user.getUserId()))
                        .weekId(weekId)
                        .entryCount((int) count)
                        .build();
                Map<String, Object> weeklyResult = mlClientService.callWeekly(weeklyReq);

                Report report = Report.builder()
                        .user(user)
                        .weekOf(weekId)
                        .patternAnalysis(reportResult)
                        .emotionDistribution(weeklyResult)
                        .build();
                reportRepository.save(report);

                // 7.4 주간 퀴즈 준비 메일 발송
                emailService.sendWeeklyQuizReady(user.getEmail(), weekId);

                log.info("사용자 {} 주간 리포트 생성 완료", user.getUserId());
            } catch (Exception e) {
                log.error("사용자 {} 주간 리포트 생성 실패", user.getUserId(), e);
            }
        });
    }

    // 7.6 week_id 형식: "2026-W21"
    private String getCurrentWeekId() {
        LocalDate today = LocalDate.now(KST);
        int week = today.get(java.time.temporal.WeekFields.ISO.weekOfWeekBasedYear());
        return today.getYear() + "-W" + String.format("%02d", week);
    }

    private LocalDateTime getWeekStart() {
        LocalDate monday = LocalDate.now(KST).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        return monday.atStartOfDay();
    }
}
