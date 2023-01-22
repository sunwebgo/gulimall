package com.xha.gulimall.search.controller;

import com.xha.gulimall.common.to.es.SkuInfoES;
import com.xha.gulimall.common.utils.R;
import com.xha.gulimall.search.service.ElasticSaveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;

@Slf4j
@RequestMapping("/search/save")
@RestController
public class ElasticSaveController {

    @Resource
    private ElasticSaveService elasticSaveService;

    /**
     * 上传sku信息
     *
     * @param upProducts 了产品
     * @return {@link R}
     */
    @PostMapping("/product")
    public boolean upProduct(@RequestBody List<SkuInfoES> upProducts) throws IOException {
        return elasticSaveService.upProduct(upProducts);
    }
}
