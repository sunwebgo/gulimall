package com.xha.gulimall.product.feign;

import com.xha.gulimall.common.to.SkuStockTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Service
@FeignClient("gulimall-ware")
public interface WareFeignService {

    /**
     * 判断库存是否存在
     *
     * @param skuIds sku id
     * @return {@link List}<{@link SkuStockTO}>
     */
    @PostMapping("/ware/waresku/hasstock")
    public List<SkuStockTO> hasStock(@RequestBody List<Long> skuIds);
}
