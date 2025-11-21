package com.lt.springstarter.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String NORMAL_EXCHANGE_NAME = "exchange.spring-starter.normal";

    // demo
    public static final String DEMO_QUEUE_NAME = "queue.spring-starter.run_task";
    public static final String DEMO_ROUTING_KEY = "key.spring-starter.run_task";


    // 死信队列
    public static final String DLX_EXCHANGE_NAME = "dlx.spring-starter.exchange";
    public static final String DLX_QUEUE = "dlx.spring-starter.queue";
    public static final String DLX_ROUTING_KEY = "dlx.spring-starter.key";

    // 任务优先级
    public static final int PRIORITY_HIGH = 5;  // 高优先级
    public static final int PRIORITY_NORMAL = 2; // 默认优先级

    @Bean
    public Queue taskQueue() {
        return QueueBuilder.durable(DEMO_QUEUE_NAME).withArgument("x-dead-letter-exchange", DLX_EXCHANGE_NAME) // 指定死信交换机
                .withArgument("x-dead-letter-routing-key", DLX_ROUTING_KEY) // 死信路由键
                .withArgument("x-max-priority", PRIORITY_HIGH) // 设置队列最大优先级为高优先级
                .build();
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // ===== demo队列 =====
    @Bean
    public DirectExchange demoExchange() {
        return new DirectExchange(NORMAL_EXCHANGE_NAME);
    }

    @Bean
    public Binding demoBinding() {
        return BindingBuilder.bind(taskQueue()).to(demoExchange()).with(DEMO_ROUTING_KEY);
    }


    // ===== 死信队列 =====
    @Bean
    public Queue dlxQueue() {
        return QueueBuilder.durable(DLX_QUEUE).build();
    }

    @Bean
    public DirectExchange dlxExchange() {
        return new DirectExchange(DLX_EXCHANGE_NAME);
    }

    @Bean
    public Binding dlxBinding() {
        return BindingBuilder.bind(dlxQueue()).to(dlxExchange()).with(DLX_ROUTING_KEY);
    }
}
