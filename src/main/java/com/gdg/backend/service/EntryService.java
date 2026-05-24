package com.gdg.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gdg.backend.dto.ml.MlLabelResult;
import com.gdg.backend.dto.ml.MlRecommendRequest;
import com.gdg.backend.dto.ml.MlRecommendResponse;
import com.gdg.backend.dto.request.EntryCreateRequest;
import com.gdg.backend.dto.request.FeedbackRequest;
import com.gdg.backend.dto.response.EntryCreateResponse;
import com.gdg.backend.entity.Entry;
import com.gdg.backend.entity.User;
import com.gdg.backend.repository.EntryRepository;
import com.gdg.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EntryService {

    private final EntryRepository entryRepository;
    private final UserRepository userRepository;
    private final MlClientService mlClientService;
    private final ObjectMapper objectMapper;

    @Transactional
    @SuppressWarnings("unchecked")
    public EntryCreateResponse createEntry(Long userId, EntryCreateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

        Entry entry = entryRepository.save(Entry.of(user, request));

        // 1. ML /label 호출
        MlLabelResult labelResult = mlClientService.label(request.getText(), userId);

        // 2. 최근 드릴 ID 조회 (추천 다양성)
        List<Integer> recentDrillIds = entryRepository.findRecentDrillIdsByUserId(
                userId, PageRequest.of(0, 3));

        // 3. ML /recommend 호출
        MlRecommendRequest recommendRequest = MlRecommendRequest.builder()
                .labelResult(labelResult)
                .context(request.getContext())
                .userId(String.valueOf(userId))
                .recentDrillIds(recentDrillIds)
                .build();

        MlRecommendResponse recommendResponse = mlClientService.recommend(recommendRequest);

        // 4. Entry에 ML 결과 저장
        Map<String, Object> llmResult = objectMapper.convertValue(labelResult, Map.class);
        entry.setLlmResult(llmResult);

        if (recommendResponse.getDrill() != null && recommendResponse.getDrill().getId() != null) {
            String drillIdStr = recommendResponse.getDrill().getId();
            // ML returns "D14" format — extract numeric part
            entry.setDrillId(Integer.parseInt(drillIdStr.replaceAll("[^0-9]", "")));
        }

        entryRepository.save(entry);

        // 5. 추천 결과 포함하여 응답
        return EntryCreateResponse.from(entry, recommendResponse);
    }

    @Transactional
    public void submitFeedback(Long entryId, FeedbackRequest request) {
        Entry entry = entryRepository.findById(entryId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 엔트리입니다."));

        entry.setHelpful(request.getHelpful());
        entry.setFeedbackAt(LocalDateTime.now());
    }
}
