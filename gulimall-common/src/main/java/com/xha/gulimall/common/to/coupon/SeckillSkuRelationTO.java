package com.xha.gulimall.common.to.coupon;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.xha.gulimall.common.to.product.SkuInfoTO;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Accessors(chain = true)
public class SeckillSkuRelationTO {
    /**
     * id
     */
    @TableId
    private Long id;
    /**
     * 活动id
     */
    private Long promotionId;
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
     * 秒杀总量
     */
    private Integer seckillCount;
    /**
     * 每人限购数量
     */
    private Integer seckillLimit;
    /**
     * 排序
     */
    private Integer seckillSort;

    /**
     * sku的详细信息
     */
    private SkuInfoTO skuInfoTO;

    /**
     * 当前秒杀的开始时间
     */
    private Date startTime;

    /**
     * 当前秒杀的结束时间
     */
    private Date endTime;

    /**
     * 随机码
     */
    private String randomCode;
}
