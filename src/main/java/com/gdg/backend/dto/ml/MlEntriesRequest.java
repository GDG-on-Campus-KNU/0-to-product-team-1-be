package com.gdg.backend.dto.ml;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MlEntriesRequest {

    private String text;

    @JsonProperty("user_id")
    private String userId;

    private MlEntriesContext context;

    @JsonProperty("recent_drill_ids")
    private List<Integer> recentDrillIds;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MlEntriesContext {

        @JsonProperty("self_condition")
        private Integer selfCondition;

        @JsonProperty("sleep_hours")
        private Double sleepHours;

        @JsonProperty("social_today")
        private String socialToday;

        @JsonProperty("exercise_today")
        private Double exerciseToday;

        public static MlEntriesContext from(java.util.Map<String, Object> context) {
            return MlEntriesContext.builder()
                    .selfCondition((Integer) context.get("self_condition"))
                    .sleepHours(toDouble(context.get("sleep_hours")))
                    .socialToday((String) context.get("social_today"))
                    .exerciseToday(toDouble(context.get("exercise_today")))
                    .build();
        }

        private static Double toDouble(Object value) {
            if (value == null) return null;
            if (value instanceof Double d) return d;
            if (value instanceof Number n) return n.doubleValue();
            return null;
        }
    }

    public static MlEntriesRequest of(String text, String userId, java.util.Map<String, Object> context, List<Integer> recentDrillIds) {
        return MlEntriesRequest.builder()
                .text(text)
                .userId(userId)
                .context(MlEntriesContext.from(context))
                .recentDrillIds(recentDrillIds)
                .build();
    }
}
