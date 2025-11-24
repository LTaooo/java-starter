package com.lt.springstarter.queue.demo;

import com.lt.springstarter.config.RabbitMQConfig;
import com.lt.springstarter.queue.retry.AbstractQueueConfig;
import com.lt.springstarter.queue.retry.RabbitRetryRegistry;
import com.lt.springstarter.queue.retry.RetryHeaderConfig;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

/**
 * Demo 业务队列配置
 */
@Configuration
public class DemoQueueConfig extends AbstractQueueConfig {

    public static final String DEMO_QUEUE_NAME = "queue.spring-starter.demo";
    public static final String DEMO_ROUTING_KEY = "key.spring-starter.demo";

    public DemoQueueConfig(RabbitRetryRegistry rabbitRetryRegistry) {
        super(rabbitRetryRegistry);
    }

    @Bean
    public Declarables demoQueueDeclarables() {
        Queue queue = QueueBuilder.durable(DEMO_QUEUE_NAME)
                .withArgument("x-dead-letter-exchange", RabbitMQConfig.DLX_EXCHANGE_NAME)
                .withArgument("x-dead-letter-routing-key", RabbitMQConfig.DLX_ROUTING_KEY)
                .withArgument("x-max-priority", 5)
                .build();
        DirectExchange exchange = new DirectExchange(RabbitMQConfig.NORMAL_EXCHANGE_NAME);
        Binding binding = BindingBuilder.bind(queue).to(exchange).with(DEMO_ROUTING_KEY);
        return new Declarables(queue, exchange, binding);
    }

    @Override
    protected String queueName() {
        return DEMO_QUEUE_NAME;
    }

    @Override
    protected String exchangeName() {
        return RabbitMQConfig.NORMAL_EXCHANGE_NAME;
    }

    @Override
    protected String routingKey() {
        return DEMO_ROUTING_KEY;
    }

    @Override
    protected RetryHeaderConfig queueRetryConfig() {
        return buildRetryConfig(Arrays.asList(2000L, 3000L), false);
    }
}

