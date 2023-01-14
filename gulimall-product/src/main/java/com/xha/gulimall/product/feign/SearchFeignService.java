package com.xha.gulimall.product.feign;

import com.xha.gulimall.common.to.es.SpuInfoES;
import com.xha.gulimall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient("gulimall-search")
@Service
public interface SearchFeignService {
    /**
     * 上传sku信息
     *
     * @param upProducts 了产品
     * @return {@link R}
     */
    @PostMapping("/search/save/product")
    public boolean upProduct(@RequestBody List<SpuInfoES> upProducts);
}
