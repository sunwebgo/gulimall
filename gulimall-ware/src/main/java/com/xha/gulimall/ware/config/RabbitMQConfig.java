package com.xha.gulimall.ware.config;

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
    public Exchange stockEventExchange() {
        return new TopicExchange(WareRmConstants.STOCK_EVENT_EXCHANGE,
                true,
                false);
    }

    @Bean
    public Queue stockDelayQueue() {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put(WareRmConstants.X_DEAD_LETTER_EXCHANGE, WareRmConstants.STOCK_EVENT_EXCHANGE);
        arguments.put(WareRmConstants.X_DEAD_LETTER_ROUTING_KEY, WareRmConstants.STOCK_RELEASE_DEAD_BINDING);
        arguments.put(WareRmConstants.X_MESSAGE_TTL, 12000);
        return new Queue(WareRmConstants.STOCK_DELAY_QUEUE,
                true,
                false,
                false,
                arguments);
    }

    @Bean
    public Queue stockReleaseStockQueue() {
        return new Queue(WareRmConstants.STOCK_RELEASE_STOCK_QUEUE,
                true,
                false,
                false);
    }

    @Bean
    public Binding stockLockedBinding() {
        return new Binding(WareRmConstants.STOCK_DELAY_QUEUE,
                Binding.DestinationType.QUEUE,
                WareRmConstants.STOCK_EVENT_EXCHANGE,
                WareRmConstants.STOCK_LOCKED_BINDING,
                null);
    }

    @Bean
    public Binding stockReleaseBinding() {
        return new Binding(WareRmConstants.STOCK_RELEASE_STOCK_QUEUE,
                Binding.DestinationType.QUEUE,
                WareRmConstants.STOCK_EVENT_EXCHANGE,
                WareRmConstants.STOCK_RELEASE_BINDING,
                null);
    }



}
