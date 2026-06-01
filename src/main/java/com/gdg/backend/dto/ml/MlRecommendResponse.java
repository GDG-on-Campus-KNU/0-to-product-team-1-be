package com.gdg.backend.dto.ml;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MlRecommendResponse {

    // 2 types: drill, crisis_card
    private String type;
    private String reason;

    // type=drill
    private DrillPayload drill;
    private CopyPayload copy;

    // type=crisis_card
    @JsonProperty("crisis_resources")
    private Map<String, String> crisisResources;

    @JsonProperty("user_message")
    private String userMessage;

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DrillPayload {
        private String id;
        private String name;
        private String category;

        @JsonProperty("duration_min")
        private Integer durationMin;

        private String instruction;
        private String citation;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CopyPayload {
        private String line1;
        private String line2;
        private String line3;
    }
}
