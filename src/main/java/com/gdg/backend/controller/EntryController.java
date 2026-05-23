package com.gdg.backend.controller;

import com.gdg.backend.dto.request.EntryCreateRequest;
import com.gdg.backend.dto.request.FeedbackRequest;
import com.gdg.backend.dto.response.EntryCreateResponse;
import com.gdg.backend.service.EntryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/entries")
@RequiredArgsConstructor
public class EntryController {

    private final EntryService entryService;

    // TODO: 인증 구현 후 @AuthenticationPrincipal로 userId 추출하도록 변경
    @PostMapping
    public ResponseEntity<EntryCreateResponse> createEntry(
            @RequestParam Long userId,
            @RequestBody EntryCreateRequest request) {
        return ResponseEntity.ok(entryService.createEntry(userId, request));
    }

    @PostMapping("/{entryId}/feedback")
    public ResponseEntity<Void> submitFeedback(
            @PathVariable Long entryId,
            @RequestBody FeedbackRequest request) {
        entryService.submitFeedback(entryId, request);
        return ResponseEntity.ok().build();
    }
}
