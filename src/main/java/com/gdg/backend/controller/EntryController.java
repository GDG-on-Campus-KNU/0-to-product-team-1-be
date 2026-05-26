package com.gdg.backend.controller;

import com.gdg.backend.dto.request.AskAnswerRequest;
import com.gdg.backend.dto.request.EntryCreateRequest;
import com.gdg.backend.dto.request.FeedbackRequest;
import com.gdg.backend.dto.request.RejectRequest;
import com.gdg.backend.dto.response.EntryCreateResponse;
import com.gdg.backend.entity.User;
import com.gdg.backend.service.EntryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Entry", description = "엔트리(일기) 관련 API")
@RestController
@RequestMapping("/entries")
@RequiredArgsConstructor
public class EntryController {

    private final EntryService entryService;

    @Operation(summary = "엔트리 생성", description = "사용자의 일기를 작성하고, ML 분석 결과를 받아 저장합니다.")
    @PostMapping
    public ResponseEntity<EntryCreateResponse> createEntry(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody EntryCreateRequest request) {
        return ResponseEntity.ok(entryService.createEntry(user.getUserId(), request));
    }

    // 14.2 ask-first 후속 응답
    @Operation(summary = "ask-first 답변 제출", description = "ask_user 응답 후 yes/no 제출 → ML /recommend/after_ask")
    @PostMapping("/{entryId}/ask-answer")
    public ResponseEntity<EntryCreateResponse> submitAskAnswer(
            @PathVariable Long entryId,
            @Valid @RequestBody AskAnswerRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(entryService.submitAskAnswer(entryId, request, user.getUserId()));
    }

    @Operation(summary = "드릴 피드백 제출", description = "드릴 완료 후 도움이 되었는지 피드백을 제출합니다.")
    @PostMapping("/{entryId}/feedback")
    public ResponseEntity<Void> submitFeedback(
            @PathVariable Long entryId,
            @RequestBody FeedbackRequest request) {
        entryService.submitFeedback(entryId, request);
        return ResponseEntity.ok().build();
    }

    // 14.4 드릴 거부 학습 신호
    @Operation(summary = "드릴 거부", description = "추천받은 드릴이 안 맞음 — ML /reject 신호 전달")
    @PostMapping("/reject")
    public ResponseEntity<Void> rejectDrill(
            @Valid @RequestBody RejectRequest request,
            @AuthenticationPrincipal User user) {
        entryService.rejectDrill(request, user.getUserId());
        return ResponseEntity.ok().build();
    }
}
