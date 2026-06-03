package com.gdg.backend.controller;

import com.gdg.backend.dto.request.EntryCreateRequest;
import com.gdg.backend.dto.request.EntryFeedbackRequest;
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

    @Operation(summary = "드릴 피드백 제출", description = "드릴 완료 여부 및 도움 여부를 기록하고 ML 개인화 학습에 반영합니다.")
    @PatchMapping("/{entryId}/feedback")
    public ResponseEntity<Void> submitFeedback(
            @PathVariable Long entryId,
            @RequestBody EntryFeedbackRequest request,
            @AuthenticationPrincipal User user) {
        entryService.submitFeedback(entryId, request, user.getUserId());
        return ResponseEntity.noContent().build();
    }
}
