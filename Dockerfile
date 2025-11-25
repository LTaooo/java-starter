# 使用多阶段构建
# 第一阶段：构建
FROM gradle:8.5-jdk17-alpine AS builder
WORKDIR /app
COPY . .
# 跳过测试以加快构建速度
RUN gradle clean build -x test --no-daemon

# 第二阶段：运行
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
# 从构建阶段复制 jar 包
COPY --from=builder /app/build/libs/*.jar app.jar

# 暴露端口
EXPOSE 8001

# 设置时区
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 启动命令
ENTRYPOINT ["java", "-jar", "app.jar"]

