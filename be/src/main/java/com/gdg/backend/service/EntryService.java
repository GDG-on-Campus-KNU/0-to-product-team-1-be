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
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class EntryService {

    private final EntryRepository entryRepository;
    private final UserRepository userRepository;
    private final MlClientService mlClientService;

    private static final Set<String> DEMO_EMAILS = Set.of(
            "asdf@asdf", "mock@mock.com", "mock2@mock.com", "mock3@mock.com", "mock4@mock.com", "mock5@mock.com"
    );

    private static final List<MlEntriesResponse> MOCK_RESPONSES = Arrays.asList(
            mockRes(3,  "cognitive_restructuring", "orange_warm",
                    "증거 찾기", "부정적인 생각을 지지하는 증거와 반박하는 증거를 각각 2가지씩 적어보세요.", "Burns(1980) 인지재구성", 7,
                    em(0.1,0.5,0.2,0.1,0.1), pt(0.0,0.1,0.1,0.6,0.1,0.1)),
            mockRes(60, "grounding",               "sky_blue",
                    "닻 내리기", "지금 앉아있는 의자에 등을 기대고 발이 바닥에 닿는 느낌에 집중해보세요.", "Grounding 기법", 3,
                    em(0.0,0.1,0.1,0.0,0.8), pt(0.6,0.1,0.1,0.1,0.0,0.1)),
            mockRes(55, "habit_design",             "pink_warm",
                    "저축 마음 습관", "오늘 하루 한 가지 작은 절제를 실천하고 무엇을 아꼈는지 기록해보세요.", "Duhigg(2012) 습관의 힘", 3,
                    em(0.1,0.2,0.5,0.1,0.1), pt(0.0,0.1,0.6,0.1,0.1,0.1)),
            mockRes(77, "sleep_circadian",          "blue_night",
                    "수면 루틴 점검", "오늘 잠들기 1시간 전 습관을 적고, 수면에 방해되는 것 하나를 개선해보세요.", "Walker(2017) 수면의 과학", 5,
                    em(0.1,0.5,0.2,0.1,0.1), pt(0.1,0.5,0.1,0.1,0.1,0.1)),
            mockRes(75, "self_compassion",          "lavender",
                    "자기 위로 편지", "힘들어하는 친한 친구에게 하듯, 지금의 나에게 위로 한 마디를 적어보세요.", "Neff(2003) 자기자비", 5,
                    em(0.0,0.1,0.1,0.0,0.8), pt(0.0,0.0,0.1,0.1,0.6,0.2))
    );

    private static Map<String, Object> em(double 분노, double 불안, double 우울, double 죄책, double 중립) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("분노", 분노); m.put("불안", 불안); m.put("우울", 우울); m.put("죄책", 죄책); m.put("중립", 중립);
        return m;
    }

    private static Map<String, Object> pt(double 독심술, double 이분법, double 당위진술, double 미래예측, double 자기비난, double 과잉일반화) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("독심술", 독심술); m.put("이분법", 이분법); m.put("당위진술", 당위진술);
        m.put("미래예측", 미래예측); m.put("자기비난", 자기비난); m.put("과잉일반화", 과잉일반화);
        return m;
    }

    private static MlEntriesResponse mockRes(int drillId, String category, String color,
                                             String name, String instruction, String citation, int durationMin,
                                             Map<String, Object> emotions, Map<String, Object> patterns) {
        Map<String, Object> label = new java.util.HashMap<>();
        label.put("emotions", emotions);
        label.put("patterns", patterns);

        Map<String, Object> drill = new java.util.HashMap<>();
        drill.put("id", drillId);
        drill.put("name", name);
        drill.put("category", category);
        drill.put("citation", citation);
        drill.put("instruction", instruction);
        drill.put("duration_min", durationMin);

        Map<String, Object> copy = new java.util.HashMap<>();
        copy.put("line1", "오늘의 드릴: " + name);
        copy.put("line2", instruction);
        copy.put("line3", "예상 소요 시간: " + durationMin + "분");

        Map<String, Object> why = new java.util.HashMap<>();
        why.put("text", "지금 상태에서 도움이 될 것 같아요.");
        why.put("tone", "supportive");
        why.put("factors", new java.util.ArrayList<>());
        why.put("mechanism", instruction);
        why.put("expected_benefit", "감정 조절과 패턴 인식에 도움이 됩니다.");

        Map<String, Object> rec = new java.util.HashMap<>();
        rec.put("type", "drill");
        rec.put("drill", drill);
        rec.put("copy", copy);
        rec.put("why", why);
        rec.put("tone", "warm");
        rec.put("reason", name + " 드릴이 현재 상태에 도움이 될 것 같아요.");

        return new MlEntriesResponse(null, null, label, rec, category, null, color, null);
    }

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
        MlEntriesResponse mlRes = callMlEntries(request, userId, user.getEmail());
        applyMlResult(entry, mlRes);

        entryRepository.save(entry);
        return EntryCreateResponse.from(entry);
    }

    private MlEntriesResponse callMlEntries(EntryCreateRequest request, Long userId, String userEmail) {
        if (!DEMO_EMAILS.contains(userEmail)) {
            return MOCK_RESPONSES.get((int)(userId % MOCK_RESPONSES.size()));
        }
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
