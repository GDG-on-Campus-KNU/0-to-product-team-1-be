package com.gdg.backend.controller;

import com.gdg.backend.dto.ApiResponse;
import com.gdg.backend.service.MlClientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admin", description = "관리자 전용 API")
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final MlClientService mlClientService;

    @Value("${admin.token:}")
    private String adminToken;

    /**
     * 5.4 시연 직전 quota 리셋
     * user_id 없으면 전체 리셋
     */
    @Operation(summary = "Quota 리셋", description = "시연 전 ML quota 리셋. ADMIN_TOKEN 필수.")
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
}
