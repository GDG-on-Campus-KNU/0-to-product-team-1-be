package com.gdg.backend.dto.ml;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MlReportRequest {

    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("week_id")
    private String weekId;
}
