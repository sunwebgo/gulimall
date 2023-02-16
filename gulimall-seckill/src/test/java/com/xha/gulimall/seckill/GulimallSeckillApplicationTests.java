package com.xha.gulimall.seckill;

import cn.hutool.core.date.LocalDateTimeUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.Date;

@SpringBootTest
class GulimallSeckillApplicationTests {

    @Test
    void contextLoads() {
        LocalDateTime now = LocalDateTime.now();
        String format = LocalDateTimeUtil.format(now, "yyyy-MM-dd HH:mm:ss");
        System.out.println(format);
    }

}
