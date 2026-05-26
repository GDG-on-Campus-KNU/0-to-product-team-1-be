package com.gdg.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gdg.backend.dto.ml.MlAfterAskRequest;
import com.gdg.backend.dto.ml.MlLabelResult;
import com.gdg.backend.dto.ml.MlRecommendRequest;
import com.gdg.backend.dto.ml.MlRecommendResponse;
import com.gdg.backend.dto.request.AskAnswerRequest;
import com.gdg.backend.dto.request.EntryCreateRequest;
import com.gdg.backend.dto.request.FeedbackRequest;
import com.gdg.backend.dto.request.RejectRequest;
import com.gdg.backend.dto.response.EntryCreateResponse;
import com.gdg.backend.entity.Entry;
import com.gdg.backend.entity.RejectedDrill;
import com.gdg.backend.entity.User;
import com.gdg.backend.repository.EntryRepository;
import com.gdg.backend.repository.RejectedDrillRepository;
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
    private final ContextCacheService contextCacheService;
    private final RejectedDrillRepository rejectedDrillRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    @SuppressWarnings("unchecked")
    public EntryCreateResponse createEntry(Long userId, EntryCreateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

        // 4.2 캐시에서 누락 컨텍스트 필드 채움 (4.3 self_condition은 캐시 X)
        Map<String, Object> filledContext = contextCacheService.fillContext(userId, request.getContext());

        // 4.1 오늘 첫 입력이면 sleep/social/exercise Redis에 저장 (TTL = KST 자정)
        contextCacheService.storeContext(userId, filledContext);

        Entry entry = entryRepository.save(Entry.of(user, request));

        // 1. ML /label 호출
        MlLabelResult labelResult = mlClientService.label(request.getText(), userId);

        // 2. 최근 드릴 ID 조회 (추천 다양성)
        List<Integer> recentDrillIds = entryRepository.findRecentDrillIdsByUserId(
                userId, PageRequest.of(0, 3));

        // 3. ML /recommend 호출
        MlRecommendRequest recommendRequest = MlRecommendRequest.builder()
                .labelResult(labelResult)
                .context(filledContext)
                .userId(String.valueOf(userId))
                .recentDrillIds(recentDrillIds)
                .build();

        MlRecommendResponse recommendResponse = mlClientService.recommend(recommendRequest);

        // 4. Entry에 ML 결과 저장
        Map<String, Object> llmResult = objectMapper.convertValue(labelResult, Map.class);
        entry.setLlmResult(llmResult);

        // 6.5 type별 분기 — crisis_card는 절대 드릴 라우팅 X
        String type = recommendResponse.getType();
        if ("drill".equals(type) && recommendResponse.getDrill() != null
                && recommendResponse.getDrill().getId() != null) {
            String drillIdStr = recommendResponse.getDrill().getId();
            entry.setDrillId(Integer.parseInt(drillIdStr.replaceAll("[^0-9]", "")));
        }
        // crisis_card: 6.1 응답 그대로 FE 전달 (변형 X), 6.3 llm_result만 저장 (hash는 ML 처리)

        entryRepository.save(entry);

        // 5. 추천 결과 포함하여 응답
        return EntryCreateResponse.from(entry, recommendResponse);
    }

    // 14.2 ask-first 후속 처리
    @Transactional
    public EntryCreateResponse submitAskAnswer(Long entryId, AskAnswerRequest request, Long userId) {
        Entry entry = entryRepository.findById(entryId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 엔트리입니다."));

        if ("no".equals(request.getChoice())) {
            return EntryCreateResponse.from(entry);
        }

        // yes → ML /recommend/after_ask 호출
        MlAfterAskRequest mlReq = MlAfterAskRequest.builder()
                .userId(String.valueOf(userId))
                .userChoice("yes")
                .offerCategory((String) entry.getLlmResult().getOrDefault("offer_category", null))
                .labelResult(entry.getLlmResult())
                .build();

        MlRecommendResponse mlRes = mlClientService.callAfterAsk(mlReq);

        if ("drill".equals(mlRes.getType()) && mlRes.getDrill() != null && mlRes.getDrill().getId() != null) {
            entry.setDrillId(Integer.parseInt(mlRes.getDrill().getId().replaceAll("[^0-9]", "")));
        }
        entryRepository.save(entry);
        return EntryCreateResponse.from(entry, mlRes);
    }

    // 14.4 드릴 거부 학습 신호
    @Transactional
    public void rejectDrill(RejectRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));
        RejectedDrill rejected = RejectedDrill.builder()
                .user(user)
                .drillId(request.getDrillId())
                .build();
        rejectedDrillRepository.save(rejected);
    }

    @Transactional
    public void submitFeedback(Long entryId, FeedbackRequest request) {
        Entry entry = entryRepository.findById(entryId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 엔트리입니다."));

        entry.setHelpful(request.getHelpful());
        entry.setFeedbackAt(LocalDateTime.now());
    }
}
