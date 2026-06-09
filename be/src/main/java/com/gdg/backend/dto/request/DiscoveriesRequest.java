package com.gdg.backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.util.List;

@Getter
public class DiscoveriesRequest {

    @Schema(description = "사용자가 입력한 발견 목록 (최대 5개)", example = "[\"잠이 부족한 날 불안 표현이 더 자주 보였어요\"]")
    @NotEmpty
    @Size(max = 5)
    private List<String> discoveries;
}
