package com.gdg.backend.dto.ml;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@Builder
public class MlRecommendRequest {

    @JsonProperty("label_result")
    private MlLabelResult labelResult;

    private Map<String, Object> context;

    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("recent_drill_ids")
    private List<Integer> recentDrillIds;
}
