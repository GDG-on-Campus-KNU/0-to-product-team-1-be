package com.gdg.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gdg.backend.dto.ml.MlAfterAskRequest;
import com.gdg.backend.dto.ml.MlEntriesRequest;
import com.gdg.backend.dto.ml.MlEntriesResponse;
import com.gdg.backend.dto.ml.MlRecommendResponse;
import com.gdg.backend.dto.request.AskAnswerRequest;
import com.gdg.backend.dto.request.EntryCreateRequest;
import com.gdg.backend.dto.request.FeedbackRequest;
import com.gdg.backend.dto.request.RejectRequest;
import com.gdg.backend.dto.response.EntryCreateResponse;
import com.gdg.backend.entity.Entry;
import com.gdg.backend.entity.RejectedDrill;
import com.gdg.backend.entity.User;
import com.gdg.backend.exception.DuplicateEntryException;
import com.gdg.backend.repository.EntryRepository;
import com.gdg.backend.repository.RejectedDrillRepository;
import com.gdg.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
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

        // 일 1회 정책 — 오늘 이미 입력했으면 409
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        if (entryRepository.existsByUser_UserIdAndRecordedDate(userId, today)) {
            throw new DuplicateEntryException("오늘은 이미 입력했습니다.");
        }

        // 캐시에서 누락 컨텍스트 필드 채움 (self_condition은 캐시 X)
        Map<String, Object> filledContext = contextCacheService.fillContext(userId, request.getContext());
        contextCacheService.storeContext(userId, filledContext);

        Entry entry = entryRepository.save(Entry.of(user, request));
        entry.setRecordedDate(today);

        // 최근 드릴 ID 조회 (추천 다양성)
        List<Integer> recentDrillIds = entryRepository.findRecentDrillIdsByUserId(
                userId, PageRequest.of(0, 3));

        // ML POST /entries 통합 호출 (label + recommend 한 번에)
        MlEntriesRequest mlReq = MlEntriesRequest.builder()
                .text(request.getText())
                .userId(String.valueOf(userId))
                .context(MlEntriesRequest.MlEntriesContext.builder()
                        .selfCondition((Integer) filledContext.get("self_condition"))
                        .sleepHours(toDouble(filledContext.get("sleep_hours")))
                        .socialToday((String) filledContext.get("social_today"))
                        .exerciseToday(toDouble(filledContext.get("exercise_today")))
                        .build())
                .recentDrillIds(recentDrillIds)
                .build();

        MlEntriesResponse mlRes = mlClientService.callEntries(mlReq);

        // Entry에 ML 결과 저장
        entry.setLlmResult(mlRes.getLabelResult());
        entry.setRecommendationJson(mlRes.getRecommendation());
        entry.setDrillCategory(mlRes.getDrillCategory());
        entry.setDrillCalendarColor(mlRes.getDrillCalendarColor());

        // type별 분기
        Map<String, Object> rec = mlRes.getRecommendation();
        String type = rec != null ? (String) rec.get("type") : null;
        if (type != null) {
            switch (type) {
                case "drill" -> {
                    Map<String, Object> drill = (Map<String, Object>) rec.get("drill");
                    if (drill != null && drill.get("drill_id") != null) {
                        Object rawId = drill.get("drill_id");
                        if (rawId instanceof Integer i) entry.setDrillId(i);
                        else if (rawId instanceof String s) entry.setDrillId(Integer.parseInt(s.replaceAll("[^0-9]", "")));
                        else if (rawId instanceof Number n) entry.setDrillId(n.intValue());
                    }
                }
                case "crisis_card" -> entry.setCrisisFlag(true);
                case "ask_user" -> {
                    entry.setOfferedCategory((String) rec.get("offer_category"));
                    entry.setAwaitingAnswer(true);
                }
            }
        }

        entryRepository.save(entry);
        return EntryCreateResponse.from(entry);
    }

    private Double toDouble(Object value) {
        if (value == null) return null;
        if (value instanceof Double d) return d;
        if (value instanceof Number n) return n.doubleValue();
        return null;
    }

    // ask-first 후속 처리
    @Transactional
    public EntryCreateResponse submitAskAnswer(Long entryId, AskAnswerRequest request, Long userId) {
        Entry entry = entryRepository.findById(entryId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 엔트리입니다."));

        if ("no".equals(request.getChoice())) {
            entry.setAwaitingAnswer(false);
            entry.setDrillCategory(null);
            entryRepository.save(entry);
            return EntryCreateResponse.from(entry);
        }

        // yes → ML /recommend/after_ask 호출
        MlAfterAskRequest mlReq = MlAfterAskRequest.builder()
                .userId(String.valueOf(userId))
                .userChoice("yes")
                .offerCategory(entry.getOfferedCategory())
                .labelResult(entry.getLlmResult())
                .build();

        MlRecommendResponse mlRes = mlClientService.callAfterAsk(mlReq);

        entry.setAwaitingAnswer(false);
        if ("drill".equals(mlRes.getType()) && mlRes.getDrill() != null && mlRes.getDrill().getId() != null) {
            entry.setDrillId(Integer.parseInt(mlRes.getDrill().getId().replaceAll("[^0-9]", "")));
            entry.setDrillCategory(mlRes.getDrill().getCategory());
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
