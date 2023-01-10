package com.xha.gulimall.ware.feign;

import com.xha.gulimall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Service
@FeignClient("gulimall-product")
public interface GetSkuInfoService {

    @RequestMapping("/product/skuinfo/getskuname/{skuId}")
    public String getSkuName(@PathVariable("skuId") Long skuId);
}
