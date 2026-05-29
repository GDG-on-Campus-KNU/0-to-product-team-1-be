package com.gdg.backend.dto.ml;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MlLabelRequest {

    private String text;

    @JsonProperty("user_id")
    private String userId;

    public static MlLabelRequest of(String text, Long userId) {
        return MlLabelRequest.builder()
                .text(text)
                .userId(String.valueOf(userId))
                .build();
    }
}
