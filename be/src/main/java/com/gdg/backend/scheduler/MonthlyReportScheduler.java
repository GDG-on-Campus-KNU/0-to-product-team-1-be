package com.gdg.backend.scheduler;

import com.gdg.backend.entity.ReportMonthly;
import com.gdg.backend.repository.EntryRepository;
import com.gdg.backend.repository.ReportMonthlyRepository;
import com.gdg.backend.repository.UserRepository;
import com.gdg.backend.service.MlClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class MonthlyReportScheduler {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter MONTH_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM");

    private final UserRepository userRepository;
    private final EntryRepository entryRepository;
    private final ReportMonthlyRepository reportMonthlyRepository;
    private final MlClientService mlClientService;

    @Scheduled(cron = "0 0 6 1 * *", zone = "Asia/Seoul")
    public void generateMonthlyReports() {
        String monthId = getPreviousMonthId();
        LocalDateTime monthStart = getPreviousMonthStart();
        LocalDateTime monthEnd = getPreviousMonthEnd();

        log.info("월간 리포트 cron 시작: monthId={}", monthId);

        userRepository.findAll().forEach(user -> {
            try {
                long count = entryRepository.countByUser_UserIdAndCreatedAtBetween(
                        user.getUserId(), monthStart, monthEnd);

                if (count < 5) {
                    log.debug("사용자 {} entries 부족({}개), 스킵", user.getUserId(), count);
                    return;
                }

                Map<String, Object> result = mlClientService.callMonthly(
                        String.valueOf(user.getUserId()), monthId);

                ReportMonthly report = ReportMonthly.builder()
                        .monthId(monthId)
                        .userId(user.getUserId())
                        .user(user)
                        .blocksJson(result)
                        .generatedAt(LocalDateTime.now(KST))
                        .build();
                reportMonthlyRepository.save(report);

                log.info("사용자 {} 월간 리포트 생성 완료", user.getUserId());
            } catch (Exception e) {
                log.error("사용자 {} 월간 리포트 생성 실패", user.getUserId(), e);
            }
        });
    }

    private String getPreviousMonthId() {
        return LocalDate.now(KST).minusMonths(1).format(MONTH_FORMAT);
    }

    private LocalDateTime getPreviousMonthStart() {
        LocalDate firstOfLastMonth = LocalDate.now(KST).minusMonths(1).withDayOfMonth(1);
        return firstOfLastMonth.atStartOfDay();
    }

    private LocalDateTime getPreviousMonthEnd() {
        LocalDate firstOfThisMonth = LocalDate.now(KST).withDayOfMonth(1);
        return firstOfThisMonth.atStartOfDay();
    }
}
