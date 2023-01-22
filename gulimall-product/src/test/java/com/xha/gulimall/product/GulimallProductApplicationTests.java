package com.xha.gulimall.product;

import com.xha.gulimall.common.constants.NumberConstants;
import com.xha.gulimall.product.entity.SpuInfoEntity;
import com.xha.gulimall.product.service.SpuInfoService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Slf4j
@SpringBootTest
class GulimallProductApplicationTests {

    @Resource
    private SpuInfoService spuInfoService;

    @Resource
    private RedissonClient redissonClient;

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

    @Test
    public void test2(){
        RLock lock = redissonClient.getLock("lock");
        lock.lock(10, TimeUnit.SECONDS);
    }


}
