package com.xha.gulimall.ware;

import com.xha.gulimall.common.to.product.SkuStockTO;
import com.xha.gulimall.ware.entity.PurchaseEntity;
import com.xha.gulimall.ware.service.PurchaseService;
import com.xha.gulimall.ware.service.WareSkuService;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
class GulimallWareApplicationTests {

    @Resource
    private PurchaseService purchaseService;

    @Resource
    private WareSkuService wareSkuService;


    @Test
    void contextLoads() {
        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setPhone("13738239983");
        purchaseService.save(purchaseEntity);
    }

    @Test
    void test1(){
        List<Long> idList = new ArrayList<>();
        idList.add(801204740L);
        idList.add(801204640L);
        idList.add(801204340L);
        idList.add(801204540L);
        idList.add(801204940L);
        idList.add(801204540L);
        List<SkuStockTO> skuStockTOS = wareSkuService.hashStock(idList);
        System.out.println("结果是：" + skuStockTOS);
    }

    /**
     * 接收消息
     */
    @RabbitListener(queues = {""})
    public void receiveMessage(Object message){
        System.out.println("监听到队列中的消息是：" + message);
    }

}
