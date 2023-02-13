package com.xha.gulimall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xha.gulimall.common.utils.PageUtils;
import com.xha.gulimall.order.entity.OrderItemEntity;

import java.util.Map;

/**
 * 订单项信息
 *
 * @author XuHuaianag
 * @email 2533694604@qq.com
 * @date 2022-12-29 16:45:50
 */
public interface OrderItemService extends IService<OrderItemEntity> {

    PageUtils queryPage(Map<String, Object> params);

    OrderItemEntity getOrderItemById(String orderSn);
}

