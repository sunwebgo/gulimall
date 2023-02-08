package com.xha.gulimall.order.vo;

import com.xha.gulimall.order.entity.OrderEntity;
import lombok.Data;

/**
 * 提交订单回答签证官
 *
 * @author Xu Huaiang
 * @date 2023/02/07
 */
@Data
public class SubmitOrderResponseVO {

    /**
     * 订单
     */
    private OrderEntity order;

    /**
     * 代码
     */
    private Integer code;
}
