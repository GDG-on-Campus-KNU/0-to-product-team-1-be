package com.gdg.backend.service;

import com.gdg.backend.dto.request.EntryCreateRequest;
import com.gdg.backend.dto.request.FeedbackRequest;
import com.gdg.backend.dto.response.EntryCreateResponse;
import com.gdg.backend.entity.Entry;
import com.gdg.backend.entity.User;
import com.gdg.backend.repository.EntryRepository;
import com.gdg.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class EntryService {

    private final EntryRepository entryRepository;
    private final UserRepository userRepository;

    @Transactional
    public EntryCreateResponse createEntry(Long userId, EntryCreateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

        Entry savedEntry = entryRepository.save(Entry.of(user, request));

        // TODO: ML 서버 호출 (POST /analyze) → llm_result, drill_id 받아서 Entry 업데이트

        return EntryCreateResponse.from(savedEntry);
    }

    @Transactional
    public void submitFeedback(Long entryId, FeedbackRequest request) {
        Entry entry = entryRepository.findById(entryId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 엔트리입니다."));

        entry.setHelpful(request.getHelpful());
        entry.setFeedbackAt(LocalDateTime.now());
    }
}
