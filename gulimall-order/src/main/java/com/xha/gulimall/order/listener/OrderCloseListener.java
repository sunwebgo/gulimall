package com.xha.gulimall.order.listener;

import com.rabbitmq.client.Channel;
import com.xha.gulimall.order.entity.OrderEntity;
import com.xha.gulimall.order.service.OrderService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;

@Service
public class OrderCloseListener {

    @Resource
    private OrderService orderService;


    @RabbitListener(queues = {"order.release.order.queue"})
    public void closeOrder(OrderEntity orderEntity, Message message, Channel channel) throws IOException {
        try {
//            关闭订单
            orderService.closeOrder(orderEntity);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        }catch (Exception e){
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }
    }


}
