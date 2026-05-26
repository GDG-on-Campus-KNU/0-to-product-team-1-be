package com.gdg.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@Getter
@NoArgsConstructor
public class EntryCreateRequest {

    @NotBlank(message = "텍스트 1~200자 입력해주세요")
    @Size(min = 1, max = 200, message = "텍스트 1~200자 입력해주세요")
    private String text;

    private Map<String, Object> context;

    private com.gdg.backend.entity.TimeOfDay timeOfDay;
}
