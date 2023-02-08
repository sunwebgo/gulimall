package com.xha.gulimall.common.to;

import lombok.Data;
import lombok.experimental.Accessors;


@Data
@Accessors(chain = true)
public class SpuInfoTO {
    /**
     * 商品id
     */
    private Long spuId;
    /**
     * 商品名称
     */
    private String spuName;
    /**
     * 品牌id
     */
    private String spuBrand;


}
