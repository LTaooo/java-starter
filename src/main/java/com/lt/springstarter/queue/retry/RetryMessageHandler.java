package com.lt.springstarter.queue.retry;

import com.lt.springstarter.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 负责统一的失败重试调度、延迟投递以及最终失败通知逻辑。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RetryMessageHandler {

    private final RabbitTemplate rabbitTemplate;
    private final RabbitRetryRegistry rabbitRetryRegistry;

    /**
     * 推送消息到指定队列，设置重试头信息。
     *
     * @param queueName  队列名称
     * @param exchange   交换机名称
     * @param routingKey 路由键
     * @param message    消息体
     */
    public void push(String queueName, String exchange, String routingKey, Object message) {
        rabbitTemplate.convertAndSend(exchange, routingKey, message, m -> {
                    Map<String, Object> headers = m.getMessageProperties().getHeaders();
                    RetryHeaderConfig config = rabbitRetryRegistry.get(queueName);
                    config.toInitHeaders(headers);
                    return m;
                }
        );
    }

    /**
     * 开始重试，如果重试次数未超过最大重试次数，则将消息投入延迟队列等待重试。
     * 如果重试次数已超过最大重试次数，则将消息投入失败队列。
     *
     */
    public void retry(Message failedMessage, Throwable cause) {
        RetryHeaderConfig headerConfig = RetryHeaderConfig.fromMessage(failedMessage);
        if (headerConfig == null) {
            log.error("消息重试配置不存在。queue={}", failedMessage.getMessageProperties().getConsumerQueue());
            fail(failedMessage, cause);
            return;
        }

        String queueName = failedMessage.getMessageProperties().getConsumerQueue();

        if (headerConfig.getRetryCount() >= headerConfig.getMaxRetry()) {
            log.warn("重试超过限制。queue={}, retry={}/{}", queueName, headerConfig.getRetryCount(), headerConfig.getMaxRetry());
            fail(failedMessage, cause);
            return;
        }

        int nextRetry = headerConfig.getRetryCount() + 1;
        Long ttl = headerConfig.delayForAttempt(nextRetry);
        if (ttl == null) {
            log.error("消息重试延迟时间不存在。queue={}, retry={}/{}", queueName, nextRetry, headerConfig.getMaxRetry());
            fail(failedMessage, cause);
            return;
        }

        Message retryMessage = buildRetryMessage(failedMessage, nextRetry, ttl);
        rabbitTemplate.send(RabbitMQConfig.DELAY_EXCHANGE_NAME,
                RabbitMQConfig.DELAY_ROUTING_KEY,
                retryMessage
        );
    }

    /**
     * 将超过最大重试次数的消息投入失败队列并视需求告警。
     */
    public void fail(Message failedMessage, @Nullable Throwable cause) {
        String queueName = failedMessage.getMessageProperties().getConsumerQueue();
        log.warn("消息进入失败队列。queue={}, routingKey={}, cause={}",
                queueName,
                failedMessage.getMessageProperties().getReceivedRoutingKey(),
                cause != null ? cause.getMessage() : null
        );

        rabbitTemplate.convertAndSend(RabbitMQConfig.FAIL_EXCHANGE_NAME,
                RabbitMQConfig.FAIL_ROUTING_KEY,
                failedMessage,
                message -> {
                    message.getMessageProperties().setExpiration(null);
                    return message;
                }
        );
    }

    private Message buildRetryMessage(Message failedMessage, int retryCount, @Nullable Long ttl) {
        Message retryMessage = MessageBuilder.fromMessage(failedMessage).build();
        Map<String, Object> newHeaders = retryMessage.getMessageProperties().getHeaders();
        newHeaders.putAll(failedMessage.getMessageProperties().getHeaders());
        newHeaders.put(RetryHeaderConfig.X_RETRY_COUNT, retryCount);
        retryMessage.getMessageProperties().setExpiration(ttl != null ? String.valueOf(ttl) : null);

        // 继承原消息的优先级，确保高优消息恢复顺序
        Integer priority = failedMessage.getMessageProperties().getPriority();
        if (priority != null) {
            retryMessage.getMessageProperties().setPriority(priority);
        }
        return retryMessage;
    }
}

