package com.xha.gulimall.order;

import com.xha.gulimall.order.entity.OrderEntity;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@Slf4j
@SpringBootTest
class GulimallOrderApplicationTests {

    @Resource
    private AmqpAdmin amqpAdmin;

    @Resource
    private RabbitTemplate rabbitTemplate;

    /**
     * 创建交换机
     */
    @Test
    void createExchange() {
        DirectExchange directExchange = new DirectExchange("direct-exchange", true, false);
        amqpAdmin.declareExchange(directExchange);
        log.info("direct-exchange交换机创建成功");
    }

    /**
     * 创建队列
     */
    @Test
    void createQueue() {
        Queue queue = new Queue("direct-queue", true, false, false);
        amqpAdmin.declareQueue(queue);
    }

    /**
     * 创建绑定
     */
    @Test
    void createBinding() {
        Binding binding = new Binding("direct-queue",
                Binding.DestinationType.QUEUE,
                "direct-exchange", "hello.rabbitmq",
                null);
        amqpAdmin.declareBinding(binding);
    }

    /**
     * 发送消息
     */
    @Test
    void sendMessage() {
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setMemberId(521521L);
        orderEntity.setOrderSn("123456");
        orderEntity.setMemberUsername("张三");

        rabbitTemplate.convertAndSend("direct-exchange",
                "hello.rabbitmq",
                orderEntity);
    }





}
