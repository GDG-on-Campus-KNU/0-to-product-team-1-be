package com.gdg.backend.dto.ml;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class MlAfterAskRequest {

    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("user_choice")
    private String userChoice;

    @JsonProperty("offer_category")
    private String offerCategory;

    @JsonProperty("offer_reason_type")
    private String offerReasonType;

    @JsonProperty("label_result")
    private Map<String, Object> labelResult;
}
