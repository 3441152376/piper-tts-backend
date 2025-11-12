package com.example.tts.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * TTS 合成请求。
 */
public class TtsRequest {
    /**
     * 文本内容。
     */
    @NotBlank
    @Size(min = 1, max = 20000)
    private String text;

    /**
     * 语言：仅支持 zh / ru。
     */
    @NotNull
    private Language lang;

    /**
     * 可选：说话人 ID（仅对多说话人模型有意义），优先级高于配置。
     */
    @Min(0)
    private Integer speakerId;

    /**
     * 预留：采样率（由模型决定，通常无需传入）。
     */
    @Min(8000)
    @Max(48000)
    private Integer sampleRate;

    public String getText() {
        return text;
    }

    public void setText(final String text) {
        this.text = text;
    }

    public Language getLang() {
        return lang;
    }

    public void setLang(final Language lang) {
        this.lang = lang;
    }

    public Integer getSpeakerId() {
        return speakerId;
    }

    public void setSpeakerId(final Integer speakerId) {
        this.speakerId = speakerId;
    }

    public Integer getSampleRate() {
        return sampleRate;
    }

    public void setSampleRate(final Integer sampleRate) {
        this.sampleRate = sampleRate;
    }
}


