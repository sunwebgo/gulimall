package com.xha.gulimall.product;

import com.xha.gulimall.common.constants.NumberConstants;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
class GulimallProductApplicationTests {

    @Test
    void contextLoads() {
        String catelogId = "0";
        log.info("结果是：" + catelogId.equals(String.valueOf(NumberConstants.ZERO)));
    }

}
