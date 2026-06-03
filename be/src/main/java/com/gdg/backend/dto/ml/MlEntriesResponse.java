package com.gdg.backend.dto.ml;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MlEntriesResponse {

    private String text;

    @JsonProperty("context_used")
    private Map<String, Object> contextUsed;

    @JsonProperty("label_result")
    private Map<String, Object> labelResult;

    private Map<String, Object> recommendation;

    @JsonProperty("drill_category")
    private String drillCategory;

    @JsonProperty("drill_category_label")
    private String drillCategoryLabel;

    @JsonProperty("drill_calendar_color")
    private String drillCalendarColor;

    @JsonProperty("labeled_at")
    private String labeledAt;
}
