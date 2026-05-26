package com.gdg.backend.exception;

import lombok.Getter;

@Getter
public class QuotaExceededException extends RuntimeException {

    private final long retryAfterSeconds;

    public QuotaExceededException(long retryAfterSeconds) {
        super("요청 한도를 초과했습니다. " + retryAfterSeconds + "초 후에 다시 시도해주세요.");
        this.retryAfterSeconds = retryAfterSeconds;
    }
}
