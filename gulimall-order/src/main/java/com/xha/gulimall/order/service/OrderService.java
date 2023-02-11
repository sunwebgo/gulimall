package com.xha.gulimall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xha.gulimall.common.to.order.OrderTO;
import com.xha.gulimall.common.utils.PageUtils;
import com.xha.gulimall.order.dto.OrderSubmitDTO;
import com.xha.gulimall.order.entity.OrderEntity;
import com.xha.gulimall.order.vo.OrderConfirmVO;
import com.xha.gulimall.order.vo.SubmitOrderResponseVO;

import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * 订单
 *
 * @author XuHuaianag
 * @email 2533694604@qq.com
 * @date 2022-12-29 16:45:50
 */
public interface OrderService extends IService<OrderEntity> {

    PageUtils queryPage(Map<String, Object> params);

    OrderConfirmVO confirmOrder() throws ExecutionException, InterruptedException;

    SubmitOrderResponseVO submitOrder(OrderSubmitDTO orderSubmitDTO);

    OrderTO getOrderById(String orderSn);

    void closeOrder(OrderEntity orderEntity);
}

