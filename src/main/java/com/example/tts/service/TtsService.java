package com.example.tts.service;

import com.example.tts.config.PiperProperties;
import com.example.tts.exception.BusinessException;
import com.example.tts.model.Language;
import com.example.tts.service.process.PiperProcessManager;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.Objects;

/**
 * TTS 核心服务：按语言选择对应模型，调用 Piper 生成 WAV。
 */
@Service
public class TtsService {
    private final PiperProperties properties;
    private final PiperProcessManager processManager;

    public TtsService(final PiperProperties properties, final PiperProcessManager processManager) {
        this.properties = properties;
        this.processManager = processManager;
    }

    /**
     * 根据语言合成语音，返回 WAV 文件路径（临时文件）。
     *
     * @param text      文本
     * @param language  语言（仅 zh 或 ru）
     * @param speakerId 可选：说话人 ID，覆盖配置
     * @return WAV 临时文件路径
     */
    public Path synthesizeToWav(final String text, final Language language, final Integer speakerId) {
        if (language == null) {
            throw new BusinessException("LANG_REQUIRED", "语言必填");
        }
        String modelPath;
        Integer finalSpeakerId = speakerId;
        switch (language) {
            case zh -> {
                modelPath = properties.getZh().getModelPath();
                if (finalSpeakerId == null) {
                    finalSpeakerId = properties.getZh().getSpeakerId();
                }
            }
            case ru -> {
                modelPath = properties.getRu().getModelPath();
                if (finalSpeakerId == null) {
                    finalSpeakerId = properties.getRu().getSpeakerId();
                }
            }
            default -> throw new BusinessException("LANG_NOT_SUPPORTED", "不支持的语言");
        }
        return processManager.synthesizeToWav(text, modelPath, finalSpeakerId);
    }
}


