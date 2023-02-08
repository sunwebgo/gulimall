package com.xha.gulimall.order.feign;

import com.xha.gulimall.common.to.WareSkuLockTO;
import com.xha.gulimall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("gulimall-ware")
public interface WareFeign {
    /**
     * 锁定库存
     *
     * @param wareSkuLockTO 器皿sku锁
     * @return {@link R}
     */
    @PostMapping("/ware/waresku/lock/sku")
    public R wareSkuLock(@RequestBody WareSkuLockTO wareSkuLockTO);
}
