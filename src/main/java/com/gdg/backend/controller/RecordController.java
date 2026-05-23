package com.gdg.backend.controller;

import com.gdg.backend.dto.response.CalendarResponse;
import com.gdg.backend.dto.response.DailyRecordResponse;
import com.gdg.backend.service.RecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/records")
@RequiredArgsConstructor
public class RecordController {

    private final RecordService recordService;

    // TODO: 인증 구현 후 @AuthenticationPrincipal로 userId 추출하도록 변경
    @GetMapping("/calendar")
    public ResponseEntity<CalendarResponse> getCalendar(
            @RequestParam Long userId,
            @RequestParam int year,
            @RequestParam int month) {
        return ResponseEntity.ok(recordService.getCalendar(userId, year, month));
    }

    // TODO: 인증 구현 후 @AuthenticationPrincipal로 userId 추출하도록 변경
    @GetMapping("/daily/{date}")
    public ResponseEntity<DailyRecordResponse> getDailyRecord(
            @RequestParam Long userId,
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        return ResponseEntity.ok(recordService.getDailyRecord(userId, date));
    }
}
