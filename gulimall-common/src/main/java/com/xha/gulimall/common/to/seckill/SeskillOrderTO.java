package com.xha.gulimall.common.to.seckill;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Data
@Accessors(chain = true)
public class SeskillOrderTO {

    /**
     * 用户id
     */
    private Long memberId;
    /**
     * 订单号
     */
    private String OrderSn;
    /**
     * 活动场次id
     */
    private Long promotionSessionId;
    /**
     * 商品id
     */
    private Long skuId;
    /**
     * 秒杀价格
     */
    private BigDecimal seckillPrice;
    /**
     * 购买数量
     */
    private Integer num;


}
