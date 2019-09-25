package com.hyzgt.mq;

import com.hyzgt.mq.listener.MyConfirmListener;
import com.hyzgt.mq.listener.MyReturnListener;
import com.hyzgt.mq.util.ConnectionUtils;
import com.hyzgt.mq.util.ExchangeType;
import com.rabbitmq.client.*;

import java.io.IOException;

/**
 * @program: RabbitmqOne
 * @description:
 * @author: Hyz.gt
 * @create: /09/22  18:11
 *
 *
 * Exchange的四总类型：direct、fanout、topic、headers
 * direct:匹配RoutingKey完全相等的Queue
 * topic:匹配RoutingKey，支持使用通配符, '.#.'  代表匹配 没有字符或者多个字符，'.*.' 代表匹配一个字符
 **/
public class Main {

    private static final String  QUEUE_NAME = "test_queue_first";

    private static final String EXCHANGE_NAME = "test_exchange_topic";

    private static final String RoutingKey = "before.first";

    public static void main(String[] agrs) throws IOException, InterruptedException {

        Connection connection = ConnectionUtils.getConnection();
        //获取队列
        Channel channel = connection.createChannel();

        /**
         * confirm是RabbitMQ的一种事物解决方案，已经在transaction事物模式的channel是不能在设置成confirm模式,两者不能共存
         *
         * Channel.confirmSelect()方法将Channel信道设置成confirm模式。一旦信道被设置成confirm模式，该信道上的所有消息都会被指派一个唯一的ID（从1开始），
         * 一旦消息被对应的Exchange接收，Broker就会发送一个确认给生产者（其中deliveryTag就是此唯一的ID）
         *
         * 普通confirm模式是串行的，即每次发送了一次消息，生产者都要等待Broker的确认消息，然后根据确认标记权衡消息重发还是继续发下一条。由于是串行的，在效率上是比较低下的。
         *
         * 发送消息采用 channel.waitForConfirms() 等待Broker服务返回消息标记，如果发送失败会重新发送，串行发送
         * channel.waitForConfirmsOrDie(),批量发送消息，如果发送失败会重新发送，频繁失败性能不增反降
         *
         */
        channel.confirmSelect();

        /**
         * 实现ConfirmListener接口，用于监听Exchange,并不能保证消息进入Queue
         * ConfirmListener使用前提是在confirm模式下才能生效
         */
        channel.addConfirmListener(new MyConfirmListener());


        /**
         * 监听消息是否发送到了队列，返回响应信息
         * ReturnListener使用前提是在channel.basicPublish()时，设置mandatory=true
         */
        channel.addReturnListener(new MyReturnListener());


        /**
         *  创建队列Queue-----消息的载体,每个消息都会被投到一个或多个队列
         *
         *  param1            队列名称
         *  param2            持久性：true队列会再重启过后存在，但是其中的消息不会存在
         *  param3            是否只能由创建者使用，其他连接不能使用
         *  param4            是否自动删除（没有连接自动删除）
         *  param5            队列的其他属性(构造参数)
         */
        channel.queueDeclare(QUEUE_NAME,true,false,false,null);

        /**
         *  声明Exchange------消息交换机,它指定消息按什么规则,路由到哪个队列
         *
         *  param1            队列名称
         *  param2            Exchange的类型,可选择direct、fanout、topic、headers
         *  param3            持久性
         *  param4            是否只能由创建者使用，其他连接不能使用
         *  param5            是否自动删除（没有连接自动删除）
         *  param6            队列的其他属性(构造参数)
         */
        channel.exchangeDeclare(EXCHANGE_NAME, ExchangeType.Topic,true,false,false,null);


        /**
         *  将Queue和Exchange进行绑定（Binding）------ 它的作用就是把Exchange和Queue按照路由规则绑定起来.
         *
         *  param1            队列Queue名称
         *  param2            消息交换机Exchange名称
         *  param3            路由关键字,exchange根据这个关键字进行消息投递
         *  param4            队列的其他属性(构造参数)
         */
        channel.queueBind(QUEUE_NAME,EXCHANGE_NAME,RoutingKey,null);

        /**
         *  推送消息.
         *
         *  param1            消息交换机Exchange名称
         *  param2            路由关键字
         *  param3            监听是否有符合的队列等同于mandatory=true/false,true(支持ReturnListener)
         *  param4            监听符合的队列上是有至少一个Consumer
         *  param5            设置消息持久化   MessageProperties.PERSISTENT_TEXT_PLAIN是持久化；MessageProperties.TEXT_PLAIN是非持久化
         *  param6            消息内容
         */
        for (int i = 0; i <10000 ;i++) {
            String messgae = "i="+i;
            channel.basicPublish(EXCHANGE_NAME,RoutingKey,true,false, MessageProperties.TEXT_PLAIN,messgae.getBytes());
           /* if(channel.waitForConfirms()){
                System.out.println(messgae+"-----【SUCCES】");
            }else{
                System.out.println(messgae+"------【ERROR】");
            }*/
        }
        channel.waitForConfirmsOrDie();

        /**
         *  basicQos：指该消费者在接收的队列里的消息，但没有应答结果之前，队列不会在发送消息给消费者
         *
         *  param1            ${param2}容纳最大的数量，
         *  param2            true代表通道chanel，false代表消费者consumer
         *
         *  下面代表的意思是：允许消费者在同一时刻下支撑10个消息
         */
        channel.basicQos(10,false);
        channel.basicQos(15,true);

        /**
         * DefaultConsumer:默认消费者，处理所有ID
         *
         *  接收消息 channel.basicConsume(param1,param2,param3);函数，重写Consumer接收消息
         *
         *  param1            队列Queue名称
         *  param2            true：自动应答   false:手动应答
         *  param3            消费者
         *
         *  手动应答channel.basicAck(param1,param2),应答成功后RabbitMQ会从队列的内存中删除该条消息,一般表示成功
         * param1             唯一ID
         * param2             true:表示小于ID的一起应答  false:只应答当前ID
         *
         *  手动应答channel.basicNack(param1,param2,param3),一般表示应答失败
         * param1             唯一ID
         * param2             true:当前ID以及小于ID   false:当前ID
         * param3             true:Queue保留ID对应的消息  false:Queue丢弃ID对应的消息    基于 param2 的条件决定丢弃还是保留哪些ID
         *
         * 手动应答channel.basicReject(param1,param2),没有批量操作之外，类似basicNack 一般表示应答失败
         * param1             唯一ID
         * param3             true:Queue保留ID对应的消息  false:Queue丢弃ID对应的消息
         *
         */
        Thread thread1 = new Thread(()->{
            Consumer consumer = new DefaultConsumer(channel){
                /**
                 *  收到消息的回调函数
                 * @param consumerTag
                 * @param envelope
                 * @param properties
                 * @param body
                 * @throws IOException
                 */
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    String mes = new String(body, "utf-8");
                    System.out.println(mes);
                //    channel.basicAck(envelope.getDeliveryTag(),false);
                //    channel.basicNack(envelope.getDeliveryTag(),false,false);
                    channel.basicReject(envelope.getDeliveryTag(),false);
                }


                /**
                 * 消费者注册成功的回调函数
                 * @param consumerTag
                 */
                @Override
                public void handleConsumeOk(String consumerTag) {
                    System.out.println("消费者注册成功的回调函数"+consumerTag);
                }

                /**
                 * 手动取消消费者注册成功的回调函数
                 * @param consumerTag
                 */
                @Override
                public void handleCancelOk(String consumerTag) {
                    System.out.println("手动取消消费者注册成功的回调函数"+consumerTag);
                }

                /**
                 * 当消费者因为其他原因被动取消注册时调用，比如queue被删除了。
                 * @param consumerTag
                 * @throws IOException
                 */
                @Override
                public void handleCancel(String consumerTag) throws IOException {
                    System.out.println("当消费者因为其他原因被动取消注册时调用，比如queue被删除了"+consumerTag);
                }

                /**
                 * 当通道或者基础连接被关闭时调用
                 * @param consumerTag
                 * @param sig
                 */
                @Override
                public void handleShutdownSignal(String consumerTag, ShutdownSignalException sig) {
                    System.out.println("当通道或者基础连接被关闭时调用"+consumerTag);
                }

                /**
                 * 收回OK
                 * @param consumerTag
                 */
                @Override
                public void handleRecoverOk(String consumerTag) {
                    System.out.println("收回OK"+consumerTag);
                }
            };
            try {
                channel.basicConsume(QUEUE_NAME,false,consumer);
                Thread.sleep(20000);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        },"Thread-Customs");
        thread1.start();
        thread1.join();

        channel.close();
        connection.close();
    }
}
