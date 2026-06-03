package com.gdg.backend.exception;

import com.gdg.backend.dto.ApiResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.UUID;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AuthException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<?> handleAuthException(AuthException e) {
        return ApiResponse.fail(e.getCode(), e.getMessage());
    }

    // 5.2 ML 429 → 429 + Retry-After 헤더
    @ExceptionHandler(QuotaExceededException.class)
    public ResponseEntity<ApiResponse<?>> handleQuotaExceeded(QuotaExceededException e) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Retry-After", String.valueOf(e.getRetryAfterSeconds()));
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .headers(headers)
                .body(ApiResponse.fail("QUOTA_EXCEEDED", e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<?> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        return ApiResponse.fail("VALIDATION_ERROR", message != null ? message : "텍스트 1~200자 입력해주세요");
    }

    // ML 포워딩 4xx/5xx → ML status·body 그대로 전달 (투명 passthrough)
    @ExceptionHandler(MlForwardException.class)
    public ResponseEntity<String> handleMlForward(MlForwardException e) {
        return ResponseEntity.status(e.getStatus())
                .contentType(MediaType.APPLICATION_JSON)
                .body(e.getBody());
    }

    // 8.3 ML 503 → 503
    @ExceptionHandler(MlUnavailableException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public ApiResponse<?> handleMlUnavailable(MlUnavailableException e) {
        return ApiResponse.fail("ML_UNAVAILABLE", e.getMessage());
    }

    // 일 1회 정책 — 중복 입력 409
    @ExceptionHandler(DuplicateEntryException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiResponse<?> handleDuplicateEntry(DuplicateEntryException e) {
        return ApiResponse.fail("DUPLICATE_ENTRY", e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<?> handleIllegalArgumentException(IllegalArgumentException e) {
        return ApiResponse.fail("NOT_FOUND", e.getMessage());
    }

    // 8.4 500 + request_id
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<?> handleException(Exception e) {
        String requestId = UUID.randomUUID().toString().substring(0, 8);
        e.printStackTrace();
        return ApiResponse.fail("INTERNAL_ERROR",
                "서버 오류가 발생했습니다. 문의 시 request_id를 알려주세요: " + requestId);
    }
}
