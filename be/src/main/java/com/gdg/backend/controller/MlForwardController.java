package com.gdg.backend.controller;

import com.gdg.backend.entity.User;
import com.gdg.backend.service.MlClientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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

    private final MlClientService mlClientService;

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

    // ── TODO: ML 서버 연동 후 구현 예정 ──

    @Operation(summary = "[TODO] 나의 발견 저장", description = "ML 서버 연동 후 구현 예정")
    @PostMapping("/discoveries")
    public ResponseEntity<Void> postDiscoveries() {
        return ResponseEntity.status(501).build();
    }

    @Operation(summary = "[TODO] 나의 발견 조회", description = "ML 서버 연동 후 구현 예정")
    @GetMapping("/discoveries")
    public ResponseEntity<Void> getDiscoveries() {
        return ResponseEntity.status(501).build();
    }

    @Operation(summary = "[TODO] 카테고리 목록 조회", description = "ML 서버 연동 후 구현 예정 — 6 카테고리 + 캘린더 색 매핑")
    @GetMapping("/categories")
    public ResponseEntity<Void> getCategories() {
        return ResponseEntity.status(501).build();
    }

    @Operation(summary = "[TODO] 베이스라인 스냅샷 생성", description = "ML 서버 연동 후 구현 예정")
    @PostMapping("/baselines/snapshot")
    public ResponseEntity<Void> postBaselineSnapshot() {
        return ResponseEntity.status(501).build();
    }

    @Operation(summary = "[TODO] 베이스라인 조회", description = "ML 서버 연동 후 구현 예정")
    @GetMapping("/baselines/{userId}")
    public ResponseEntity<Void> getBaseline(@PathVariable String userId) {
        return ResponseEntity.status(501).build();
    }

    @Operation(summary = "[TODO] 퀴즈 답변 제출", description = "ML 서버 연동 후 구현 예정")
    @PostMapping("/quiz/answer")
    public ResponseEntity<Void> postQuizAnswer() {
        return ResponseEntity.status(501).build();
    }

    @Operation(summary = "[TODO] 퀴즈 정답률 조회", description = "ML 서버 연동 후 구현 예정")
    @GetMapping("/quiz/correct-rate")
    public ResponseEntity<Void> getQuizCorrectRate() {
        return ResponseEntity.status(501).build();
    }

    @Operation(summary = "[TODO] 드릴 거부", description = "ML 서버 연동 후 구현 예정 — 거부 학습 신호")
    @PostMapping("/drills/{id}/reject")
    public ResponseEntity<Void> rejectDrill(@PathVariable String id) {
        return ResponseEntity.status(501).build();
    }

    @Operation(summary = "[TODO] 최신 주간 리포트", description = "ML 서버 연동 후 구현 예정")
    @GetMapping("/weekly/latest")
    public ResponseEntity<Void> getWeeklyLatest() {
        return ResponseEntity.status(501).build();
    }

    @Operation(summary = "[TODO] 최신 월간 리포트", description = "ML 서버 연동 후 구현 예정")
    @GetMapping("/monthly/latest")
    public ResponseEntity<Void> getMonthlyLatest() {
        return ResponseEntity.status(501).build();
    }
}
