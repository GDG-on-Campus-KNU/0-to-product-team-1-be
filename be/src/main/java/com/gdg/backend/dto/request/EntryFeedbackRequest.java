package com.gdg.backend.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class EntryFeedbackRequest {

    @JsonProperty("drill_completed")
    private Boolean drillCompleted;

    @JsonProperty("helpful")
    private Boolean helpful;
}
