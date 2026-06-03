package com.gdg.backend.exception;

import org.springframework.http.HttpStatusCode;

/**
 * ML 포워딩 중 ML이 4xx/5xx를 반환했을 때, 그 status·body를 그대로 FE로 전달하기 위한 예외.
 * (불투명 500으로 마스킹하지 않고 ML 응답을 투명하게 passthrough)
 */
public class MlForwardException extends RuntimeException {

    private final HttpStatusCode status;
    private final String body;

    public MlForwardException(HttpStatusCode status, String body) {
        super("ML forward error: " + status);
        this.status = status;
        this.body = body;
    }

    public HttpStatusCode getStatus() {
        return status;
    }

    public String getBody() {
        return body;
    }
}
