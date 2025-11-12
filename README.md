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

## 扩展更多语言（示例：英语 en）
当前仓库默认内置支持中文（`zh`）与俄语（`ru`）。若需要英语等更多语言，有三种方式可选：

1) 临时替换模型（零代码改动）
- 下载英语模型（`.onnx` 与同名 `.onnx.json`）后，用启动参数把英语模型路径覆盖到现有 `ru` 的 `modelPath` 上，仅用于临时验证。
- 局限：接口中的 `lang` 仍需传 `ru` 才能命中该模型，不利于前端语义与后续扩展。

2) 多实例部署（推荐）
- 复制本服务部署一份，仅将模型切换为英语并监听不同端口/域名；由网关/Nginx 按语言路由到对应实例。
- 前端：按语言选择调用不同域名/路径（例如 `ttszr.egg404.com` 负责 zh/ru，`tts-en.egg404.com` 负责 en）。

3) 代码扩展（长期方案）
- 修改 `Language` 枚举新增 `en`；
- 在 `PiperProperties` 增加 `en` 的 `LanguageModel` 字段，并在 `application.yml` 增加 `tts.piper.en.modelPath`；
- 在 `TtsService` 的语言分支中增加 `en` 逻辑；
- 文档中补充 `lang=en`。

英语模型参考（需下载 `.onnx` 与同名 `.onnx.json`）：
- en_US（美国英语）目录：`https://huggingface.co/rhasspy/piper-voices/tree/main/en/en_US`
- en_GB（英国英语）目录：`https://huggingface.co/rhasspy/piper-voices/tree/main/en/en_GB`
- Piper 项目：`https://github.com/OHF-Voice/piper1-gpl`

## 语言与模型说明（俄语 & 英语）

### 俄语（ru）
- 推荐模型：`ru_RU/irina/medium`
  - 模型与配置下载（需成对）：  
    - ONNX：`ru_RU-irina-medium.onnx`  
    - JSON：`ru_RU-irina-medium.onnx.json`  
  - 目录链接：`https://huggingface.co/rhasspy/piper-voices/tree/main/ru/ru_RU/irina/medium`
- 启动参数（示例）：
```bash
java -Dserver.port=8080 \
  -Dtts.piper.binaryPath=/usr/local/bin/piper \
  -Dtts.piper.ru.modelPath=/abs/path/ru_RU-irina-medium.onnx \
  -Dtts.piper.zh.modelPath=/abs/path/zh_CN-huayan-medium.onnx \
  -jar app.jar
```
- 合成示例：
```bash
curl -sS --fail -X POST https://ttszr.egg404.com/api/v1/tts \
  -H "Content-Type: application/json" \
  --data '{"text":"Привет, мир!","lang":"ru"}' \
  --output out_ru.wav
```
- 注意事项：
  - 请求体必须为 UTF-8；俄文文本使用西里尔字符集，避免编码混乱。
  - Piper 需要 `.onnx` 与同名 `.onnx.json` 同目录存在，否则会报错。
  - 个别多说话人模型需要 `speakerId`。`irina` 通常为单说话人，无需指定。

### 英语（en）
- 当前后端默认未内置 `en`，可按“扩展更多语言”章节操作：
  - 多实例（推荐）或代码扩展枚举与配置后，即可通过 `lang=en` 使用。
- 模型目录（二选一示例）：
  - en_US：`https://huggingface.co/rhasspy/piper-voices/tree/main/en/en_US`
  - en_GB：`https://huggingface.co/rhasspy/piper-voices/tree/main/en/en_GB`
- 合成示例（假设已扩展或使用英语专用实例域名）：
```bash
curl -sS --fail -X POST https://tts-en.egg404.com/api/v1/tts \
  -H "Content-Type: application/json" \
  --data '{"text":"Hello world!","lang":"en"}' \
  --output out_en.wav
```
- 注意事项：
  - 依旧需确保 `.onnx` 与同名 `.onnx.json` 配置文件同目录存在。
  - 多说话人模型时可使用 `speakerId` 指定说话人。

## 许可证

- 本服务使用的 TTS 引擎 Piper 源自 [`OHF-Voice/piper1-gpl`](https://github.com/OHF-Voice/piper1-gpl)（GPL-3.0）。


