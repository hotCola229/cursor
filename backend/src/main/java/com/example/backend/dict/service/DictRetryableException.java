package com.example.backend.dict.service;

/**
 * 用于触发 spring-retry 的重试（仅对 5xx、超时、IO 异常重试）
 */
public class DictRetryableException extends RuntimeException {
    private final Integer httpStatus;

    public DictRetryableException(String message, Throwable cause) {
        super(message, cause);
        this.httpStatus = null;
    }

    public DictRetryableException(String message, int httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public Integer getHttpStatus() {
        return httpStatus;
    }
}
