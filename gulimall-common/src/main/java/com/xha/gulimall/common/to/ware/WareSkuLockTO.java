package com.xha.gulimall.common.to.ware;

import com.xha.gulimall.common.to.order.OrderItemTO;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 下单后锁定库存
 *
 * @author Xu Huaiang
 * @date 2023/02/08
 */
@Data
@Accessors(chain = true)
public class WareSkuLockTO {

    /**
     * 订单号
     */
    private String orderSn;

    /**
     * 订单项服务条款
     */
    private List<OrderItemTO> orderItemTOS;

    /**
     * 锁定状态
     */
    private boolean locked;
}
