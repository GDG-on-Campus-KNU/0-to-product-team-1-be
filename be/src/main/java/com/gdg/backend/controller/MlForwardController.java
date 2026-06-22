package com.gdg.backend.controller;

import com.gdg.backend.dto.request.DiscoveriesRequest;
import com.gdg.backend.entity.User;
import com.gdg.backend.repository.EntryRepository;
import com.gdg.backend.service.MlClientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;

import java.util.List;
import java.util.Map;

/**
 * 14.5~14.14 ML 포워딩 엔드포인트
 */
@Tag(name = "ML Forward", description = "ML 서비스 포워딩 API (일부 협의/구현 예정)")
@RestController
@RequiredArgsConstructor
public class MlForwardController {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final MlClientService mlClientService;
    private final EntryRepository entryRepository;

    // 14.5 GET /drills/{id} — 인증 필요(JWT), user 객체 불필요
    @Operation(summary = "드릴 단건 조회")
    @GetMapping("/drills/{id}")
    public ResponseEntity<Map<String, Object>> getDrill(
            @PathVariable String id,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(mlClientService.getDrill(id));
    }

    // 14.6 GET /drills — 인증 필요(JWT), user 객체 불필요
    @Operation(summary = "드릴 전체 목록")
    @GetMapping("/drills")
    public ResponseEntity<List<Map<String, Object>>> getDrills(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(mlClientService.getDrills());
    }

    // 14.9 GET /reports/pending
    @Operation(summary = "미확인 주간 리포트 조회")
    @GetMapping("/reports/pending")
    public ResponseEntity<Map<String, Object>> getReportsPending(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(mlClientService.getReportsPending(String.valueOf(user.getUserId())));
    }

    // 14.10 PATCH /reports/{id}/read
    @Operation(summary = "리포트 읽음 처리")
    @PatchMapping("/reports/{id}/read")
    public ResponseEntity<Void> markReportRead(@PathVariable String id) {
        mlClientService.markReportRead(id);
        return ResponseEntity.ok().build();
    }

    // 14.11 PATCH /weekly/quiz
    @Operation(summary = "주간 퀴즈 제출")
    @PatchMapping("/weekly/quiz")
    public ResponseEntity<Map<String, Object>> patchWeeklyQuiz(
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal User user) {
        body.put("user_id", String.valueOf(user.getUserId()));
        return ResponseEntity.ok(mlClientService.patchWeeklyQuiz(body));
    }

    // 14.12 POST /insights
    @Operation(summary = "인사이트 생성")
    @PostMapping("/insights")
    public ResponseEntity<Map<String, Object>> postInsights(
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal User user) {
        body.put("user_id", String.valueOf(user.getUserId()));
        return ResponseEntity.ok(mlClientService.postInsights(body));
    }

    // 14.13 GET /insights
    @Operation(summary = "인사이트 목록 조회")
    @GetMapping("/insights")
    public ResponseEntity<Map<String, Object>> getInsights(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(mlClientService.getInsights(String.valueOf(user.getUserId())));
    }

    // 드릴 피드백 제출
    @Operation(summary = "드릴 피드백", description = "드릴이 도움이 되었는지 피드백을 ML에 전달합니다.")
    @PostMapping("/feedback")
    public ResponseEntity<Map<String, Object>> postFeedback(
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal User user) {
        body.put("user_id", String.valueOf(user.getUserId()));
        return ResponseEntity.ok(mlClientService.postFeedback(body));
    }

    // 14.14 GET /export
    @Operation(summary = "사용자 데이터 내보내기")
    @GetMapping("/export")
    public ResponseEntity<Map<String, Object>> exportUserData(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(mlClientService.exportUserData(String.valueOf(user.getUserId())));
    }

    // ── ML 포워딩 ──

    // 드릴 거부 — ML POST /reject (body {user_id, drill_id:정수})
    @Operation(summary = "드릴 거부", description = "거부 학습 신호를 ML에 전달합니다. 이후 추천에서 자동 회피.")
    @PostMapping("/drills/{id}/reject")
    public ResponseEntity<Map<String, Object>> rejectDrill(
            @PathVariable String id,
            @AuthenticationPrincipal User user) {
        Map<String, Object> body = new HashMap<>();
        body.put("user_id", String.valueOf(user.getUserId()));
        body.put("drill_id", Integer.parseInt(id)); // ML은 drill_id 정수형
        return ResponseEntity.ok(mlClientService.rejectDrill(body));
    }

    // 나의 발견 저장 — ML POST /insights/user_discovery (week_of 필수)
    @Operation(summary = "나의 발견 저장", description = "사용자가 적은 발견을 ML에 전달 (키워드 매칭 → affinity 가산).")
    @PostMapping("/discoveries")
    public ResponseEntity<Map<String, Object>> postDiscoveries(
            @RequestBody @jakarta.validation.Valid DiscoveriesRequest request,
            @AuthenticationPrincipal User user) {
        Map<String, Object> body = new HashMap<>();
        body.put("user_id", String.valueOf(user.getUserId()));
        body.put("week_of", getCurrentWeekId());
        body.put("discoveries", request.getDiscoveries());
        return ResponseEntity.ok(mlClientService.postUserDiscovery(body));
    }

    // 나의 발견 조회 — ML GET /insights/user_discovery?user_id=&limit=
    @Operation(summary = "나의 발견 조회", description = "최근 발견 목록을 ML에서 조회합니다.")
    @GetMapping("/discoveries")
    public ResponseEntity<Map<String, Object>> getDiscoveries(
            @RequestParam(defaultValue = "5") int limit,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(
                mlClientService.getUserDiscovery(String.valueOf(user.getUserId()), limit));
    }

    // 베이스라인 스냅샷 생성 — ML POST /baseline/recompute (entries 30일치 전달)
    @Operation(summary = "베이스라인 스냅샷 생성", description = "최근 30일 entries를 모아 ML에 재계산 요청.")
    @PostMapping("/baselines/snapshot")
    public ResponseEntity<Map<String, Object>> postBaselineSnapshot(
            @AuthenticationPrincipal User user) {
        Long userId = user.getUserId();
        LocalDate today = LocalDate.now(KST);
        List<com.gdg.backend.entity.Entry> entries = entryRepository
                .findByUser_UserIdAndRecordedDateBetween(userId, today.minusDays(30), today);

        // ML /baseline/recompute 는 entry당 label_result(patterns/emotions/behaviors) 키를 읽음.
        // (label_result_json·context_json 으로 보내면 ML이 못 읽어 baseline이 빈 값으로 나옴)
        List<Map<String, Object>> entryData = entries.stream()
                .map(e -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("created_at", e.getCreatedAt() != null ? e.getCreatedAt().toString() : null);
                    m.put("context_used", e.getContextJson() != null ? e.getContextJson() : Map.of());
                    m.put("label_result", e.getLabelResultJson() != null ? e.getLabelResultJson() : Map.of());
                    return m;
                })
                .toList();

        Map<String, Object> body = new HashMap<>();
        body.put("user_id", String.valueOf(userId));
        body.put("entries", entryData);
        body.put("window_days", 30);
        return ResponseEntity.ok(mlClientService.recomputeBaseline(body));
    }

    // 베이스라인 조회 — ML GET /baseline?user_id=
    @Operation(summary = "베이스라인 조회", description = "ML에서 사용자 베이스라인 스냅샷을 조회합니다.")
    @GetMapping("/baselines/{userId}")
    public ResponseEntity<Map<String, Object>> getBaseline(@PathVariable String userId) {
        return ResponseEntity.ok(mlClientService.getBaseline(userId));
    }

    // 최신 주간 리포트 — ML GET /weekly?user_id=&week=(이번 주)
    @Operation(summary = "최신 주간 리포트", description = "이번 주 주간 리포트를 ML에서 조회합니다.")
    @GetMapping("/weekly/latest")
    public ResponseEntity<Map<String, Object>> getWeeklyLatest(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(
                mlClientService.getWeekly(String.valueOf(user.getUserId()), getCurrentWeekId()));
    }

    // 최신 월간 리포트 — ML GET /monthly?user_id=&month=(이번 달)
    @Operation(summary = "최신 월간 리포트", description = "이번 달 월간 리포트를 ML에서 조회합니다.")
    @GetMapping("/monthly/latest")
    public ResponseEntity<Map<String, Object>> getMonthlyLatest(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(
                mlClientService.getMonthly(String.valueOf(user.getUserId()), getCurrentMonthId()));
    }

    // ── 헬퍼 ──

    private String getCurrentWeekId() {
        LocalDate today = LocalDate.now(KST);
        int week = today.get(java.time.temporal.WeekFields.ISO.weekOfWeekBasedYear());
        return today.getYear() + "-W" + String.format("%02d", week);
    }

    private String getCurrentMonthId() {
        return LocalDate.now(KST)
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM"));
    }
}
