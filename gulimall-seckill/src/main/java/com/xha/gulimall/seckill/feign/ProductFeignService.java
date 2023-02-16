package com.xha.gulimall.seckill.feign;

import com.xha.gulimall.common.to.product.SkuInfoTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient("gulimall-product")
public interface ProductFeignService {
    /**
     * 得到所有sku信息列表
     *
     * @return {@link List}<{@link SkuInfoTO}>
     */
    @GetMapping("/product/skuinfo/getAllSkuInfoList")
    List<SkuInfoTO> getAllSkuInfoList();
}
