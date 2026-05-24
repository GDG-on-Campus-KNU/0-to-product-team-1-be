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

    // 5 types: drill, crisis_card, positive_card, ask_user, skip
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

    // type=positive_card, skip
    private String message;

    // type=ask_user (v9.4.4)
    private String question;
    private List<Map<String, String>> options;

    @JsonProperty("offer_category")
    private String offerCategory;

    @JsonProperty("offer_reason_type")
    private String offerReasonType;

    @JsonProperty("offer_context_value")
    private Double offerContextValue;

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
