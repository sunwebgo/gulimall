package com.xha.gulimall.product.feign;

import com.xha.gulimall.common.to.product.SkuReductionTO;
import com.xha.gulimall.common.to.product.SpuBoundTO;
import com.xha.gulimall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Service
@FeignClient("gulimall-coupon")
public interface CouponFeignService {
    /**
     * 保存spu的积分信息
     */
    @PostMapping("/coupon/spubounds/save")
    R saveSpuBound(@RequestBody SpuBoundTO spuBoundTO);


    @PostMapping("/coupon/skufullreduction/saveskureduction")
    R saveSkuReduction(@RequestBody SkuReductionTO skuReductionTO);
}
