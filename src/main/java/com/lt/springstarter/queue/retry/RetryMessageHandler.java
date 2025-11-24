package com.lt.springstarter.queue.retry;

import com.lt.springstarter.config.RabbitMQConfig;
import com.lt.springstarter.util.FeishuRobot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Map;

/**
 * 负责统一的失败重试调度、延迟投递以及最终失败通知逻辑。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RetryMessageHandler {

    private final RabbitTemplate rabbitTemplate;
    private final FeishuRobot feishuRobot;

    /**
     * 开始重试，如果重试次数未超过最大重试次数，则将消息投入延迟队列等待重试。
     * 如果重试次数已超过最大重试次数，则将消息投入失败队列。
     *
     */
    public void retry(Message failedMessage, Throwable cause) {
        String payload = Arrays.toString(failedMessage.getBody());
        RetryHeaderConfig headerConfig = RetryHeaderConfig.fromMessage(failedMessage);
        if (headerConfig == null) {
            log.error("消息重试配置不存在。queue={}", failedMessage.getMessageProperties().getConsumerQueue());
            fail(payload, failedMessage, cause);
            return;
        }

        String queueName = failedMessage.getMessageProperties().getConsumerQueue();

        if (headerConfig.getRetryCount() >= headerConfig.getMaxRetry()) {
            log.warn("重试超过限制。queue={}, retry={}/{}", queueName, headerConfig.getRetryCount(), headerConfig.getMaxRetry());
            fail(payload, failedMessage, cause);
            return;
        }

        int nextRetry = headerConfig.getRetryCount() + 1;
        Long ttl = headerConfig.delayForAttempt(nextRetry);
        if (ttl == null) {
            log.error("消息重试延迟时间不存在。queue={}, retry={}/{}", queueName, nextRetry, headerConfig.getMaxRetry());
            fail(payload, failedMessage, cause);
            return;
        }

        rabbitTemplate.convertAndSend(RabbitMQConfig.DELAY_EXCHANGE_NAME,
                                      RabbitMQConfig.DELAY_ROUTING_KEY,
                                      payload,
                                      buildMessagePostProcessor(failedMessage, nextRetry, ttl)
        );
    }

    /**
     * 将超过最大重试次数的消息投入失败队列并视需求告警。
     */
    public void fail(Object payload, Message failedMessage, @Nullable Throwable cause) {
        String queueName = failedMessage.getMessageProperties().getConsumerQueue();
        log.warn("消息进入失败队列。queue={}, routingKey={}",
                  queueName,
                  failedMessage.getMessageProperties().getReceivedRoutingKey()
        );

        rabbitTemplate.convertAndSend(RabbitMQConfig.FAIL_EXCHANGE_NAME,
                                      RabbitMQConfig.FAIL_ROUTING_KEY,
                                      failedMessage,
                                      message -> {
                                          message.getMessageProperties().setExpiration(null);
                                          return message;
                                      }
        );
        RetryHeaderConfig headerConfig = RetryHeaderConfig.fromMessage(failedMessage);
        if (headerConfig != null) {
            if (headerConfig.getNotifyFeishu()) {
                sendFeishuAlert(queueName, payload, cause, headerConfig.getRetryCount());
            }
        }
    }

    private MessagePostProcessor buildMessagePostProcessor(Message failedMessage, int retryCount, @Nullable Long ttl) {
        return message -> {
            Map<String, Object> newHeaders = message.getMessageProperties().getHeaders();
            newHeaders.putAll(failedMessage.getMessageProperties().getHeaders());
            newHeaders.put(RetryHeaderConfig.X_RETRY_COUNT, retryCount);
            if (ttl != null) {
                message.getMessageProperties().setExpiration(String.valueOf(ttl));
            }

            // 继承原消息的优先级，确保高优消息恢复顺序
            Integer priority = failedMessage.getMessageProperties().getPriority();
            if (priority != null) {
                message.getMessageProperties().setPriority(priority);
            }
            return message;
        };
    }

    private void sendFeishuAlert(String queueName, Object payload, Throwable cause, int retryCount) {
        String content = String.format("队列: %s\n重试次数: %d\npayload: %s\nerror: %s",
                                       queueName,
                                       retryCount,
                                       payload,
                                       cause == null ? "unknown" : cause.getMessage()
        );
        feishuRobot.sendTextMessage(content);
    }
}

