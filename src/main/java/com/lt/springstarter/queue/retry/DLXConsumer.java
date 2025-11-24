package com.lt.springstarter.queue.retry;

import com.lt.springstarter.config.RabbitMQConfig;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class DLXConsumer {
    private final RabbitTemplate rabbitTemplate;

    private final RetryMessageHandler retryMessageHandler;

    public DLXConsumer(RabbitTemplate rabbitTemplate, RetryMessageHandler retryMessageHandler) {
        this.rabbitTemplate = rabbitTemplate;
        this.retryMessageHandler = retryMessageHandler;
    }

    @RabbitListener(queues = RabbitMQConfig.DLX_QUEUE, ackMode = "MANUAL")
    public void receive(Object payload, Channel channel, Message message) {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            RetryHeaderConfig headerConfig = RetryHeaderConfig.fromMessage(message);
            if (headerConfig == null) {
                retryMessageHandler.fail(payload, message, null);
                return;
            }

            log.warn("死信队列开始重试。origin-routing-key={}, retry={}/{}.",
                     headerConfig.getOriginalRoutingKey(),
                     headerConfig.getRetryCount(),
                     headerConfig.getMaxRetry()
            );
            rabbitTemplate.convertAndSend(headerConfig.getOriginalExchange(),
                                          headerConfig.getOriginalRoutingKey(),
                                          payload,
                                          msg -> {
                                              Map<String, Object> newHeaders = msg.getMessageProperties().getHeaders();
                                              newHeaders.putAll(message.getMessageProperties().getHeaders());
                                              headerConfig.fillHeaders(newHeaders);
                                              return msg;
                                          }
            );
            channel.basicAck(deliveryTag, false);
        } catch (Exception ex) {
            retryMessageHandler.fail(payload, message, ex);
        }
    }
}
