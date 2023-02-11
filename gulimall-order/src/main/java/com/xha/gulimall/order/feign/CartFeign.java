package com.xha.gulimall.order.feign;

import com.xha.gulimall.common.to.cart.CartInfoTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient("gulimall-cart")
public interface CartFeign {
    /**
     * 获取用户购物车条目
     *
     * @return {@link List}<{@link CartInfoTO}>
     */
    @GetMapping("/userCartItems")
    public List<CartInfoTO> getUserCartItems();
}
