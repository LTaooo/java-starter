package com.lt.springstarter.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

/**
 * RabbitMQ 重试配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "spring.rabbitmq.retry")
public class RabbitRetryProperties {

    /**
     * 全局最大重试次数
     */
    private int maxRetry = 2;

    /**
     * 全局每次重试的延迟（毫秒），长度不足时会取最后一个值
     */
    private List<Long> delays = Arrays.asList(5000L, 10000L);

    /**
     * 是否在最终失败时通知飞书
     */
    private boolean notifyFeishu = true;

    @Data
    public static class QueueRetryConfig {
        /**
         * 队列专属最大重试次数，为空时回退全局
         */
        private Integer maxRetry;
        /**
         * 队列专属延迟配置
         */
        private List<Long> delays;
        /**
         * 队列专属通知开关
         */
        private Boolean notifyFeishu;
    }
}

