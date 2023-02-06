package com.xha.gulimall.order.controller;

import cn.hutool.core.lang.UUID;
import com.xha.gulimall.order.entity.OrderEntity;
import com.xha.gulimall.order.entity.OrderItemEntity;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
public class RabbitController {

    @Resource
    private RabbitTemplate rabbitTemplate;

    @GetMapping("/sendmessage")
    public String sendMessage(){
        for (int i = 0; i < 10; i++) {
            if (i % 2 == 0){
                OrderItemEntity orderItemEntity = new OrderItemEntity();
                orderItemEntity.setSkuName("华为 Mate50 Pro -->" + i);
                rabbitTemplate.convertAndSend("direct-exchange","hello.rabbitmq",orderItemEntity,new CorrelationData(UUID.randomUUID().toString()));
            }else{
                OrderEntity orderEntity = new OrderEntity();
                orderEntity.setMemberUsername("张三-->" + i);
                rabbitTemplate.convertAndSend("direct-exchange","hello.rabbitmq",orderEntity,new CorrelationData(UUID.randomUUID().toString()));
            }
        }
        return "消息发送成功";
    }
}
