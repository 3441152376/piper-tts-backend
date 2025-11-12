package com.example.tts.controller;

import com.example.tts.model.TtsRequest;
import com.example.tts.service.TtsService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

/**
 * TTS 控制器：提供语音合成接口。
 */
@RestController
@RequestMapping("/api/v1/tts")
public class TtsController {
    private final TtsService ttsService;

    public TtsController(final TtsService ttsService) {
        this.ttsService = ttsService;
    }

    /**
     * 合成语音（WAV）并流式返回。
     * 注意：返回完成后删除临时文件，避免残留。
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StreamingResponseBody> synthesize(@Valid @RequestBody final TtsRequest request) {
        Path wavPath = ttsService.synthesizeToWav(request.getText(), request.getLang(), request.getSpeakerId());
        String filename = "tts_" + request.getLang() + "_" + LocalDateTime.now().toString().replace(":", "-") + ".wav";
        StreamingResponseBody body = outputStream -> {
            try (InputStream is = new FileInputStream(wavPath.toFile())) {
                FileCopyUtils.copy(is, outputStream);
                outputStream.flush();
            } finally {
                try {
                    Files.deleteIfExists(wavPath);
                } catch (IOException ignored) {
                }
            }
        };
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("audio/wav"));
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"");
        return new ResponseEntity<>(body, headers, HttpStatus.OK);
    }
}


