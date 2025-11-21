package com.lt.springstarter.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String NORMAL_EXCHANGE_NAME = "exchange.spring-starter.normal";

    // 死信队列
    public static final String DLX_EXCHANGE_NAME = "dlx.spring-starter.exchange";
    public static final String DLX_QUEUE = "dlx.spring-starter.queue";
    public static final String DLX_ROUTING_KEY = "dlx.spring-starter.key";

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public Declarables dlxQueue() {
        Queue queue = QueueBuilder.durable(DLX_QUEUE).build();
        DirectExchange exchange = new DirectExchange(DLX_EXCHANGE_NAME);
        Binding binding = BindingBuilder.bind(queue).to(exchange).with(DLX_ROUTING_KEY);
        return new Declarables(queue, exchange, binding);
    }
}
