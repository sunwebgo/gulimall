package com.xha.gulimall.common.to;

import lombok.Data;
import lombok.experimental.Accessors;


@Data
@Accessors(chain = true)
public class SkuStockTO {

    private Long skuId;

    private Boolean hasStock;
}
