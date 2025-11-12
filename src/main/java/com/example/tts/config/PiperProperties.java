package com.example.tts.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Piper 相关配置，所有路径、超时等均从配置读取，避免硬编码。
 */
@Validated
@ConfigurationProperties(prefix = "tts.piper")
public class PiperProperties {
    /**
     * Piper 可执行文件路径（例如：piper），可为绝对路径或系统 PATH 中的命令名。
     */
    @NotBlank
    private String binaryPath = "piper";

    /**
     * 调用 Piper 的超时时间（毫秒）。
     */
    @Min(1000)
    @Max(300000)
    private long processTimeoutMs = 60000;

    /**
     * 文本最大长度限制，防止内存与时间开销过大。
     */
    @Min(1)
    @Max(20000)
    private int maxTextLength = 5000;

    /**
     * 中文模型配置。
     */
    @NotNull
    private LanguageModel zh = new LanguageModel();

    /**
     * 俄语模型配置。
     */
    @NotNull
    private LanguageModel ru = new LanguageModel();

    public String getBinaryPath() {
        return binaryPath;
    }

    public void setBinaryPath(final String binaryPath) {
        this.binaryPath = binaryPath;
    }

    public long getProcessTimeoutMs() {
        return processTimeoutMs;
    }

    public void setProcessTimeoutMs(final long processTimeoutMs) {
        this.processTimeoutMs = processTimeoutMs;
    }

    public int getMaxTextLength() {
        return maxTextLength;
    }

    public void setMaxTextLength(final int maxTextLength) {
        this.maxTextLength = maxTextLength;
    }

    public LanguageModel getZh() {
        return zh;
    }

    public void setZh(final LanguageModel zh) {
        this.zh = zh;
    }

    public LanguageModel getRu() {
        return ru;
    }

    public void setRu(final LanguageModel ru) {
        this.ru = ru;
    }

    /**
     * 语言模型配置。
     */
    @Validated
    public static class LanguageModel {
        /**
         * Piper 模型 .onnx 文件绝对路径。
         */
        @NotBlank
        private String modelPath;

        /**
         * 可选：说话人 ID（部分多说话人模型需要）。
         */
        @Min(0)
        private Integer speakerId;

        public String getModelPath() {
            return modelPath;
        }

        public void setModelPath(final String modelPath) {
            this.modelPath = modelPath;
        }

        public Integer getSpeakerId() {
            return speakerId;
        }

        public void setSpeakerId(final Integer speakerId) {
            this.speakerId = speakerId;
        }
    }
}


