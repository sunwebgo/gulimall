package com.xha.gulimall.order.service.impl;

import com.rabbitmq.client.Channel;
import com.xha.gulimall.order.entity.OrderEntity;
import com.xha.gulimall.order.entity.OrderItemEntity;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RabbitListener(queues = {"direct-queue"})
public class RabbitServiceImpl {

    /**
     * 接收消息
     */
    @RabbitHandler
    public void receiveMessage1(Message message,OrderItemEntity orderItemEntity, Channel channel){
        System.out.println("消息体对象：OrderItemEntity");
//        deliveryTag在当前通道内是自增的
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            if (deliveryTag % 2 == 0){
                channel.basicAck(deliveryTag,false);
                System.out.println("收到了消息：" + deliveryTag);
            }else{
//                第三个为true，表示消息重新入队
                channel.basicNack(deliveryTag,false,true);
                System.out.println("收到了消息：" + deliveryTag);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 接收消息
     */
    @RabbitHandler
    public void receiveMessage2(Message message,OrderEntity orderEntity,Channel channel){
        System.out.println("消息体对象：OrderEntity");
        //        deliveryTag在当前通道内是自增的
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            if (deliveryTag % 2 == 0){
                channel.basicAck(deliveryTag,false);
                System.out.println("收到了消息：" + deliveryTag);
            }else{
//                第三个为true，表示消息重新入队
                channel.basicNack(deliveryTag,false,true);
                System.out.println("收到了消息：" + deliveryTag);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
