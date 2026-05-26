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
    }
}
