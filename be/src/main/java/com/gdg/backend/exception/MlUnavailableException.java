package com.gdg.backend.exception;

public class MlUnavailableException extends RuntimeException {
    public MlUnavailableException(String cause) {
        super("지금 라벨링이 어려워요. 잠시 후 다시 시도해주세요. (" + cause + ")");
    }
}
