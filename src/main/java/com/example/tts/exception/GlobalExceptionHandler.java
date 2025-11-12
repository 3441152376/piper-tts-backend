package com.example.tts.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理，统一返回 JSON，避免泄露内部信息。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, Object>> handleBusinessException(final BusinessException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("code", ex.getCode());
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON).body(body);
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, ConstraintViolationException.class, IllegalArgumentException.class})
    public ResponseEntity<Map<String, Object>> handleValidationException(final Exception ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("code", "INVALID_ARGUMENT");
        body.put("message", "请求参数不合法");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE + ";charset=" + StandardCharsets.UTF_8)
                .body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(final Exception ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("code", "INTERNAL_ERROR");
        body.put("message", "服务器内部错误");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(body);
    }
}


