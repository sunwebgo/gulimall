package com.xha.gulimall.seckill.config;

import com.xha.gulimall.common.constants.rabbitmq.order.OrderRmConstants;
import com.xha.gulimall.common.constants.rabbitmq.seckill.SeckillRmConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author Xu Huaiang
 * @date 2023/02/15
 */
@Configuration
public class RabbitMQConfig {


    @Bean
    public Queue orderSeckillOrderQueue() {
        return new Queue(SeckillRmConstants.ORDER_SECKILL_ORDER_QUEUE,
                true,
                false,
                false);
    }


    @Bean
    public Binding orderSeckillOrderBinding() {
        return new Binding(SeckillRmConstants.ORDER_SECKILL_ORDER_QUEUE,
                Binding.DestinationType.QUEUE,
                OrderRmConstants.ORDER_EVENT_EXCHANGE,
                SeckillRmConstants.ORDER_SECKILL_ORDER_BINDING,
                null);
    }

}

