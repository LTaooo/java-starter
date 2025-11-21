package com.lt.springstarter.queue.retry;

import com.lt.springstarter.config.RabbitMQConfig;
import com.lt.springstarter.util.FeishuRobot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import java.util.List;
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
     * 调度下一次重试。
     *
     * @return true 表示已进入延迟队列等待重试，false 表示超过阈值
     */
    public boolean scheduleRetry(String queueName,
                                 String delayRoutingKey,
                                 Object payload,
                                 Message failedMessage,
                                 Throwable cause) {
        RetryHeaderConfig headerConfig = RetryHeaderConfig.fromMessage(failedMessage);
        ResolvedRetryConfig retryConfig = resolveRetryConfig(headerConfig);
        int currentRetry = headerConfig.getRetryCount();

        if (currentRetry >= retryConfig.maxRetry()) {
            return false;
        }

        int nextRetry = currentRetry + 1;
        long ttl = retryConfig.delayForAttempt(nextRetry);

        log.warn("消息消费失败，准备进入延迟队列重试。queue={}, retry={}/{}, ttl={}ms, payload={}, error={}",
                queueName, nextRetry, retryConfig.maxRetry(), ttl, payload, cause == null ? null : cause.getMessage());

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.DELAY_EXCHANGE_NAME,
                delayRoutingKey,
                payload,
                buildMessagePostProcessor(queueName, failedMessage, cause, nextRetry, retryConfig, ttl)
        );
        return true;
    }

    /**
     * 将超过最大重试次数的消息投入失败队列并视需求告警。
     */
    public void routeToFailQueue(String queueName,
                                 Object payload,
                                 Message failedMessage,
                                 Throwable cause) {
        RetryHeaderConfig headerConfig = RetryHeaderConfig.fromMessage(failedMessage);
        ResolvedRetryConfig retryConfig = resolveRetryConfig(headerConfig);
        int currentRetry = headerConfig.getRetryCount();

        log.error("消息重试已达上限，进入失败队列。queue={}, retry={}, payload={}, error={}",
                queueName, currentRetry, payload, cause == null ? null : cause.getMessage(), cause);

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.DLX_EXCHANGE_NAME,
                RabbitMQConfig.DLX_ROUTING_KEY,
                payload,
                buildMessagePostProcessor(queueName, failedMessage, cause, currentRetry, retryConfig, null)
        );

        if (retryConfig.notifyFeishu()) {
            sendFeishuAlert(queueName, payload, cause, currentRetry);
        }
    }

    private MessagePostProcessor buildMessagePostProcessor(String queueName,
                                                           Message failedMessage,
                                                           Throwable cause,
                                                           int retryCount,
                                                           ResolvedRetryConfig retryConfig,
                                                           Long ttl) {
        return message -> {
            Map<String, Object> newHeaders = message.getMessageProperties().getHeaders();
            newHeaders.putAll(failedMessage.getMessageProperties().getHeaders());
            newHeaders.put(RabbitRetryHeaders.X_RETRY_COUNT, retryCount);
            newHeaders.put(RabbitRetryHeaders.X_MAX_RETRY, retryConfig.maxRetry());
            newHeaders.put(RabbitRetryHeaders.X_DELAY, retryConfig.delays());
            newHeaders.put(RabbitRetryHeaders.X_NOTIFY_FEISHU, retryConfig.notifyFeishu());
            newHeaders.put(RabbitRetryHeaders.X_ORIGINAL_QUEUE, queueName);
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
                cause == null ? "unknown" : cause.getMessage());
        feishuRobot.sendTextMessage(content);
    }

    private ResolvedRetryConfig resolveRetryConfig(RetryHeaderConfig headerConfig) {
        return new ResolvedRetryConfig(
                headerConfig.getMaxRetry(),
                headerConfig.getDelays(),
                headerConfig.getNotifyFeishu()
        );
    }

    private record ResolvedRetryConfig(int maxRetry, List<Long> delays, boolean notifyFeishu) {

        long delayForAttempt(int attempt) {
            if (CollectionUtils.isEmpty(delays)) {
                throw new IllegalStateException("未找到任何重试延迟配置");
            }
            int index = Math.min(Math.max(attempt - 1, 0), delays.size() - 1);
            return delays.get(index);
        }
    }
}

