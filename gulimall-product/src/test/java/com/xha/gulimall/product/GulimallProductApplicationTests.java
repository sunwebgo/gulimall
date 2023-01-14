package com.xha.gulimall.product;

import com.xha.gulimall.common.constants.NumberConstants;
import com.xha.gulimall.product.entity.SpuInfoEntity;
import com.xha.gulimall.product.service.SpuInfoService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@Slf4j
@SpringBootTest
class GulimallProductApplicationTests {

    @Resource
    private SpuInfoService spuInfoService;

    @Test
    void contextLoads() {
        String catelogId = "0";
        log.info("结果是：" + catelogId.equals(String.valueOf(NumberConstants.ZERO)));
    }

    @Test
    void test1(){
        SpuInfoEntity spuInfoEntity = new SpuInfoEntity();
        spuInfoEntity.setSpuName("testing");
        spuInfoEntity.setId(24L);
        spuInfoService.updateById(spuInfoEntity);
    }

}
