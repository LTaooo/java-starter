package com.lt.springstarter.queue.demo;

import com.lt.springstarter.queue.retry.RabbitRetryRegistry;
import com.lt.springstarter.queue.retry.RetryHeaderConfig;
import com.lt.springstarter.queue.retry.RetryMessageHandler;
import lombok.*;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DemoProducer {
    private final RabbitRetryRegistry rabbitRetryRegistry;
    private final RetryMessageHandler retryMessageHandler;


    public void sendMessage(DemoMessage message) {
        RetryHeaderConfig config = rabbitRetryRegistry.get(DemoQueueConfig.QUEUE_NAME);
        retryMessageHandler.push(DemoQueueConfig.QUEUE_NAME,
                config.getOriginalExchange(),
                config.getOriginalRoutingKey(),
                message
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
