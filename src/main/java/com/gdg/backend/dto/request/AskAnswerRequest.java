package com.gdg.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AskAnswerRequest {

    @NotBlank
    private String choice; // "yes" or "no"
}
