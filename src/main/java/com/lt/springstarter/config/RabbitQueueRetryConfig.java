package com.lt.springstarter.config;

import com.lt.springstarter.queue.retry.RabbitRetryHeaders;
import lombok.Data;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 队列级别的重试配置。
 */
@Data
public class RabbitQueueRetryConfig {

    /**
     * 队列专属最大重试次数，为空时回退全局。
     */
    private Integer maxRetry;

    /**
     * 队列专属延迟配置。
     */
    private List<Long> delays;

    /**
     * 队列专属通知开关。
     */
    private Boolean notifyFeishu;

    public Map<String, Object> getHeaders() {
        Map<String, Object> headers = new HashMap<>();
        if (maxRetry != null) {
            headers.put(RabbitRetryHeaders.X_MAX_RETRY, maxRetry);
        }
        if (!CollectionUtils.isEmpty(delays)) {
            headers.put(RabbitRetryHeaders.X_DELAY, delays);
        }
        if (notifyFeishu != null) {
            headers.put(RabbitRetryHeaders.X_NOTIFY_FEISHU, notifyFeishu);
        }
        return headers;
    }
}

