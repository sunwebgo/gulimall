package com.xha.gulimall.ware.feign;

import com.xha.gulimall.common.to.order.OrderTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Service
@FeignClient("gulimall-order")
public interface OrderFeignService {
    /**
     * 通过id获取订单
     *
     * @param orderSn 订单sn
     * @return {@link OrderTO}
     */
    @GetMapping("/order/order/getOrderById/{orderSn}")
    public OrderTO getOrderById(@PathVariable("orderSn") String orderSn);
}
