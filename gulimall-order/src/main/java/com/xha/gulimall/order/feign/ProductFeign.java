package com.xha.gulimall.order.feign;

import com.xha.gulimall.common.to.SpuInfoTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("gulimall-product")
public interface ProductFeign {
    /**
     * 根据skuId得到spu信息
     *
     * @param skuId sku id
     * @return {@link SpuInfoTO}
     */
    @GetMapping("/product/spuinfo/{skuId}")
    public SpuInfoTO getSpuInfo(@PathVariable("skuId") Long skuId);
}
