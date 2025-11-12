# Piper TTS 后端（中文/俄语）

基于 [Piper](https://github.com/OHF-Voice/piper1-gpl) 的本地化文本转语音（TTS）后端服务，仅支持中文（zh）与俄语（ru）。

## 特性

- 仅支持 `zh` 与 `ru`，开箱即用的 REST API
- 无硬编码：模型路径、超时、二进制路径均通过配置项管理
- 流式返回 WAV 音频
- 安全：进程超时、输入大小限制、避免命令注入
- Docker 支持，便于部署

## 运行要求

- Java 21+
- 安装 Piper 可执行文件（建议：`python3 -m pip install piper-tts`）
- 准备 Piper 模型（.onnx），各语言至少一个模型

模型与 Piper 项目参考：
- Piper 项目仓库：[`OHF-Voice/piper1-gpl`](https://github.com/OHF-Voice/piper1-gpl)

## 配置

编辑 `src/main/resources/application.yml`：

```yaml
tts:
  piper:
    binaryPath: piper
    processTimeoutMs: 60000
    maxTextLength: 5000
    zh:
      modelPath: /models/zh/model.onnx
    ru:
      modelPath: /models/ru/model.onnx
```

> 提示：将真实模型文件挂载到容器 `/models`，或在本机配置绝对路径。

## 本地运行

```bash
mvn spring-boot:run
```

## Docker 运行

```bash
docker build -t piper-tts-backend:0.1.0 .
docker run --rm -p 8080:8080 \
  -v /abs/path/to/your/models:/models \
  -e "JAVA_TOOL_OPTIONS=-Xms256m -Xmx1024m" \
  piper-tts-backend:0.1.0
```

## API

### 合成语音

- `POST /api/v1/tts`
- Content-Type: `application/json`
- 响应：`audio/wav`（流式）

请求示例：

```json
{
  "text": "你好，世界！",
  "lang": "zh"
}
```

或

```json
{
  "text": "Привет, мир!",
  "lang": "ru",
  "speakerId": 0
}
```

### 健康检查

- `GET /api/v1/health`
- 响应：`application/json`

## 注意

- 文本长度受 `maxTextLength` 限制，防止过大请求导致资源耗尽。
- 若使用多说话人模型，可通过 `speakerId` 指定说话人。
- 合成使用临时文件，响应结束后会自动删除。

## 许可证

- 本服务使用的 TTS 引擎 Piper 源自 [`OHF-Voice/piper1-gpl`](https://github.com/OHF-Voice/piper1-gpl)（GPL-3.0）。


