package com.xha.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.rabbitmq.client.Channel;
import com.xha.gulimall.common.to.order.OrderTO;
import com.xha.gulimall.common.to.product.SkuStockTO;
import com.xha.gulimall.common.to.ware.WareSkuLockTO;
import com.xha.gulimall.common.to.rabbitmq.StockLockedTO;
import com.xha.gulimall.common.utils.PageUtils;
import com.xha.gulimall.common.utils.R;
import com.xha.gulimall.ware.entity.WareSkuEntity;
import org.springframework.amqp.core.Message;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 商品库存
 *
 * @author XuHuaianag
 * @email 2533694604@qq.com
 * @date 2022-12-29 16:47:49
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPageByCondition(Map<String, Object> params);

    R addStock(Long skuId, Long wareId, Integer skuNum);

    List<SkuStockTO> hashStock(List<Long> skuIds);

    void wareSkuLock(WareSkuLockTO wareSkuLockTO);


    void unlockedStock(StockLockedTO stockLockedTO);

    void unlockedStock(OrderTO orderTO);
}

