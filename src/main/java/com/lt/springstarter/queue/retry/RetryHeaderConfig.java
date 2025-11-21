package com.lt.springstarter.queue.retry;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.lt.springstarter.util.Json;
import com.lt.springstarter.util.Validator;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.amqp.core.Message;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 用于承载 RabbitMQ 消息头中的重试配置。
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RetryHeaderConfig {

    @JsonProperty(RabbitRetryHeaders.X_RETRY_COUNT)
    @NotNull
    private int retryCount;

    @JsonProperty(RabbitRetryHeaders.X_MAX_RETRY)
    @NotNull
    private Integer maxRetry;

    @JsonProperty(RabbitRetryHeaders.X_DELAY)
    @NotNull
    private List<Long> delays;

    @JsonProperty(RabbitRetryHeaders.X_NOTIFY_FEISHU)
    @NotNull
    private Boolean notifyFeishu;

    @JsonProperty(RabbitRetryHeaders.X_ORIGINAL_QUEUE)
    @NotNull
    private String originalQueue;

    public static RetryHeaderConfig fromHeaders(Map<String, Object> headers) {
        RetryHeaderConfig config = Json.toObject(Json.toJsonString(headers), RetryHeaderConfig.class);
        Validator.check(config);
        return Objects.requireNonNullElseGet(config, RetryHeaderConfig::new);
    }

    public static RetryHeaderConfig fromMessage(Message message) {
        return fromHeaders(message.getMessageProperties().getHeaders());
    }

    public long delayForAttempt(int attempt) {
        if (CollectionUtils.isEmpty(delays)) {
            throw new IllegalStateException("未找到任何重试延迟配置");
        }
        int index = Math.min(Math.max(attempt - 1, 0), delays.size() - 1);
        return delays.get(index);
    }
}

