package com.example.tts.service.process;

import com.example.tts.config.PiperProperties;
import com.example.tts.exception.BusinessException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Piper 进程封装：负责以安全方式调用 Piper 生成 WAV。
 * 实现要点：
 * - 通过标准输入传递文本，避免 shell 拼接，防止命令注入。
 * - 可配置的超时，避免僵尸进程。
 * - 使用临时文件存储 WAV，调用者负责后续删除。
 */
@Component
public class PiperProcessManager {
    private final PiperProperties properties;

    public PiperProcessManager(final PiperProperties properties) {
        this.properties = properties;
    }

    /**
     * 使用 Piper 生成 WAV 文件。
     *
     * @param text        文本
     * @param modelPath   模型 .onnx 路径
     * @param speakerId   可选：说话人 ID
     * @return 生成的 WAV 临时文件路径
     */
    public Path synthesizeToWav(final String text, final String modelPath, final Integer speakerId) {
        validate(text, modelPath);
        Path tempFile = createTempWav();
        List<String> command = buildCommand(modelPath, speakerId, tempFile.toAbsolutePath().toString());
        execute(text, command, tempFile);
        return tempFile;
    }

    private void validate(final String text, final String modelPath) {
        if (StringUtils.isBlank(text)) {
            throw new BusinessException("EMPTY_TEXT", "文本为空");
        }
        if (text.length() > properties.getMaxTextLength()) {
            throw new BusinessException("TEXT_TOO_LONG", "文本超出长度限制");
        }
        if (StringUtils.isBlank(modelPath)) {
            throw new BusinessException("MODEL_PATH_MISSING", "模型路径未配置");
        }
        if (!Files.isRegularFile(Path.of(modelPath))) {
            throw new BusinessException("MODEL_NOT_FOUND", "模型文件不存在");
        }
    }

    private Path createTempWav() {
        try {
            Path temp = Files.createTempFile("piper_", ".wav");
            temp.toFile().deleteOnExit();
            return temp;
        } catch (IOException e) {
            throw new BusinessException("TEMP_FILE_ERROR", "创建临时文件失败");
        }
    }

    private List<String> buildCommand(final String modelPath, final Integer speakerId, final String outputFile) {
        List<String> cmd = new ArrayList<>();
        cmd.add(properties.getBinaryPath());
        cmd.add("--model");
        cmd.add(modelPath);
        if (speakerId != null) {
            cmd.add("--speaker");
            cmd.add(String.valueOf(speakerId));
        }
        cmd.add("--output_file");
        cmd.add(outputFile);
        return cmd;
    }

    private void execute(final String text, final List<String> command, final Path outputFile) {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        try {
            Process process = pb.start();
            Future<?> ioTask = Executors.newSingleThreadExecutor().submit(() -> writeTextToStdin(process, text));
            boolean finished = process.waitFor(properties.getProcessTimeoutMs(), TimeUnit.MILLISECONDS);
            ioTask.get(10, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                Files.deleteIfExists(outputFile);
                throw new BusinessException("PIPER_TIMEOUT", "Piper 超时未返回");
            }
            int code = process.exitValue();
            if (code != 0 || !Files.isRegularFile(outputFile) || fileTooSmall(outputFile)) {
                Files.deleteIfExists(outputFile);
                throw new BusinessException("PIPER_FAILED", "Piper 生成音频失败");
            }
        } catch (BusinessException be) {
            throw be;
        } catch (Exception e) {
            try {
                Files.deleteIfExists(outputFile);
            } catch (IOException ignored) {
            }
            throw new BusinessException("PIPER_ERROR", "调用 Piper 发生错误");
        }
    }

    private boolean fileTooSmall(final Path outputFile) {
        try {
            return Files.size(outputFile) < 44; // WAV header 最小 44 字节
        } catch (IOException e) {
            return true;
        }
    }

    private void writeTextToStdin(final Process process, final String text) {
        try (OutputStream os = process.getOutputStream()) {
            os.write(text.getBytes(StandardCharsets.UTF_8));
            os.flush();
        } catch (IOException e) {
            // 让上层通过退出码处理
        }
    }
}


