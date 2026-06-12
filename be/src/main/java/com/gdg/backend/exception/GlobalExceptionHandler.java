package com.gdg.backend.exception;

import com.gdg.backend.dto.ApiResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

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

    // 깨진 JSON / 빈 바디 → 400 (catch-all로 새서 500 되던 것 수정)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<?> handleMessageNotReadable(HttpMessageNotReadableException e) {
        return ApiResponse.fail("INVALID_REQUEST_BODY", "요청 본문을 읽을 수 없습니다. JSON 형식을 확인해주세요.");
    }

    // 파라미터 타입 불일치 (예: 숫자 자리에 문자) → 400
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<?> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        return ApiResponse.fail("INVALID_PARAMETER", "파라미터 형식이 올바르지 않습니다: " + e.getName());
    }

    // 필수 쿼리 파라미터 누락 → 400
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<?> handleMissingParameter(MissingServletRequestParameterException e) {
        return ApiResponse.fail("MISSING_PARAMETER", "필수 파라미터가 없습니다: " + e.getParameterName());
    }

    // 지원하지 않는 HTTP 메서드 → 405
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public ApiResponse<?> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        return ApiResponse.fail("METHOD_NOT_ALLOWED", "지원하지 않는 HTTP 메서드입니다: " + e.getMethod());
    }

    // 지원하지 않는 Content-Type → 415
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    public ApiResponse<?> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException e) {
        return ApiResponse.fail("UNSUPPORTED_MEDIA_TYPE", "Content-Type을 application/json으로 보내주세요.");
    }

    // 존재하지 않는 경로 → 404
    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponse<?> handleNoResourceFound(NoResourceFoundException e) {
        return ApiResponse.fail("NOT_FOUND", "존재하지 않는 경로입니다.");
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
