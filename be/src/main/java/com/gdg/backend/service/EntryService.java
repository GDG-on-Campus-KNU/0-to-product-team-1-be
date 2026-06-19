package com.gdg.backend.service;

import com.gdg.backend.dto.ml.MlEntriesRequest;
import com.gdg.backend.dto.ml.MlEntriesResponse;
import com.gdg.backend.dto.request.EntryCreateRequest;
import com.gdg.backend.dto.request.EntryFeedbackRequest;
import com.gdg.backend.dto.response.EntryCreateResponse;
import com.gdg.backend.entity.Entry;
import com.gdg.backend.entity.User;
import com.gdg.backend.exception.DuplicateEntryException;
import com.gdg.backend.repository.EntryRepository;
import com.gdg.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class EntryService {

    private final EntryRepository entryRepository;
    private final UserRepository userRepository;
    private final MlClientService mlClientService;

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

        Entry entry = Entry.of(user, request.getText(), today);
        entry.setContextJson(request.getContext());

        // ML 호출 + 결과 반영
        MlEntriesResponse mlRes = callMlEntries(request, userId);
        applyMlResult(entry, mlRes);

        entryRepository.save(entry);
        return EntryCreateResponse.from(entry);
    }

    private MlEntriesResponse callMlEntries(EntryCreateRequest request, Long userId) {
        List<Integer> recentDrillIds = entryRepository.findRecentDrillIdsByUserId(
                userId, PageRequest.of(0, 3));

        MlEntriesRequest mlReq = MlEntriesRequest.of(
                request.getText(), String.valueOf(userId), request.getContext(), recentDrillIds);

        return mlClientService.callEntries(mlReq);
    }

    @SuppressWarnings("unchecked")
    private void applyMlResult(Entry entry, MlEntriesResponse mlRes) {
        entry.setLabelResultJson(mlRes.getLabelResult());
        entry.setRecommendationJson(mlRes.getRecommendation());
        entry.setDrillCategory(mlRes.getDrillCategory());
        entry.setDrillCalendarColor(mlRes.getDrillCalendarColor());

        Map<String, Object> rec = mlRes.getRecommendation();
        String type = rec != null ? (String) rec.get("type") : null;
        if (type != null) {
            switch (type) {
                case "drill" -> {
                    Map<String, Object> drill = (Map<String, Object>) rec.get("drill");
                    if (drill != null && (drill.get("id") != null || drill.get("drill_id") != null)) {
                        Object rawId = drill.get("id") != null ? drill.get("id") : drill.get("drill_id");
                        if (rawId instanceof Integer i) entry.setDrillId(i);
                        else if (rawId instanceof String s) entry.setDrillId(Integer.parseInt(s.replaceAll("[^0-9]", "")));
                        else if (rawId instanceof Number n) entry.setDrillId(n.intValue());
                    }
                }
                case "crisis_card" -> entry.setCrisisFlag(true);
            }
        }
    }

    @Transactional
    public void submitFeedback(String entryId, EntryFeedbackRequest request, Long userId) {
        Entry entry = resolveFeedbackEntry(entryId, userId);

        if (request.getDrillCompleted() != null) entry.setDrillCompleted(request.getDrillCompleted());
        if (request.getHelpful() != null) entry.setHelpful(request.getHelpful());
        entryRepository.save(entry);

        if (entry.getDrillId() != null) {
            try {
                mlClientService.postFeedback(Map.of(
                        "user_id", String.valueOf(userId),
                        "drill_id", entry.getDrillId(),
                        "completed", request.getDrillCompleted() != null && request.getDrillCompleted(),
                        "helpful", request.getHelpful() != null && request.getHelpful()
                ));
            } catch (Exception e) {
                log.warn("ML 피드백 전달 실패 (무시): entryId={}, error={}", entryId, e.getMessage());
            }
        }
    }

    /**
     * entryId가 유효한 숫자면 해당 엔트리를, 아니면(없음·"undefined" 등) 호출 사용자의 당일 기록을 반환.
     * FE가 드릴 받은 직후 entryId 없이 피드백을 보내는 경우 대비 (일 1회 정책상 당일 기록은 최대 1건).
     */
    private Entry resolveFeedbackEntry(String entryId, Long userId) {
        Long parsedId = tryParseLong(entryId);
        if (parsedId != null) {
            return entryRepository.findById(parsedId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 엔트리입니다."));
        }
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        return entryRepository.findFirstByUser_UserIdAndRecordedDate(userId, today)
                .orElseThrow(() -> new IllegalArgumentException("오늘 작성한 기록이 없습니다."));
    }

    private Long tryParseLong(String value) {
        if (value == null) return null;
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // 14.4 드릴 거부는 MlForwardController POST /drills/{id}/reject → ML /reject로 처리됨
    // (ML insights_store가 데이터 주인 — BE 별도 저장 불필요)
}
