package com.xha.gulimall.order.vo;

import com.sun.org.apache.xpath.internal.operations.Bool;
import com.xha.gulimall.common.to.ReceiveAddressTO;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class OrderConfirmVO {

    /**
     * 收货地址列表
     */
    private List<ReceiveAddressTO> address;

    /**
     * 购物项
     */
    private List<OrderItemVO> items;

    /**
     * 用户积分
     */
    private Integer integration;

    /**
     * 订单总额
     */
    private BigDecimal totalPrice;

    /**
     * 支付价格
     */
    private BigDecimal payPrice;

    /**
     * 订单号
     */
    private String orderToken;

    /**
     * 库存
     */
    private Map<Long, Boolean> stocks;


    /**
     * 商品数量
     */
    private Integer count = 0;
}
