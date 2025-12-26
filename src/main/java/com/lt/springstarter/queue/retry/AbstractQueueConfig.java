package com.lt.springstarter.queue.retry;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * 抽象队列配置，封装通用的重试配置注册逻辑。
 */
@RequiredArgsConstructor
public abstract class AbstractQueueConfig {

    private final RabbitRetryRegistry rabbitRetryRegistry;

    @PostConstruct
    protected void initRetryConfig() {
        rabbitRetryRegistry.register(queueName(), queueRetryConfig());
    }

    /**
     * 子类返回队列名称，用于注册映射。
     */
    protected abstract String queueName();

    protected abstract String exchangeName();

    protected abstract String routingKey();

    /**
     * 子类可覆盖提供具体配置，返回 null 则表示沿用全局默认值。
     */
    protected abstract RetryHeaderConfig queueRetryConfig();

    /**
     * 便捷方法，快速构造重试配置。
     */
    protected RetryHeaderConfig buildRetryConfig(List<Long> delays) {
        RetryHeaderConfig config = new RetryHeaderConfig();
        config.setRetryCount(0);
        config.setOriginalRoutingKey(routingKey());
        config.setDelays(delays);
        config.setOriginalExchange(exchangeName());
        return config;
    }
}

