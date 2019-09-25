package com.hyzgt.mq.util;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;

/**
 * @program: RabbitmqOne
 * @description:
 * @author: Hyz.gt
 * @create: /09/22  17:41
 **/
public class ConnectionUtils {

    public static Connection getConnection() throws IOException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("192.168.1.84");
        factory.setPort(5673);
        factory.setUsername("admin");
        factory.setPassword("admin");
        factory.setVirtualHost("MyRabbitmq");
        return factory.newConnection();
    }
}
