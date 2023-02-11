package com.xha.gulimall.common.to.order;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class OrderItemTO {

    /**
     * sku id
     */
    private Long skuId;

    /**
     * 锁定数量
     */
    private Integer count;
}
