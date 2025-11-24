package com.lt.springstarter.queue.demo;

import com.lt.springstarter.config.RabbitMQConfig;
import com.lt.springstarter.queue.retry.RabbitRetryRegistry;
import com.lt.springstarter.queue.retry.RetryHeaderConfig;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class DemoProducer {
    private final RabbitTemplate rabbitTemplate;
    private final RabbitRetryRegistry rabbitRetryRegistry;

    public DemoProducer(RabbitTemplate rabbitTemplate, RabbitRetryRegistry rabbitRetryRegistry) {
        this.rabbitTemplate = rabbitTemplate;
        this.rabbitRetryRegistry = rabbitRetryRegistry;
    }

    public void sendMessage(DemoMessage message) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.NORMAL_EXCHANGE_NAME, DemoQueueConfig.DEMO_ROUTING_KEY, message, m -> {
                                          Map<String, Object> headers = m.getMessageProperties().getHeaders();
                                          RetryHeaderConfig config = rabbitRetryRegistry.get(DemoQueueConfig.DEMO_QUEUE_NAME);
                                          config.toInitHeaders(headers);
                                          return m;
                                      }
        );
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DemoMessage {
        @NonNull
        private String message;
    }
}
