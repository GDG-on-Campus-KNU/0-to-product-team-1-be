package com.gdg.backend.controller;

import com.gdg.backend.dto.request.EntryCreateRequest;
import com.gdg.backend.dto.request.FeedbackRequest;
import com.gdg.backend.dto.response.EntryCreateResponse;
import com.gdg.backend.service.EntryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Entry", description = "엔트리(일기) 관련 API")
@RestController
@RequestMapping("/entries")
@RequiredArgsConstructor
public class EntryController {

    private final EntryService entryService;

    // TODO: 인증 구현 후 @AuthenticationPrincipal로 userId 추출하도록 변경
    @Operation(summary = "엔트리 생성", description = "사용자의 일기를 작성하고, ML 분석 결과를 받아 저장합니다.")
    @PostMapping
    public ResponseEntity<EntryCreateResponse> createEntry(
            @RequestParam Long userId,
            @RequestBody EntryCreateRequest request) {
        return ResponseEntity.ok(entryService.createEntry(userId, request));
    }

    @Operation(summary = "드릴 피드백 제출", description = "드릴 완료 후 도움이 되었는지 피드백을 제출합니다.")
    @PostMapping("/{entryId}/feedback")
    public ResponseEntity<Void> submitFeedback(
            @PathVariable Long entryId,
            @RequestBody FeedbackRequest request) {
        entryService.submitFeedback(entryId, request);
        return ResponseEntity.ok().build();
    }
}
