package com.rabbitmq.solon;

import com.rabbitmq.client.*;
import org.noear.solon.annotation.Component;

import java.io.IOException;

@Component
public class DemoMessageConsumer extends DefaultConsumer implements Consumer {

    public DemoMessageConsumer(Channel channel) {
        super(channel);
    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
        System.out.println(body);

        getChannel().basicAck(envelope.getDeliveryTag(), false);
    }
}