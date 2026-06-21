package com.gdg.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class WeeklyMemoRequest {

    @NotBlank(message = "메모를 입력해주세요.")
    @Size(max = 100, message = "메모는 최대 100자까지 입력 가능합니다.")
    private String memo;
}
