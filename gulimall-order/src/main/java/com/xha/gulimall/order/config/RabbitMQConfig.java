package com.xha.gulimall.order.config;

import com.xha.gulimall.common.constants.rabbitmq.order.OrderRmConstants;
import com.xha.gulimall.common.constants.rabbitmq.ware.WareRmConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * RabbitMQ创建queue、exchange、binding
 *
 * @author Xu Huaiang
 * @date 2023/02/10
 */
@Configuration
public class RabbitMQConfig {

    @Bean
    public Queue orderDelayQueue() {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put(OrderRmConstants.X_DEAD_LETTER_EXCHANGE, OrderRmConstants.ORDER_EVENT_EXCHANGE);
        arguments.put(OrderRmConstants.X_DEAD_LETTER_ROUTING_KEY, OrderRmConstants.ORDER_RELEASE_ORDER_BINDING);
        arguments.put(OrderRmConstants.X_MESSAGE_TTL, 6000);
        return new Queue(OrderRmConstants.ORDER_DELAY_QUEUE,
                true,
                false,
                false,
                arguments);
    }

    @Bean
    public Queue orderReleaseOrderQueue() {
        return new Queue(OrderRmConstants.ORDER_RELEASE_ORDER_QUEUE,
                true,
                false,
                false);
    }

    @Bean
    public Exchange orderEventExchange() {
        return new TopicExchange(OrderRmConstants.ORDER_EVENT_EXCHANGE,
                true,
                false);
    }

    @Bean
    public Binding orderCreateOrderBinding() {
        return new Binding(OrderRmConstants.ORDER_DELAY_QUEUE,
                Binding.DestinationType.QUEUE,
                OrderRmConstants.ORDER_EVENT_EXCHANGE,
                OrderRmConstants.ORDER_CREATE_ORDER_BINDING,
                null);
    }

    @Bean
    public Binding orderReleaseOrderBinding() {
        return new Binding(OrderRmConstants.ORDER_RELEASE_ORDER_QUEUE,
                Binding.DestinationType.QUEUE,
                OrderRmConstants.ORDER_EVENT_EXCHANGE,
                OrderRmConstants.ORDER_RELEASE_ORDER_BINDING,
                null);
    }

    /**
     * 当下单成功后，订单向库存服务的库存解锁队列绑定，发送消息
     *
     * @return {@link Binding}
     */
    @Bean
    public Binding orderWareBinding() {
        return new Binding(WareRmConstants.STOCK_RELEASE_STOCK_QUEUE,
                Binding.DestinationType.QUEUE,
                OrderRmConstants.ORDER_EVENT_EXCHANGE,
                OrderRmConstants.ORDER_RELEASE_OTHER_BINDING,
                null);
    }


}
