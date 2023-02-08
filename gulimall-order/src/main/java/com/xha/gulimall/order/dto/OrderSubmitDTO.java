package com.xha.gulimall.order.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 订单提交dto
 *
 * @author Xu Huaiang
 * @date 2023/02/07
 */
@Data
public class OrderSubmitDTO {
    /**
     * 收货地 id
     */
    private Long addrId;

    /**
     * 支付类型
     */
    private String payType;

    /**
     * 订单令牌
     */
    private String orderToken;

}
