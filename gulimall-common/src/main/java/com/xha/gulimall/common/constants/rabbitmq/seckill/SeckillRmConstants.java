package com.xha.gulimall.common.constants.rabbitmq.seckill;

public class SeckillRmConstants {
    /**
     * 交换机
     */
    public static final String ORDER_EVENT_EXCHANGE = "order-event-exchange";

    /**
     * 队列
     */
    public static final String ORDER_SECKILL_ORDER_QUEUE = "order.seckill.order.queue";

    /**
     * routing-key
     */
    public static final String ORDER_SECKILL_ORDER_BINDING = "order.seckill.order";
}
