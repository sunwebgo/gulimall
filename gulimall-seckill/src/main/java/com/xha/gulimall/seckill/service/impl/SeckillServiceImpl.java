package com.xha.gulimall.seckill.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.xha.gulimall.common.constants.CacheConstants;
import com.xha.gulimall.common.constants.rabbitmq.seckill.SeckillRmConstants;
import com.xha.gulimall.common.to.coupon.SeckillSkuRelationTO;
import com.xha.gulimall.common.to.member.MemberTO;
import com.xha.gulimall.common.to.seckill.SeskillOrderTO;
import com.xha.gulimall.common.utils.R;
import com.xha.gulimall.seckill.constants.CommonConstants;
import com.xha.gulimall.seckill.interceptor.LoginInterceptor;
import com.xha.gulimall.seckill.service.SeckillService;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class SeckillServiceImpl implements SeckillService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private RabbitTemplate rabbitTemplate;

    /**
     * 获得秒杀的商品
     *
     * @return {@link R}
     */
    @Override
    public List<SeckillSkuRelationTO> getSeckillSkus() {
//        1.获取到当前缓存中的秒杀场次时间范围列表
        Set<String> keys = stringRedisTemplate.keys(CacheConstants.SECKILL_SESSION_CACHE + "*");
        List<SeckillSkuRelationTO> seckillSkuRelationTOList = null;
        for (String key : keys) {
            //            1.1将seckill:session:替换为空串
            String time_key = key.replace(CacheConstants.SECKILL_SESSION_CACHE, "");
//            1.2将key由_分割
            String[] timeList = time_key.split("_");
            Long start = Long.valueOf(timeList[0]);
            Long end = Long.valueOf(timeList[1]);
//            1.3获取当前时间
            long currentTime = new Date().getTime();
            if (currentTime >= start && currentTime <= end) {
//        2.获取到当前对应的秒杀场次和包含的skuID
                List<String> skuSky = stringRedisTemplate.opsForList().range(key, -100, 100);

//        3.根据key（秒杀场次_skuID）取出对应的秒杀商品信息
                BoundHashOperations<String, String, String> boundHashOperations = stringRedisTemplate.boundHashOps(CacheConstants.SECKILL_SKU_CACHE);
                List<String> relationSkuList = boundHashOperations.multiGet(skuSky);
                if (!CollectionUtils.isEmpty(relationSkuList)) {
                    seckillSkuRelationTOList = relationSkuList.stream().map(relationSku -> {
                        String jsonStr = JSONUtil.toJsonStr(relationSku);
                        return JSONUtil.toBean(jsonStr, SeckillSkuRelationTO.class);
                    }).collect(Collectors.toList());
                }
                break;
            }
        }
        return seckillSkuRelationTOList;
    }

    /**
     * 根据skuID查询当前商品的秒杀信息
     *
     * @return {@link R}
     */
    @Override
    public SeckillSkuRelationTO getSeckillInfoBySkuId(Long skuId) {
        BoundHashOperations<String, String, String> boundHashOperations = stringRedisTemplate
                .boundHashOps(CacheConstants.SECKILL_SKU_CACHE);
//        1.查询出所有参与秒杀的商品
        Set<String> keys = boundHashOperations.keys();
        for (String key : keys) {
//        2.分离key，得到商品的skuID
            String[] skuIdCache = key.split("_");
            if (skuIdCache[1].equals(skuId.toString())) {
//        3.如果skuID相等查询到当前对应的SeckillSkuRelationTO对象
                String seckillSkuStr = boundHashOperations.get(key);
                SeckillSkuRelationTO seckillSkuRelationTO = JSONUtil.toBean(seckillSkuStr, SeckillSkuRelationTO.class);
//        4.判断当前时间是否是处于秒杀时间
                long currentTime = new Date().getTime();
                if (currentTime < seckillSkuRelationTO.getStartTime().getTime() ||
                        currentTime > seckillSkuRelationTO.getEndTime().getTime()) {
                    seckillSkuRelationTO.setRandomCode(null);
                }
                return seckillSkuRelationTO;
            }
        }
        return null;
    }

    /**
     * 秒杀
     *
     * @param killId sessionId_skuId
     * @param num    秒杀数量
     * @param key    携带随机码
     * @return {@link String}
     */
    @Override
    public String seckill(String killId, Integer num, String key) {
//      获取到当前登录的用户
        MemberTO memberTO = LoginInterceptor.threadLoginUser.get();
        String orderSn = null;

//        1.获取当前秒杀商品的详细信息
        BoundHashOperations<String, String, String> boundHashOperations = stringRedisTemplate.boundHashOps(CacheConstants.SECKILL_SKU_CACHE);
        String skuInfo = boundHashOperations.get(killId);
        if (!StringUtils.isEmpty(skuInfo)) {
            SeckillSkuRelationTO seckillSkuRelationTO = JSONUtil.toBean(skuInfo, SeckillSkuRelationTO.class);
//            2.校验合法性
            long startTime = seckillSkuRelationTO.getStartTime().getTime();
            long endTime = seckillSkuRelationTO.getEndTime().getTime();
            long currentTime = new Date().getTime();
//              2.1检验秒杀是否过期
            if (currentTime >= startTime && currentTime <= endTime) {
//              2.2检验随机码
                if (seckillSkuRelationTO.getRandomCode().equals(key)) {
//              2.3检验限购数量
                    if (seckillSkuRelationTO.getSeckillLimit() >= num) {
//              2.4一人一单（幂等性），解决，缓存占位(userId_sessionId_skuId)
                        Long userId = memberTO.getId();
                        Long sessionId = seckillSkuRelationTO.getPromotionSessionId();
                        Long skuId = seckillSkuRelationTO.getSkuId();
                        String uniqueOrder = userId + "_" + sessionId + "_" + skuId;
                        Boolean exist = stringRedisTemplate.opsForValue().setIfAbsent(
                                CacheConstants.UNIQUE_ORDER_CACHE + uniqueOrder,
                                num.toString(),
                                endTime - currentTime,
                                TimeUnit.MILLISECONDS);
//                            2.4.1占位成功
                        if (exist) {
//              2.5使用分布式信号量机制扣减商品数量
                            RSemaphore semaphore = redissonClient.getSemaphore(
                                    CommonConstants.SECKILL_TOKEN + key);
//                            2.5.1获取到信号量
                            boolean acquireResult = semaphore.tryAcquire(num);
                            if (acquireResult) {
//                            2.5.2生成订单号（使用mp提供的IdWorker生成分布式唯一ID：雪花算法）
                                orderSn = IdWorker.getTimeId();
//                            2.5.3生成订单对象
                                SeskillOrderTO seskillOrderTO = new SeskillOrderTO();
                                seskillOrderTO.setMemberId(memberTO.getId())
                                        .setOrderSn(orderSn)
                                        .setSkuId(seckillSkuRelationTO.getSkuId())
                                        .setPromotionSessionId(seckillSkuRelationTO.getPromotionSessionId())
                                        .setSeckillPrice(seckillSkuRelationTO.getSeckillPrice());
//                            2.5.4发送订单对象到消息队列
                                rabbitTemplate.convertAndSend(
                                        SeckillRmConstants.ORDER_EVENT_EXCHANGE,
                                        SeckillRmConstants.ORDER_SECKILL_ORDER_BINDING,
                                        seskillOrderTO);
                            }
                        }
                    }
                }
            }
        }
        return orderSn;
    }

}
