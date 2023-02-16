package com.xha.gulimall.order.listener;

import com.rabbitmq.client.Channel;
import com.xha.gulimall.common.to.seckill.SeskillOrderTO;
import com.xha.gulimall.order.entity.OrderEntity;
import com.xha.gulimall.order.service.OrderService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;

@Component
public class OrderSeckillListener {

    @Resource
    private OrderService orderService;

    @RabbitListener(queues = {"order.seckill.order.queue"})
    public void seckillOrder(SeskillOrderTO seskillOrderTO, Message message, Channel channel) throws IOException {
        try {
//            创建秒杀订单
            orderService.createSeckillOrder(seskillOrderTO);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        }catch (Exception e){
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }
    }
}
