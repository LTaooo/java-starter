package com.lt.springstarter.queue;

import com.lt.springstarter.config.RabbitQueueRetryConfig;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 允许业务模块以代码方式注册自定义的队列重试配置。
 */
@Component
public class RabbitRetryRegistry {

    private final Map<String, RabbitQueueRetryConfig> queueConfigs = new ConcurrentHashMap<>();

    public void register(String queueName, RabbitQueueRetryConfig config) {
        Assert.hasText(queueName, "queueName must not be blank");
        Assert.notNull(config, "queue retry config must not be null");
        queueConfigs.put(queueName, config);
    }

    public RabbitQueueRetryConfig get(String queueName) {
        return queueConfigs.get(queueName);
    }

    public Map<String, RabbitQueueRetryConfig> getAll() {
        return Collections.unmodifiableMap(queueConfigs);
    }
}

