package com.example.tts.exception;

/**
 * 业务异常，包含稳定的错误消息，便于上层转换为 HTTP 错误响应。
 */
public class BusinessException extends RuntimeException {
    private final String code;

    public BusinessException(final String code, final String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}


