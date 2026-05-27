package com.gdg.backend.scheduler;

import com.gdg.backend.repository.EntryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;

@Component
@RequiredArgsConstructor
@Slf4j
public class AwaitingAnswerScheduler {

    private final EntryRepository entryRepository;

    @Scheduled(cron = "0 0 1 * * *", zone = "Asia/Seoul")
    @Transactional
    public void expireAwaitingAnswers() {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        entryRepository.findByAwaitingAnswerTrueAndRecordedDateBefore(today)
                .forEach(entry -> {
                    entry.setAwaitingAnswer(false);
                    log.info("미답변 자동 마감: entryId={}", entry.getEntryId());
                });
    }
}
