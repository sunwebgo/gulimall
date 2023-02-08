package com.xha.gulimall.order.vo;

import com.xha.gulimall.order.entity.OrderEntity;
import com.xha.gulimall.order.entity.OrderItemEntity;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.List;

/**
 * 创建订单VO
 *
 * @author Xu Huaiang
 * @date 2023/02/07
 */
@Data
@Accessors(chain = true)
public class CreateOrderVO {

    /**
     * 订单
     */
    private OrderEntity order;

    /**
     * 订单项
     */
    private List<OrderItemEntity> orderItems;

    /**
     * 支付价格
     */
    private BigDecimal payPrice;
}
