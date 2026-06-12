package com.gdg.backend.controller;

import com.gdg.backend.dto.ApiResponse;
import com.gdg.backend.scheduler.BaselineUpdateScheduler;
import com.gdg.backend.service.MlClientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admin", description = "관리자 전용 API — 팀 협의 필요")
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final MlClientService mlClientService;
    private final BaselineUpdateScheduler baselineUpdateScheduler;

    @Value("${admin.token:}")
    private String adminToken;

    /**
     * 5.4 시연 직전 quota 리셋
     * user_id 없으면 전체 리셋
     */
    @Operation(summary = "Quota 리셋", description = "시연 전 ML quota 리셋. ADMIN_TOKEN 필수. [협의 필요] FE 호출 여부 및 사용 방식 미정")
    @PostMapping("/quota/reset")
    public ResponseEntity<ApiResponse<?>> resetQuota(
            @RequestHeader("X-Admin-Token") String token,
            @RequestParam(required = false) String userId) {

        if (!adminToken.equals(token)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.fail("FORBIDDEN", "유효하지 않은 관리자 토큰입니다."));
        }

        mlClientService.resetQuota(userId, adminToken);
        return ResponseEntity.ok(ApiResponse.success("quota 리셋 완료"));
    }

    /**
     * 14.16 baseline 갱신 수동 트리거 (cron과 동일 로직)
     */
    @Operation(summary = "Baseline 갱신 수동 실행", description = "전체 유저 최근 30일 entries로 ML baseline 즉시 재계산. ADMIN_TOKEN 필수.")
    @PostMapping("/baseline/trigger")
    public ResponseEntity<ApiResponse<?>> triggerBaselineUpdate(
            @RequestHeader("X-Admin-Token") String token) {

        if (!adminToken.equals(token)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.fail("FORBIDDEN", "유효하지 않은 관리자 토큰입니다."));
        }

        baselineUpdateScheduler.updateBaselines();
        return ResponseEntity.ok(ApiResponse.success("baseline 갱신 완료"));
    }
}
