package com.lt.springstarter.queue.retry;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 允许业务模块以代码方式注册自定义的队列重试配置。
 */
@Component
public class RabbitRetryRegistry {

    private final Map<String, RetryHeaderConfig> queueConfigs = new ConcurrentHashMap<>();

    public void register(String queueName, RetryHeaderConfig config) {
        queueConfigs.put(queueName, config);
    }

    public RetryHeaderConfig get(String queueName) {
        return queueConfigs.get(queueName);
    }
}

