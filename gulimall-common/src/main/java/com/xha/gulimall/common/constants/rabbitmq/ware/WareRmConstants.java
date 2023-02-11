package com.xha.gulimall.common.constants.rabbitmq.ware;

import lombok.Data;

@Data
public class WareRmConstants {

    /**
     * 队列
     */
    public static final String STOCK_DELAY_QUEUE = "stock.delay.queue";

    public static final String STOCK_RELEASE_STOCK_QUEUE = "stock.release.stock.queue";


    /**
     * 交换机
     */
    public static final String STOCK_EVENT_EXCHANGE =  "stock-event-exchange";

    /**
     * routing-key
     */
    public static final String STOCK_LOCKED_BINDING = "stock.locked";

    public static final String STOCK_RELEASE_DEAD_BINDING = "stock.release";

    public static final String STOCK_RELEASE_BINDING = "stock.release.#";

    /**
     * arguments
     */
    public static final String X_DEAD_LETTER_EXCHANGE = "x-dead-letter-exchange";

    public static final String X_DEAD_LETTER_ROUTING_KEY = "x-dead-letter-routing-key";

    public static final String X_MESSAGE_TTL = "x-message-ttl";

}
