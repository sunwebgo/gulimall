package com.xha.gulimall.seckill.feign;

import com.xha.gulimall.common.to.coupon.SeckillSessionTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@FeignClient("gulimall-coupon")
public interface CouponFeignService {

    /**
     * 查询秒杀场次
     *
     * @return {@link List}<{@link SeckillSessionTO}>
     */
    @PostMapping("/coupon/seckillsession/getSeckillSession")
    List<SeckillSessionTO> getSeckillSession();

}
