package com.xha.gulimall.product;

import com.xha.gulimall.common.constants.NumberConstants;
import com.xha.gulimall.product.dao.AttrAttrgroupRelationDao;
import com.xha.gulimall.product.dao.SkuSaleAttrValueDao;
import com.xha.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.xha.gulimall.product.entity.SpuInfoDescEntity;
import com.xha.gulimall.product.entity.SpuInfoEntity;
import com.xha.gulimall.product.service.AttrGroupService;
import com.xha.gulimall.product.service.SpuInfoDescService;
import com.xha.gulimall.product.service.SpuInfoService;
import com.xha.gulimall.product.vo.SkuItemSaleAttrVO;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@SpringBootTest
class GulimallProductApplicationTests {

    @Resource
    private SpuInfoService spuInfoService;

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private SpuInfoDescService spuInfoDescService;

    @Resource
    private AttrAttrgroupRelationDao attrAttrgroupRelationDao;

    @Resource
    private AttrGroupService attrGroupService;

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

    @Test
    public void test3(){
        List<AttrAttrgroupRelationEntity> attrAttrgroupRelationList = attrAttrgroupRelationDao.selectList(null);
        System.out.println(attrAttrgroupRelationList);
    }

    @Test
    public void test4(){
        attrGroupService.getAttrGroupWithAttrsBySpuId(21L);
    }

    @Resource
    private SkuSaleAttrValueDao skuSaleAttrValueDao;

    @Test
    public void test5(){
        List<SkuItemSaleAttrVO> saleAttrBySpuId = skuSaleAttrValueDao.getSaleAttrBySpuId(27L);
        System.out.println(saleAttrBySpuId);
    }


}
