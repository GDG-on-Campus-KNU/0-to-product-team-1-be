package com.gdg.backend.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RejectRequest {

    @JsonProperty("drill_id")
    private Integer drillId;
}
