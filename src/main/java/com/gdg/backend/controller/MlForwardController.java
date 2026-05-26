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
@Tag(name = "ML Forward", description = "ML 서비스 포워딩 API")
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

    // 14.14 GET /export
    @Operation(summary = "사용자 데이터 내보내기")
    @GetMapping("/export")
    public ResponseEntity<Map<String, Object>> exportUserData(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(mlClientService.exportUserData(String.valueOf(user.getUserId())));
    }
}
