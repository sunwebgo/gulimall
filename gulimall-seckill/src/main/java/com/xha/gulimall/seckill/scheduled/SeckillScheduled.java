package com.xha.gulimall.seckill.scheduled;


import com.xha.gulimall.seckill.constants.CommonConstants;
import com.xha.gulimall.seckill.service.SeckillScheduledService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@EnableScheduling
public class SeckillScheduled {

    @Resource
    private SeckillScheduledService seckillScheduledService;

    @Resource
    private RedissonClient redissonClient;

    /**
     * 上架秒杀商品定时任务
     */
    @Scheduled(cron = "0 0/10 * * * ?")
    private void uploadSeckillProduct() {
        log.info("开始上架秒杀商品");
//        1.创建分布式锁()
        RLock up_lock = redissonClient.getLock(CommonConstants.UPLOAD_LOCK);
        try {
//        2.获取锁
            up_lock.lock(CommonConstants.LOCK_MAX_TIME, TimeUnit.SECONDS);
            seckillScheduledService.uploadSeckillScheduled();
        } finally {
//        3.释放锁
            up_lock.unlock();
        }
    }

}
