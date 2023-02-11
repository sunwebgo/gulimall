package com.xha.gulimall.ware.listener;


import com.rabbitmq.client.Channel;
import com.xha.gulimall.common.to.order.OrderTO;
import com.xha.gulimall.common.to.rabbitmq.StockLockedTO;
import com.xha.gulimall.ware.service.WareSkuService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;

@Service
@RabbitListener(queues = {"stock.release.stock.queue"})
public class handleStockLockedRelease {



    @Resource
    private WareSkuService wareSkuService;

    /**
     * 释放库存
     * 1.判断库存工作单是否存在
     *      存在：
     *          2.根据工作单中的订单id查询订单
     *              存在：
     *                  3.判断订单状态
     *                      订单取消：释放库存
     *                      未取消：不需要释放
     *              不存在：
     *                  释放库存
     *      不存在：
     *          锁定库存业务失败，数据回滚，不需要释放库存
     *
     * @param stockLockedTO 股票锁定
     */

    @RabbitHandler
    public void handleStockLockedRelease(StockLockedTO stockLockedTO, Message message, Channel channel) throws IOException {

        try {
            wareSkuService. unlockedStock(stockLockedTO);
            //                释放库存后手动Ack
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
//                释放库存后手动Ack
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }
    }


    @RabbitHandler
    public void handleOrderClose(OrderTO orderTO, Message message, Channel channel) throws IOException {

        try {
            wareSkuService. unlockedStock(orderTO);
            //                释放库存后手动Ack
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
//                释放库存后手动Ack
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }
    }
}

