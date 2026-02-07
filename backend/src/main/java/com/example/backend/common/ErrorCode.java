package com.example.backend.common;

public enum ErrorCode {

    PARAM_VALID_ERROR(40001, "参数校验失败"),
    PARAM_TYPE_ERROR(40002, "请求参数格式或类型错误"),
    PROJECT_NOT_FOUND(40401, "项目不存在"),
    RATE_LIMIT(42901, "请求过于频繁，请稍后重试"),
    INTERNAL_SERVER_ERROR(50000, "服务器内部错误");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}

