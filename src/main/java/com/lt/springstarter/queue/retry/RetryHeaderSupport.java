package com.lt.springstarter.queue.retry;

import com.lt.springstarter.config.RabbitQueueRetryConfig;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;

/**
 * 统一的消息头写入工具。
 */
public final class RetryHeaderSupport {

    private RetryHeaderSupport() {
    }

    /**
     * 供生产者初始化消息头。
     */
    public static void ensureInitialHeaders(Map<String, Object> headers,
                                            String queueName,
                                            RabbitQueueRetryConfig queueConfig) {
        RabbitQueueRetryConfig config = requireConfig(queueName, queueConfig);
        headers.put(RabbitRetryHeaders.X_RETRY_COUNT, 0);
        headers.put(RabbitRetryHeaders.X_MAX_RETRY, config.getMaxRetry());
        headers.put(RabbitRetryHeaders.X_DELAY, config.getDelays());
        headers.put(RabbitRetryHeaders.X_NOTIFY_FEISHU, config.getNotifyFeishu());
        headers.put(RabbitRetryHeaders.X_ORIGINAL_QUEUE, queueName);
    }


    private static RabbitQueueRetryConfig requireConfig(String queueName, RabbitQueueRetryConfig queueConfig) {
        Assert.notNull(queueConfig, () -> "未找到队列 " + queueName + " 的重试配置");
        Assert.notNull(queueConfig.getMaxRetry(), () -> "队列 " + queueName + " 的 maxRetry 未配置");
        Assert.state(!CollectionUtils.isEmpty(queueConfig.getDelays()), () -> "队列 " + queueName + " 的 delays 未配置");
        Assert.notNull(queueConfig.getNotifyFeishu(), () -> "队列 " + queueName + " 的 notifyFeishu 未配置");
        return queueConfig;
    }
}

