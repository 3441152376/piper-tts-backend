# 构建阶段
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 mvn -q -e -B dependency:go-offline
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -q -e -B clean package -DskipTests

# 运行阶段
FROM eclipse-temurin:21-jre
ENV TZ=Asia/Shanghai
WORKDIR /opt/tts

# 安装 Piper（通过 Python 包），同时保留系统干净
RUN apt-get update && apt-get install -y --no-install-recommends python3 python3-pip && \
    python3 -m pip install --no-cache-dir --upgrade pip && \
    python3 -m pip install --no-cache-dir piper-tts && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

# 模型目录（通过挂载提供）
VOLUME ["/models"]

COPY --from=build /app/target/piper-tts-backend-*.jar /opt/tts/app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/opt/tts/app.jar"]


