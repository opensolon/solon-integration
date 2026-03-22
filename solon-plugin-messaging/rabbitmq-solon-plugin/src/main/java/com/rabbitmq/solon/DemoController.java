package com.rabbitmq.solon;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Inject;
import org.noear.solon.annotation.Mapping;

import java.io.IOException;

@Controller
public class DemoController {
    @Inject
    private Channel producer;

    @Mapping("/send")
    public void send(String msg) throws IOException {
        //发送
        AMQP.BasicProperties msgProperties = new AMQP.BasicProperties();
        producer.basicPublish(RabbitmqConfig.EXCHANGE_NAME, "topic.test", msgProperties, msg.getBytes());
    }
}
