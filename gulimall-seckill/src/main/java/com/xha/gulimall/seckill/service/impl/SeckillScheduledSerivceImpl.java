package com.xha.gulimall.seckill.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.xha.gulimall.common.constants.CacheConstants;
import com.xha.gulimall.common.to.coupon.SeckillSessionTO;
import com.xha.gulimall.common.to.coupon.SeckillSkuRelationTO;
import com.xha.gulimall.common.to.product.SkuInfoTO;
import com.xha.gulimall.seckill.constants.CommonConstants;
import com.xha.gulimall.seckill.feign.CouponFeignService;
import com.xha.gulimall.seckill.feign.ProductFeignService;
import com.xha.gulimall.seckill.service.SeckillScheduledService;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SeckillScheduledSerivceImpl implements SeckillScheduledService {


    @Resource
    private CouponFeignService couponFeignService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private ProductFeignService productFeignService;

    @Resource
    private RedissonClient redissonClient;

    /**
     * 上传秒杀商品
     */
    @Override
    public void uploadSeckillScheduled() {
//        1.获取到最近3天需要参加秒杀的活动
        List<SeckillSessionTO> seckillSessionList = couponFeignService.getSeckillSession();
        if (!CollectionUtils.isEmpty(seckillSessionList)) {
//        2.缓存秒杀活动信息
            saveSessionInfo(seckillSessionList);
//        3.缓存秒杀活动关联的商品信息
            saveSessionSkuInfo(seckillSessionList);
        }

    }

    /**
     * 缓存秒杀活动信息
     *
     * @param seckillSessionList 秒杀会话列表
     */
    private void saveSessionInfo(List<SeckillSessionTO> seckillSessionList) {
        seckillSessionList.stream().forEach(seckillSessionTO -> {
//            1.将开始时间和结束时间作为key
            Long startTime = seckillSessionTO.getStartTime().getTime();
            Long endTime = seckillSessionTO.getEndTime().getTime();
            String key = CacheConstants.SECKILL_SESSION_CACHE + startTime + "_" + endTime;
            if (!stringRedisTemplate.hasKey(key)) {
//            2.将sessionId和skuId作为value
                List<SeckillSkuRelationTO> relationSkuList = seckillSessionTO.getRelationSkus();
                List<String> session_sku_id = relationSkuList.stream()
                        .map(seckillSkuRelationTO -> {
                            return seckillSkuRelationTO.getPromotionSessionId() + "_" +
                                    seckillSkuRelationTO.getSkuId();
                        })
                        .collect(Collectors.toList());

                if (!CollectionUtils.isEmpty(session_sku_id)) {
//            3.缓存秒杀活动
                    stringRedisTemplate
                            .opsForList()
                            .leftPushAll(key, session_sku_id);
                }

            }
        });
    }

    /**
     * 缓存秒杀活动关联的商品信息
     *
     * @param seckillSessionList 秒杀会话列表
     */
    private void saveSessionSkuInfo(List<SeckillSessionTO> seckillSessionList) {

//        1.远程查询出所有的sku信息
        List<SkuInfoTO> allSkuInfoList = productFeignService.getAllSkuInfoList();

//        2.查询封装SeckillSkuRelationTO对象
        seckillSessionList.stream().forEach(seckillSessionTO -> {

            List<SeckillSkuRelationTO> relationSkuList = seckillSessionTO.getRelationSkus();
            List<SeckillSkuRelationTO> seckillSkuRelationTOS = relationSkuList.stream().map(seckillSkuRelationTO -> {
                return addSkuDetailInfo(allSkuInfoList, seckillSkuRelationTO);
            }).collect(Collectors.toList());

//        3.缓存秒杀活动关联的商品信息
            seckillSkuRelationTOS.stream().forEach(seckillSkuRelationTO -> {
//            3.1生成随机码
                String token = IdWorker.getTimeId();
                seckillSkuRelationTO.setStartTime(seckillSessionTO.getStartTime())
                        .setEndTime(seckillSessionTO.getEndTime())
                        .setRandomCode(token);
                String session_sku_key = seckillSkuRelationTO.getPromotionSessionId() + "_" + seckillSkuRelationTO.getSkuId();
                if (!stringRedisTemplate.boundHashOps(CacheConstants.SECKILL_SKU_CACHE)
                        .hasKey(session_sku_key)) {
                    String seckillSkuInfo = JSONUtil.toJsonStr(seckillSkuRelationTO);
                    stringRedisTemplate.boundHashOps(CacheConstants.SECKILL_SKU_CACHE)
                            .put(session_sku_key, seckillSkuInfo);
//            3.2创建信号量,信号量名为随机码,信号量初始大小为秒杀商品数量(相当于商品库存)
                    Integer seckillCount = seckillSkuRelationTO.getSeckillCount();
                    redissonClient.getSemaphore(CommonConstants.SECKILL_TOKEN + token)
                            .trySetPermits(seckillCount);
                }
            });
        });
    }

    /**
     * 添加sku详细信息
     *
     * @return {@link SeckillSkuRelationTO}
     */
    public SeckillSkuRelationTO addSkuDetailInfo(List<SkuInfoTO> allSkuInfoList, SeckillSkuRelationTO seckillSkuRelationTO) {
        allSkuInfoList.stream().forEach(skuInfoTO -> {
            if (skuInfoTO.getSkuId().equals(seckillSkuRelationTO.getSkuId())) {
                seckillSkuRelationTO.setSkuInfoTO(skuInfoTO);
            }
        });
        return seckillSkuRelationTO;
    }
}
