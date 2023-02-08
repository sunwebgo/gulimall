package com.xha.gulimall.ware.pojo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class SkuWareIdList {

    /**
     * sku id
     */
    private Long skuId;

    /**
     * 仓库id
     */
    private List<Long> wareId;

    /**
     * 商品数量
     */
    private Integer count;
}
