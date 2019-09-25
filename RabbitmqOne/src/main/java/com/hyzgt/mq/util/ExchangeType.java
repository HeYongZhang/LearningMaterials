package com.hyzgt.mq.util;

/**
 * @program: RabbitmqOne
 * @description:
 * @author: Hyz.gt
 * @create: /09/23  14:53
 **/
public interface ExchangeType {

    String Topic = "topic";

    String Direct = "direct";

    String Fanout = "fanout";

    String Headers = "headers";


}
