package com.gdg.backend.controller;

import com.gdg.backend.dto.response.DrillTodayResponse;
import com.gdg.backend.service.DrillService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/drills")
@RequiredArgsConstructor
public class DrillController {

    private final DrillService drillService;

    // TODO: 인증 구현 후 @AuthenticationPrincipal로 userId 추출하도록 변경
    @GetMapping("/today")
    public ResponseEntity<DrillTodayResponse> getTodayDrill(@RequestParam Long userId) {
        return ResponseEntity.ok(drillService.getTodayDrill(userId));
    }
}
