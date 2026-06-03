package com.gdg.backend.dto;

import lombok.Getter;

@Getter
public class ApiResponse<T> {

    private final String code;
    private final String message;
    private final T data;

    private ApiResponse(String code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>("SUCCESS", "요청이 성공했습니다.", data);
    }

    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>("SUCCESS", "요청이 성공했습니다.", null);
    }

    public static <T> ApiResponse<T> fail(String code, String message) {
        return new ApiResponse<>(code, message, null);
    }
}
