package com.hyzgt.mq.listener;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.ReturnListener;

import java.io.IOException;

/**
 * @program: RabbitmqOne
 * @description:
 * @author: Hyz.gt
 * @create: /09/24  09:38
 **/
public class MyReturnListener implements ReturnListener {


    @Override
    public void handleReturn(int replyCode, String replyText, String exchange, String routingKey, AMQP.BasicProperties basicProperties, byte[] body) throws IOException {
        System.out.println("消息发送到队列失败：回复编码："+replyCode+",回复失败文本："+replyText+",接收Exchange："+exchange+",使用RoutingKey："+routingKey+",发送内容："+new String(body));
    }
}
