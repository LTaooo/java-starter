package com.lt.springstarter.queue.retry;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.lt.springstarter.util.Json;
import com.lt.springstarter.util.Validator;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.amqp.core.Message;
import org.springframework.lang.Nullable;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;

/**
 * 用于承载 RabbitMQ 消息头中的重试配置。
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RetryHeaderConfig {

    public static final String X_RETRY_COUNT = "x-retry-count";
    public static final String X_MAX_RETRY = "x-max-retry";
    public static final String X_DELAY = "x-retry-delay";
    public static final String X_ORIGINAL_ROUTING_KEY = "x-original-routing-key";
    public static final String X_ORIGINAL_EXCHANGE = "x-original-exchange";

    @JsonProperty(RetryHeaderConfig.X_RETRY_COUNT)
    @NotNull
    private int retryCount;

    @JsonProperty(RetryHeaderConfig.X_DELAY)
    @NotNull
    private List<Long> delays;

    @JsonProperty(RetryHeaderConfig.X_ORIGINAL_ROUTING_KEY)
    @NotNull
    private String originalRoutingKey;

    @JsonProperty(RetryHeaderConfig.X_ORIGINAL_EXCHANGE)
    @NotNull
    private String originalExchange;

    public static @Nullable RetryHeaderConfig fromHeaders(Map<String, Object> headers) {
        RetryHeaderConfig config = Json.toObject(Json.toJsonString(headers), RetryHeaderConfig.class);
        if (Validator.isValid(config)) {
            return config;
        }
        return null;
    }

    public static @Nullable RetryHeaderConfig fromMessage(Message message) {
        return fromHeaders(message.getMessageProperties().getHeaders());
    }

    public Long getMaxRetry() {
        return (long) delays.size();
    }

    public @Nullable Long delayForAttempt(int attempt) {
        if (CollectionUtils.isEmpty(delays)) {
            return null;
        }
        int index = Math.min(Math.max(attempt - 1, 0), delays.size() - 1);
        return delays.get(index);
    }

    public void toInitHeaders(Map<String, Object> headers) {
        fillHeaders(headers);
        headers.put(RetryHeaderConfig.X_RETRY_COUNT, 0);
    }

    public void fillHeaders(Map<String, Object> headers) {
        headers.put(RetryHeaderConfig.X_RETRY_COUNT, retryCount);
        headers.put(RetryHeaderConfig.X_MAX_RETRY, getMaxRetry());
        headers.put(RetryHeaderConfig.X_DELAY, delays);
        headers.put(RetryHeaderConfig.X_ORIGINAL_ROUTING_KEY, originalRoutingKey);
        headers.put(RetryHeaderConfig.X_ORIGINAL_EXCHANGE, originalExchange);
    }
}

