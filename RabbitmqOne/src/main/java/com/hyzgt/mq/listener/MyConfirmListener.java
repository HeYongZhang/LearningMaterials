package com.hyzgt.mq.listener;

import com.rabbitmq.client.ConfirmListener;

import java.io.IOException;

/**
 * 实现ConfirmListener接口，用于监听Exchange,并不能保证消息进入Queue
 * ConfirmListener使用前提是在confirm模式下才能生效
 */
public class MyConfirmListener implements ConfirmListener {

    @Override
    public void handleAck(long l, boolean b) throws IOException {
        System.out.println("handleAck{l:"+l+",b:"+b+"}");
    }

    @Override
    public void handleNack(long l, boolean b) throws IOException {
        System.out.println("handleNack{l:"+l+",b:"+b+"}");
    }
}
