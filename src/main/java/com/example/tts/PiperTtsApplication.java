package com.example.tts;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 应用入口：基于 Piper 的本地化 TTS 后端，仅支持中文与俄语。
 * 说明：
 * - 采用分层架构（Controller / Service / Config），接口语义清晰，易于维护与扩展。
 * - 通过配置文件指定模型路径，避免任何硬编码。
 * - 进程调用 Piper 二进制以生成 WAV 音频，提供流式响应。
 */
@SpringBootApplication
public class PiperTtsApplication {
    public static void main(final String[] args) {
        SpringApplication.run(PiperTtsApplication.class, args);
    }
}


