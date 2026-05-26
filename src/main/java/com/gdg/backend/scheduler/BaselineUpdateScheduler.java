package com.gdg.backend.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 14.16 매일 0:30 KST baseline 갱신 cron
 */
@Slf4j
@Component
public class BaselineUpdateScheduler {

    @Scheduled(cron = "0 30 0 * * *", zone = "Asia/Seoul")
    public void updateBaselines() {
        log.info("Baseline 갱신 cron 시작");
        // ML baseline 엔드포인트 연동 필요 시 MlClientService.callBaseline() 추가
        // 현재: 로그만 (ML 서버 baseline 자동 관리)
        log.info("Baseline 갱신 cron 완료");
    }
}
