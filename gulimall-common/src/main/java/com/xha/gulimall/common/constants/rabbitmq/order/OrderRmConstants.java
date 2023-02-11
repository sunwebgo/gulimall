package com.xha.gulimall.common.constants.rabbitmq.order;

public class OrderRmConstants {

    /**
     * exchange
     */
    public static final String ORDER_EVENT_EXCHANGE = "order-event-exchange";

    /**
     * queue
     */
    public static final String ORDER_DELAY_QUEUE = "order.delay.queue";

    public static final String ORDER_RELEASE_ORDER_QUEUE = "order.release.order.queue";

    /**
     * routing-key
     */
    public static final String ORDER_CREATE_ORDER_BINDING = "order.create.order";

    public static final String ORDER_RELEASE_ORDER_BINDING = "order.release.order";

    public static final String ORDER_RELEASE_OTHER_BINDING = "order.release.other.#";

    /**
     * arguments
     */
    public static final String X_DEAD_LETTER_EXCHANGE = "x-dead-letter-exchange";

    public static final String X_DEAD_LETTER_ROUTING_KEY = "x-dead-letter-routing-key";

    public static final String X_MESSAGE_TTL = "x-message-ttl";
}
