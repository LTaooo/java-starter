package com.lt.springstarter.queue.demo;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Component
public class DemoConsumer {


    @RabbitListener(queues = DemoQueueConfig.DEMO_QUEUE_NAME, ackMode = "MANUAL")
    public void receive(DemoProducer.DemoMessage demoMessage, Channel channel, Message message) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        // 模拟业务随机失败，验证通用重试与失败队列链路
        // if (ThreadLocalRandom.current().nextBoolean()) {
        //     throw new IllegalStateException("随机失败，触发重试测试");
        // }
        channel.basicAck(deliveryTag, false);
    }
}
