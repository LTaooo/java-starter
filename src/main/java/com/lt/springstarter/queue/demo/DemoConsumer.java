package com.lt.springstarter.queue.demo;

import com.lt.springstarter.queue.retry.RetryMessageHandler;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
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

    @RabbitListener(queues = DemoQueueConfig.DEMO_QUEUE_NAME, ackMode = "MANUAL")
    public void receive(DemoProducer.DemoMessage demoMessage, Channel channel, Message message) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            handleBusinessMessage(demoMessage);
            channel.basicAck(deliveryTag, false);
        } catch (Exception ex) {
            log.error("Demo 消费者处理失败，准备进入重试链路。payload={}", demoMessage, ex);
            try {
                boolean scheduled = retryMessageHandler.scheduleRetry(
                        DemoQueueConfig.DEMO_QUEUE_NAME,
                        DemoQueueConfig.DEMO_DELAY_ROUTING_KEY,
                        demoMessage,
                        message,
                        ex
                );
                if (!scheduled) {
                    retryMessageHandler.routeToFailQueue(
                            DemoQueueConfig.DEMO_QUEUE_NAME,
                            demoMessage,
                            message,
                            ex
                    );
                }
                channel.basicAck(deliveryTag, false);
            } catch (Exception handlerEx) {
                log.error("重试链路异常，消息将被重新入队避免丢失。payload={}", demoMessage, handlerEx);
                channel.basicNack(deliveryTag, false, true);
            }
        }
    }

    private void handleBusinessMessage(DemoProducer.DemoMessage demoMessage) {
        // 模拟业务随机失败，验证通用重试与失败队列链路
        if (ThreadLocalRandom.current().nextBoolean()) {
            throw new IllegalStateException("随机失败，触发重试测试");
        }
        log.info("Demo 消费成功，payload={}", demoMessage);
    }
}
