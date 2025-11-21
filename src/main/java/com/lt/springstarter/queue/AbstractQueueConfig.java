package com.lt.springstarter.queue;

import com.lt.springstarter.config.RabbitQueueRetryConfig;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 抽象队列配置，封装通用的重试配置注册逻辑。
 */
@RequiredArgsConstructor
public abstract class AbstractQueueConfig {

    private final RabbitRetryRegistry rabbitRetryRegistry;

    @PostConstruct
    protected void initRetryConfig() {
        RabbitQueueRetryConfig config = queueRetryConfig();
        if (config != null) {
            rabbitRetryRegistry.register(queueName(), config);
        }
    }

    /**
     * 子类返回队列名称，用于注册映射。
     */
    protected abstract String queueName();

    /**
     * 子类可覆盖提供具体配置，返回 null 则表示沿用全局默认值。
     */
    protected RabbitQueueRetryConfig queueRetryConfig() {
        return null;
    }

    /**
     * 便捷方法，快速构造重试配置。
     */
    protected RabbitQueueRetryConfig buildRetryConfig(Integer maxRetry,
                                                      List<Long> delays,
                                                      Boolean notifyFeishu) {
        RabbitQueueRetryConfig config = new RabbitQueueRetryConfig();
        if (maxRetry != null) {
            config.setMaxRetry(maxRetry);
        }
        if (!CollectionUtils.isEmpty(delays)) {
            config.setDelays(delays);
        }
        if (notifyFeishu != null) {
            config.setNotifyFeishu(notifyFeishu);
        }
        return config;
    }
}

