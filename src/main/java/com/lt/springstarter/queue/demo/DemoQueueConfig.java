package com.lt.springstarter.queue.demo;

import com.lt.springstarter.config.RabbitMQConfig;
import com.lt.springstarter.config.RabbitRetryProperties;
import com.lt.springstarter.queue.AbstractQueueConfig;
import com.lt.springstarter.queue.RabbitRetryRegistry;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
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
    protected RabbitRetryProperties.QueueRetryConfig queueRetryConfig() {
        return buildRetryConfig(2, Arrays.asList(5000L, 10000L), true);
    }
}

