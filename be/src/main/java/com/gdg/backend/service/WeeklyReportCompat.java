package com.gdg.backend.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 주간 리포트 응답 호환 변환기.
 *
 * <p>ML(/weekly)이 돌려주는 실제 응답 구조와 FE가 기대하는 구조가 다르다.
 * FE는 초기 Mock 데이터 구조(block1_action_rate 등)에 맞춰 구현되어 있고,
 * ML 실제 응답은 drill_action / dominant_pattern / blocksJson.weekly_coaching 형태다.
 * 이 클래스는 조회 응답을 내보낼 때만 FE 구조로 번역한다. DB 원본은 건드리지 않는다.
 *
 * <p>이미 FE 구조(Mock으로 생성된 리포트)면 그대로 통과시키고,
 * 필드가 비어있는 과거 깨진 리포트도 기본값으로 채워 화면이 죽지 않게 방어한다.
 */
public final class WeeklyReportCompat {

    private WeeklyReportCompat() {
    }

    /** blocksJson을 FE가 기대하는 block1/2/3 구조로 변환한 새 Map을 반환. */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> toFeBlocks(Map<String, Object> blocks,
                                                 Map<String, Object> visualizations) {
        Map<String, Object> src = blocks != null ? blocks : Map.of();
        Map<String, Object> viz = visualizations != null ? visualizations : Map.of();

        // 이미 FE 구조면(Mock 생성분) 그대로 통과
        if (src.containsKey("block1_action_rate")
                && src.containsKey("block2_emotion_summary")
                && src.containsKey("block3_weekly_tip")) {
            return new HashMap<>(src);
        }

        Map<String, Object> result = new HashMap<>();

        // block1_action_rate ← drill_action.practice_rate(0~1) × 100
        Map<String, Object> drillAction = asMap(src.get("drill_action"));
        double practiceRate = asDouble(drillAction.get("practice_rate"), 0.0);
        int practicedCount = (int) asDouble(drillAction.get("practiced_count"), 0.0);
        int recommendedCount = (int) asDouble(drillAction.get("recommended_count"), 0.0);
        Map<String, Object> block1 = new HashMap<>();
        block1.put("rate", round1(practiceRate * 100.0));
        block1.put("description", recommendedCount > 0
                ? "추천 드릴 " + recommendedCount + "개 중 " + practicedCount + "개를 실천했어요."
                : "이번 주 드릴 수행률입니다.");
        result.put("block1_action_rate", block1);

        // block2_emotion_summary ← emotion_pentagon.dominant
        Map<String, Object> pentagon = asMap(viz.get("emotion_pentagon"));
        String dominant = asString(pentagon.get("dominant"), "중립");
        Map<String, Object> block2 = new HashMap<>();
        block2.put("primary_emotion", dominant);
        block2.put("description", "이번 주 가장 많이 나타난 감정입니다.");
        result.put("block2_emotion_summary", block2);

        // block3_weekly_tip ← weekly_coaching.next_week_focus.reason
        Map<String, Object> coaching = asMap(src.get("weekly_coaching"));
        Map<String, Object> nextFocus = asMap(coaching.get("next_week_focus"));
        Map<String, Object> block3 = new HashMap<>();
        block3.put("tip", asString(nextFocus.get("reason"), "꾸준히 기록을 이어가 보세요."));
        block3.put("source", asString(nextFocus.get("source"), "CBT 기반 권고"));
        result.put("block3_weekly_tip", block3);

        if (src.containsKey("self_check_quiz")) {
            result.put("self_check_quiz", src.get("self_check_quiz"));
        }

        return result;
    }

    /** visualizationsJson에 weekly_coaching을 채워(없으면 blocksJson에서 이동) FE 구조로 변환한 새 Map을 반환. */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> toFeVisualizations(Map<String, Object> blocks,
                                                         Map<String, Object> visualizations) {
        Map<String, Object> src = blocks != null ? blocks : Map.of();
        Map<String, Object> result = new HashMap<>(visualizations != null ? visualizations : Map.of());

        // FE가 반드시 읽는 배열/객체 필드 방어 (없으면 빈 값)
        result.putIfAbsent("pattern_diff", new ArrayList<>());
        result.putIfAbsent("discoveries", new ArrayList<>());
        result.putIfAbsent("condition_flow", Map.of("points", new ArrayList<>()));
        result.putIfAbsent("emotion_pentagon", defaultPentagon());

        // weekly_coaching: visualizations에 없으면 blocksJson에서 가져와 이동
        if (!result.containsKey("weekly_coaching")) {
            Map<String, Object> coaching = asMap(src.get("weekly_coaching"));
            result.put("weekly_coaching", coaching.isEmpty() ? defaultCoaching() : new HashMap<>(coaching));
        }

        return result;
    }

    private static Map<String, Object> defaultPentagon() {
        List<Map<String, Object>> axes = new ArrayList<>();
        for (String label : List.of("불안", "우울", "분노", "죄책", "중립")) {
            Map<String, Object> axis = new HashMap<>();
            axis.put("label", label);
            axis.put("value", "중립".equals(label) ? 1.0 : 0.0);
            axes.add(axis);
        }
        Map<String, Object> pentagon = new HashMap<>();
        pentagon.put("axes", axes);
        pentagon.put("dominant", "중립");
        pentagon.put("entries_used", 0);
        return pentagon;
    }

    private static Map<String, Object> defaultCoaching() {
        Map<String, Object> state = new HashMap<>();
        state.put("key", "observing");
        state.put("label", "관찰기");
        state.put("summary", "아직 한 주를 읽기엔 기록이 적어요. 며칠 더 모이면 흐름이 보여요.");
        Map<String, Object> nextFocus = new HashMap<>();
        nextFocus.put("category", "grounding");
        nextFocus.put("label_ko", "마음 챙김");
        nextFocus.put("reason", "가볍게 마음을 살피는 연습으로 시작해요.");
        Map<String, Object> coaching = new HashMap<>();
        coaching.put("state", state);
        coaching.put("next_week_focus", nextFocus);
        return coaching;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> asMap(Object o) {
        return o instanceof Map ? (Map<String, Object>) o : Map.of();
    }

    private static double asDouble(Object o, double fallback) {
        return o instanceof Number ? ((Number) o).doubleValue() : fallback;
    }

    private static String asString(Object o, String fallback) {
        return o instanceof String ? (String) o : fallback;
    }

    private static double round1(double v) {
        return Math.round(v * 10.0) / 10.0;
    }
}
