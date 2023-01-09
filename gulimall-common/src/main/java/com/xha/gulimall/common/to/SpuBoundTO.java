package com.xha.gulimall.common.to;

import lombok.Data;

import java.math.BigDecimal;

/**
 * spu优惠券TO
 *
 * @author Xu Huaiang
 * @date 2023/01/08
 */
@Data
public class SpuBoundTO {

    private Long spuId;

    private BigDecimal buyBounds;

    private BigDecimal growBounds;
}
