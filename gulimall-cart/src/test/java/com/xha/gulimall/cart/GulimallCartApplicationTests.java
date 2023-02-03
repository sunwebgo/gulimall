package com.xha.gulimall.cart;

import cn.hutool.core.math.MathUtil;
import cn.hutool.json.JSONUtil;
import com.xha.gulimall.cart.vo.CartInfoVO;
import org.apache.commons.lang.StringEscapeUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

@SpringBootTest
class GulimallCartApplicationTests {

    @Test
    public void test(){
        String test = "[{\"skuId\":4462049604,\"check\":true,\"title\":\"华为 Mate 50 Pro 16G 256G 红色\",\"image\":\"https://imagebed-xuhuaiang.oss-cn-shanghai.aliyuncs.com/gulimall/02d04345-7d6d-4302-8462-3447f572897c_73ab4d2e818d2211.jpg\",\"skuAttr\":[\"运行内存：16G\",\"内存：256G\",\"机身颜色：红色\"],\"price\":1000,\"count\":2,\"totalPrice\":2000}]";
        String s = JSONUtil.toJsonStr(test);
        CartInfoVO cartInfoVO = JSONUtil.toBean(s, CartInfoVO.class);
        System.out.println(cartInfoVO);
    }

}
