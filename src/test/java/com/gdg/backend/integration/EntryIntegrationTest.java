package com.gdg.backend.integration;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 9.x 통합 테스트 7 시나리오 — 실행: docker-compose up 후 ML 서버 기동 필요
 * @Disabled 제거 후 실행
 */
@Disabled("ML 서버 + DB 필요 — 통합 환경에서만 실행")
class EntryIntegrationTest {

    // 9.1 정상 입력 → 200 (type=drill 등 5 type 중 하나)
    @Test
    @DisplayName("9.1 정상 입력 → 200")
    void normalInput_returns200() {
        // POST /entries { text: "오늘 힘들었다", context: {...} }
        // assert status == 200
        // assert recommendation.type in [drill, crisis_card, positive_card, ask_user, skip]
    }

    // 9.2 분당 2번째 입력 → 429 + Retry-After 헤더
    @Test
    @DisplayName("9.2 분당 2회 → 429 + Retry-After")
    void secondRequestPerMinute_returns429WithRetryAfter() {
        // POST /entries 2회 연속
        // assert status == 429
        // assert header Retry-After exists
    }

    // 9.3 위기 텍스트 → crisis_card (드릴 0)
    @Test
    @DisplayName("9.3 위기 입력 → crisis_card")
    void crisisText_returnsCrisisCard() {
        // POST /entries { text: "사라지고 싶다" }
        // assert recommendation.type == "crisis_card"
        // assert recommendation.drill == null
    }

    // 9.4 빈 텍스트 → 400 VALIDATION_ERROR
    @Test
    @DisplayName("9.4 빈 텍스트 → 400 VALIDATION_ERROR")
    void emptyText_returns400() {
        // POST /entries { text: "" }
        // assert status == 400
        // assert code == "VALIDATION_ERROR"
    }

    // 9.5 같은 time_of_day 중복 → 409 (협의 완료: 두 번째 입력 거부)
    @Test
    @DisplayName("9.5 같은 time_of_day 중복 → 409")
    void duplicateTimeOfDay_returns409() {
        // POST /entries { time_of_day: "morning" } x 2
        // assert 두 번째 == 409
    }

    // 9.6 일요일 cron 후 /records/weekly에 리포트 1건
    @Test
    @DisplayName("9.6 주간 cron → 리포트 생성")
    void weeklyReportCron_createsReport() {
        // WeeklyReportScheduler.generateWeeklyReports() 직접 호출
        // assert reportRepository.count() == 이전+1
    }

    // 9.7 12명 동시 입력 → 모두 200 (user 격리)
    @Test
    @DisplayName("9.7 동시 12명 → 모두 200")
    void concurrent12Users_allReturn200() {
        // 12개 스레드로 POST /entries 동시 호출
        // assert all == 200
    }
}
