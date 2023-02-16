package com.xha.gulimall.seckill.service;

import com.xha.gulimall.common.to.coupon.SeckillSkuRelationTO;
import com.xha.gulimall.common.utils.R;

import java.util.List;

public interface SeckillService {
    List<SeckillSkuRelationTO> getSeckillSkus();

    SeckillSkuRelationTO getSeckillInfoBySkuId(Long skuId);

    String seckill(String killId, Integer num, String key);
}
