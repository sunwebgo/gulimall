package com.xha.gulimall.ware;

import com.xha.gulimall.ware.entity.PurchaseEntity;
import com.xha.gulimall.ware.service.PurchaseService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
class GulimallWareApplicationTests {

    @Resource
    private PurchaseService purchaseService;

    @Test
    void contextLoads() {
        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setPhone("13738239983");
        purchaseService.save(purchaseEntity);
    }

}
