package com.lt.springstarter.queue.demo;

import com.lt.springstarter.config.RabbitMQConfig;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class DemoProducer {
    private final RabbitTemplate rabbitTemplate;

    public DemoProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendMessage(DemoMessage message) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.NORMAL_EXCHANGE_NAME, DemoQueueConfig.DEMO_ROUTING_KEY, message);
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DemoMessage {
        @NonNull
        private String message;
    }
}
