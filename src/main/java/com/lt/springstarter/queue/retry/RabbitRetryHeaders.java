package com.lt.springstarter.queue.retry;

/**
 * 统一维护 RabbitMQ 重试相关的消息头字段，避免魔法字符串分散在各处。
 */
public final class RabbitRetryHeaders {

    private RabbitRetryHeaders() {
    }

    public static final String X_RETRY_COUNT = "x-retry-count";
    public static final String X_MAX_RETRY = "x-max-retry";
    public static final String X_DELAY = "x-delay";
    public static final String X_NOTIFY_FEISHU = "x-notify-feishu";
    public static final String X_ORIGINAL_QUEUE = "x-original-queue";
}

