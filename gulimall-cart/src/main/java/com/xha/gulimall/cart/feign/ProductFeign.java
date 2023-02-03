package com.xha.gulimall.cart.feign;


import com.xha.gulimall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Service
@FeignClient("gulimall-product")
public interface ProductFeign {

    /**
     * 根据skuId查询商品的详细信息
     *
     * @param skuId sku id
     * @return {@link R}
     */
    @RequestMapping("/product/skuinfo/info/{skuId}")
    public R getSkuInfo(@PathVariable("skuId") Long skuId);

    /**
     * 根据skuId获取到销售属性列表
     *
     * @param skuId sku id
     * @return {@link List}<{@link String}>
     */
    @RequestMapping("/product/skusaleattrvalue/info/{skuId}")
//    @RequiresPermissions("product:skusaleattrvalue:info")
    public List<String> getSaleAttrBySkuId(@PathVariable("skuId") Long skuId);
}
