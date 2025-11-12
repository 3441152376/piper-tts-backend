package com.example.tts.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查与元数据接口。
 */
@RestController
@RequestMapping("/api/v1/health")
public class HealthController {
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> getStatus() {
        Map<String, Object> data = new HashMap<>();
        data.put("status", "ok");
        data.put("service", "piper-tts-backend");
        data.put("version", "0.1.0");
        data.put("langs", new String[]{"zh", "ru"});
        return data;
    }
}


