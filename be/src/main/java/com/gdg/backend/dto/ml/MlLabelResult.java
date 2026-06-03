package com.gdg.backend.dto.ml;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MlLabelResult {

    private Map<String, Double> patterns;
    private Map<String, Double> behaviors;
    private Map<String, Double> emotions;
    private double intensity;
    private double confidence;

    @JsonProperty("evidence_span")
    private String evidenceSpan;

    @JsonProperty("crisis_detected")
    private boolean crisisDetected;

    @JsonProperty("calendar_dominant")
    private String calendarDominant;

    @JsonProperty("model_used")
    private String modelUsed;

    @JsonProperty("labeled_at")
    private String labeledAt;

    @JsonProperty("clarified_winner")
    private String clarifiedWinner;

    @JsonProperty("confidence_raw")
    private Double confidenceRaw;

    @JsonProperty("confidence_adjusted_by")
    private String confidenceAdjustedBy;
}
