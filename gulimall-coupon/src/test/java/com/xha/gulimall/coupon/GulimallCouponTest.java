package com.xha.gulimall.coupon;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.LocalDateTimeUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xha.gulimall.common.to.coupon.SeckillSessionTO;
import com.xha.gulimall.coupon.dao.SeckillSessionDao;
import com.xha.gulimall.coupon.entity.SeckillSessionEntity;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@SpringBootTest
public class GulimallCouponTest {

    @Resource
    private SeckillSessionDao seckillSessionDao;

    @Test
    public void test(){
//        1.等到时间的格式化表示
        LocalDateTime beginTime = LocalDateTimeUtil.beginOfDay(LocalDateTime.now());
//          1.1算上今天，向后延长2天，一共三天时间
        LocalDateTime endTime = LocalDateTimeUtil
                .offset(LocalDateTimeUtil.endOfDay(LocalDateTime.now()),2,ChronoUnit.DAYS);
//          1.2格式化
        String beginFormatTime = LocalDateTimeUtil.format(beginTime, DatePattern.NORM_DATETIME_PATTERN);
        String endFormatTime =  LocalDateTimeUtil.format(endTime, DatePattern.NORM_DATETIME_PATTERN);
        LambdaQueryWrapper<SeckillSessionEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.between(SeckillSessionEntity::getStartTime,beginFormatTime,endFormatTime);
        List<SeckillSessionEntity> seckillSessionList = seckillSessionDao.selectList(queryWrapper);
        List<SeckillSessionTO> seckillSessionTOList = null;
        if (!CollectionUtils.isEmpty(seckillSessionList)){
            seckillSessionTOList = seckillSessionList.stream().map(seckillSessionEntity -> {
                SeckillSessionTO seckillSessionTO = new SeckillSessionTO();
                BeanUtils.copyProperties(seckillSessionEntity, seckillSessionTO);
                return seckillSessionTO;
            }).collect(Collectors.toList());
        }
        System.out.println(seckillSessionList);


    }
}
