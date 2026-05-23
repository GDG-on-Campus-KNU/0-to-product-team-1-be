package com.gdg.backend.controller;

import com.gdg.backend.dto.response.DrillTodayResponse;
import com.gdg.backend.service.DrillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Drill", description = "드릴 관련 API")
@RestController
@RequestMapping("/drills")
@RequiredArgsConstructor
public class DrillController {

    private final DrillService drillService;

    // TODO: 인증 구현 후 @AuthenticationPrincipal로 userId 추출하도록 변경
    @Operation(summary = "오늘의 드릴 조회", description = "오늘 드릴 완료 여부를 확인하고, 완료된 경우 드릴 결과를 반환합니다.")
    @GetMapping("/today")
    public ResponseEntity<DrillTodayResponse> getTodayDrill(@RequestParam Long userId) {
        return ResponseEntity.ok(drillService.getTodayDrill(userId));
    }
}
