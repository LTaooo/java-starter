package com.lt.springstarter.queue.dlx;

import com.lt.springstarter.config.RabbitMQConfig;
import com.lt.springstarter.queue.retry.RabbitRetryHeaders;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class DLXConsumer {
    @RabbitListener(queues = RabbitMQConfig.DLX_QUEUE, ackMode = "MANUAL")
    public void receive(Object payload, Channel channel, Message message) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            String originQueue = (String) message.getMessageProperties()
                    .getHeaders()
                    .getOrDefault(RabbitRetryHeaders.X_ORIGINAL_QUEUE, "unknown");
            log.error("失败队列收到消息，originQueue={}, payload={}, headers={}",
                    originQueue, payload, message.getMessageProperties().getHeaders());
            channel.basicAck(deliveryTag, false);
        } catch (Exception ex) {
            log.error("处理失败队列消息时发生异常，消息将重新入队。", ex);
            channel.basicNack(deliveryTag, false, true);
        }
    }
}
