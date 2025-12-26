package com.lt.springstarter.queue.demo;

import com.lt.springstarter.queue.retry.RetryMessageHandler;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Component
@RequiredArgsConstructor
public class DemoConsumer {

    private final RetryMessageHandler retryMessageHandler;

    @RabbitListener(queues = DemoQueueConfig.QUEUE_NAME, ackMode = "MANUAL")
    public void receive(DemoProducer.DemoMessage demoMessage, Channel channel, Message message) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            handleBusinessMessage();
            channel.basicAck(deliveryTag, false);
        } catch (Exception ex) {
            log.error("{}消费者处理失败，开始延迟重试。payload={}", DemoQueueConfig.QUEUE_NAME, demoMessage, ex);
            retryMessageHandler.retry(message, ex);
            channel.basicAck(deliveryTag, false);
        }
    }

    private void handleBusinessMessage() {
        // 模拟业务随机失败，验证通用重试与失败队列链路
        if (ThreadLocalRandom.current().nextBoolean()) {
            throw new IllegalStateException("随机失败，触发重试测试");
        }
        log.info("{}：消费成功", DemoQueueConfig.QUEUE_NAME);
    }
}
