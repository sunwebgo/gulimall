package com.xha.gulimall.product.feign;

import com.xha.gulimall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("gulimall-seckill")
public interface SeckillFeignService {

    /**
     * 根据skuID查询当前商品是否参加秒杀
     *
     * @return {@link R}
     */
    @GetMapping("/sku/seckill/{skuId}")
    R getSeckillInfoBySkuId(@PathVariable("skuId") Long skuId);
}
